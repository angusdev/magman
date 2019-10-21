package org.ellab.magman;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

public class ImageViewerCanvas {
    public static final int PREV_PAGE = -1;
    public static final int NEXT_PAGE = -2;

    private static final int MOUSE_CLICK_MOVE_THERSHOLD = 5;
    private static final int MOUSE_CLICK_TIME_THERSHOLD_MS = 200;

    private static final float PREV_PAGE_AREA = 0.1f;
    private static final float NEXT_PAGE_AREA = 0.9f;

    private ImageViewerCanvas me;
    private Canvas canvas;

    private boolean stretchToFit = true;
    private boolean shrinkToFit = true;
    private float maxZoom = Float.MAX_VALUE;
    private float minZoom = 0f; // 0 means fit to window
    private float wheelZoomStep = 0.1f;
    private boolean wheelUpToZoomIn = true;

    private BufferedImage image;
    private Point imageSize = new Point(0, 0);

    private Image scaledImage;
    private float scaledImageScale;

    private boolean isMouseDown;
    private long mouseDownTime;
    private boolean movedOrZoomed = false;

    private float zoom = 1.0f;
    private Point start = new Point(0, 0);

    // offset before mouse down
    private Point offset = new Point(0, 0);

    // offset after mouse down and before mouse up
    private Point moveOffset = new Point(0, 0);

    private List<ImageViewerCanvasAdapter> listener = new ArrayList<>();

    public static class ImageViewerCanvasAdapter {
        public void changePage(ImageViewerCanvas c, int offset) {
        }
    }

    public ImageViewerCanvas(Canvas canvas) {
        this.me = this;
        this.canvas = canvas;

        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                if (image != null) {
                    recalcZoomOffset();
                    if (scaledImage == null || zoom != scaledImageScale) {
                        scaledImage = resize(image, zoom);
                        scaledImageScale = zoom;
                    }
                    doubleBufferedDraw(e.display, e.gc, scaledImage, offset.x + moveOffset.x, offset.y + moveOffset.y,
                            e.width, e.height);
                }
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {
                start.x = e.x;
                start.y = e.y;
                moveOffset.x = 0;
                moveOffset.y = 0;
                isMouseDown = true;
                mouseDownTime = new Date().getTime();
            }

            @Override
            public void mouseUp(MouseEvent e) {
                boolean isClick = withinMouseClickThershold(e);

                isMouseDown = false;
                mouseDownTime = 0;

                if (isClick) {
                    Rectangle rect = canvas.getBounds();
                    if (start.x <= rect.width * PREV_PAGE_AREA) {
                        listener.stream().forEach(a -> a.changePage(me, PREV_PAGE));
                    }
                    else if (start.x >= rect.width * NEXT_PAGE_AREA) {
                        listener.stream().forEach(a -> a.changePage(me, NEXT_PAGE));
                    }

                    return;
                }

                offset.x += e.x - start.x;
                offset.y += e.y - start.y;

                normalizePoistion();

                moveOffset.x = 0;
                moveOffset.y = 0;

                canvas.redraw();
            }
        });

        canvas.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (isMouseDown) {
                    if (withinMouseClickThershold(e)) {
                        return;
                    }
                    movedOrZoomed = true;

                    moveOffset.x = e.x - start.x;
                    moveOffset.y = e.y - start.y;

                    canvas.redraw();
                }
            }
        });

        canvas.addMouseWheelListener(new MouseWheelListener() {
            public void mouseScrolled(MouseEvent e) {
                if (zoom <= minZoom && ((wheelUpToZoomIn && e.count < 0) || (!wheelUpToZoomIn && e.count > 0))) {
                    return;
                }

                movedOrZoomed = true;

                zoom = Math.min(maxZoom, Math.max(minZoom,
                        zoom + wheelZoomStep * (e.count / Math.abs(e.count)) * (wheelUpToZoomIn ? 1 : -1)));

                if ((shrinkToFit || stretchToFit) && minZoom == 0) {
                    // won't smaller then canvas
                    zoom = Math.max(zoom, calcFitScale(canvas.getBounds()));
                }
                zoom = Math.max(zoom, 0.1f);

                normalizePoistion();

                canvas.redraw();
            }
        });
    }

    public ImageViewerCanvas withStretchToFit(final boolean stretchToFit) {
        this.stretchToFit = stretchToFit;
        return this;
    }

    public ImageViewerCanvas withShrinkToFit(final boolean shrinkToFit) {
        this.shrinkToFit = shrinkToFit;
        return this;
    }

    public ImageViewerCanvas withMaxZoom(final float maxZoom) {
        this.maxZoom = maxZoom;
        return this;
    }

    public ImageViewerCanvas withMinZoom(final float minZoom) {
        this.minZoom = minZoom;
        return this;
    }

    public ImageViewerCanvas withWheelZoomStep(final float wheelZoomStep) {
        this.wheelZoomStep = wheelZoomStep;
        return this;
    }

    public ImageViewerCanvas withWheelUpToZoomIn(final boolean wheelUpToZoomIn) {
        this.wheelUpToZoomIn = wheelUpToZoomIn;
        return this;
    }

    public ImageViewerCanvas addListener(final ImageViewerCanvasAdapter adapter) {
        this.listener.add(adapter);
        return this;
    }

    private boolean withinMouseClickThershold(MouseEvent e) {
        return moveOffset.x == 0 && moveOffset.y == 0 && e.x - start.x <= MOUSE_CLICK_MOVE_THERSHOLD
                && e.y - start.y <= MOUSE_CLICK_MOVE_THERSHOLD
                && new Date().getTime() - mouseDownTime <= MOUSE_CLICK_TIME_THERSHOLD_MS;
    }

    public static Image resize(Image image, float scale) {
        return resize(convertToAWT(image.getImageData()), scale);
    }

    // https://stackoverflow.com/questions/4752748/swt-how-to-do-high-quality-image-resize/39449656
    public static Image resize(BufferedImage image, float scale) {
        // resize buffered image
        int newWidth = Math.round(scale * image.getWidth());
        int newHeight = Math.round(scale * image.getHeight());

        // determine scaling mode for best result: if downsizing, use area averaging, if upsizing, use smooth scaling
        // (usually bilinear).
        int mode = scale < 1 ? BufferedImage.SCALE_AREA_AVERAGING : BufferedImage.SCALE_SMOOTH;
        java.awt.Image scaledImage = image.getScaledInstance(newWidth, newHeight, mode);

        // convert the scaled image back to a buffered image
        image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(scaledImage, 0, 0, null);

        // reconstruct swt image
        ImageData imageData = convertToSWT(image);
        return new org.eclipse.swt.graphics.Image(Display.getDefault(), imageData);
    }

    private static BufferedImage convertToAWT(ImageData data) {
        ColorModel colorModel = null;
        PaletteData palette = data.palette;
        if (palette.isDirect) {
            colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
            BufferedImage bufferedImage = new BufferedImage(colorModel,
                    colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    RGB rgb = palette.getRGB(pixel);
                    pixelArray[0] = rgb.red;
                    pixelArray[1] = rgb.green;
                    pixelArray[2] = rgb.blue;
                    raster.setPixels(x, y, 1, 1, pixelArray);
                }
            }
            return bufferedImage;
        }
        else {
            RGB[] rgbs = palette.getRGBs();
            byte[] red = new byte[rgbs.length];
            byte[] green = new byte[rgbs.length];
            byte[] blue = new byte[rgbs.length];
            for (int i = 0; i < rgbs.length; i++) {
                RGB rgb = rgbs[i];
                red[i] = (byte) rgb.red;
                green[i] = (byte) rgb.green;
                blue[i] = (byte) rgb.blue;
            }
            if (data.transparentPixel != -1) {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
            }
            else {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
            }
            BufferedImage bufferedImage = new BufferedImage(colorModel,
                    colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    pixelArray[0] = pixel;
                    raster.setPixel(x, y, pixelArray);
                }
            }
            return bufferedImage;
        }
    }

    private static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
                    colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                    colorModel.getPixelSize(), palette);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    data.setPixel(x, y, pixel);
                    if (colorModel.hasAlpha()) {
                        data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                    }
                }
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                    colorModel.getPixelSize(), palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
            ComponentColorModel colorModel = (ComponentColorModel) bufferedImage.getColorModel();
            // ASSUMES: 3 BYTE BGR IMAGE TYPE
            PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                    colorModel.getPixelSize(), palette);
            // This is valid because we are using a 3-byte Data model with no transparent pixels
            data.transparentPixel = -1;
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }
        return null;
    }

    private static void doubleBufferedDraw(Display display, GC dest, Image image, int x, int y, int width, int height) {
        Image bufferImage = new Image(display, width, height);
        GC gc = new GC(bufferImage);
        gc.setBackground(dest.getBackground());
        gc.fillRectangle(0, 0, width + 1, height + 1);
        gc.drawImage(image, x, y);

        dest.drawImage(bufferImage, 0, 0);

        bufferImage.dispose();
        gc.dispose();
    }

    public void changeImage(final BufferedImage image) {
        this.image = image;
        if (scaledImage != null) {
            scaledImage.dispose();
            scaledImage = null;
        }
        imageSize.x = image.getWidth();
        imageSize.y = image.getHeight();

        movedOrZoomed = false;

        recalcZoomOffset();

        canvas.redraw();
    }

    private float calcFitScale(final Rectangle rect) {
        float fitScale = 1.0f;

        if ((imageSize.x > rect.width && shrinkToFit) || imageSize.x < rect.width && stretchToFit) {
            fitScale = rect.width / (float) imageSize.x;
        }
        if ((imageSize.y > rect.height && shrinkToFit) || imageSize.y < rect.height && stretchToFit) {
            float tmp = rect.height / (float) imageSize.y;
            fitScale = fitScale < tmp ? fitScale : tmp;
        }

        return fitScale;
    }

    private void recalcZoomOffset() {
        if (!movedOrZoomed) {
            Rectangle rect = canvas.getBounds();

            zoom = calcFitScale(rect);

            int scaledWidth = (int) (imageSize.x * zoom);
            int scaledHeight = (int) (imageSize.y * zoom);

            offset.x = scaledWidth < rect.width ? rect.width / 2 - scaledWidth / 2 : 0;
            offset.y = scaledHeight < rect.height ? rect.height / 2 - scaledHeight / 2 : 0;
        }
    }

    private void normalizePoistion() {
        int scaledWidth = (int) (imageSize.x * zoom);
        int scaledHeight = (int) (imageSize.y * zoom);
        Rectangle rect = canvas.getBounds();

        if (scaledWidth < rect.width) {
            // image fit in the canvas horizontally, center horizontally
            offset.x = rect.width / 2 - scaledWidth / 2;
        }
        else if ((offset.x < 0 && offset.x + scaledWidth < rect.width)
                || (offset.x > 0 && offset.x + scaledWidth > rect.width)) {
            if (moveOffset.x > 0) {
                // mouse move from left to right, snap to left edge
                offset.x = 0;
            }
            else {
                // mouse move from right to left , snap to right edge
                offset.x = (int) Math.floor(rect.width - scaledWidth);
            }
        }

        if (scaledHeight < rect.height) {
            // image fit in the canvas vertically, center vertically
            offset.y = rect.height / 2 - scaledHeight / 2;
        }
        else if ((offset.y < 0 && offset.y + scaledHeight < rect.width)
                || (offset.y > 0 && offset.y + scaledHeight > rect.height)) {
            if (moveOffset.y > 0) {
                // mouse move from top to bottom, snap to top edge
                offset.y = 0;
            }
            else {
                // mouse move from bottom to top, snap to bottom edge
                offset.y = (int) Math.floor(rect.height - scaledHeight);
            }
        }
    }
}
