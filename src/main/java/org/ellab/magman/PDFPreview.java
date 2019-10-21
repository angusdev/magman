package org.ellab.magman;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDFPreview implements Closeable {
    public static final int FIRST_PAGE = 0;
    public static final int LAST_PAGE = -1;
    public static final int PREV_PAGE = -2;
    public static final int NEXT_PAGE = -3;

    private int dpi = 300;
    private PDDocument doc;
    private PDFRenderer pdfRenderer;
    private int totalPages;
    private int currPage;

    public int getTotalPages() {
        return totalPages;
    }

    public void load(String file) throws IOException {
        if (doc != null) {
            doc.close();
            doc = null;
        }

        doc = PDDocument.load(new File(file));
        pdfRenderer = new PDFRenderer(doc);
        totalPages = doc.getDocumentCatalog().getPages().getCount();
        currPage = 0;
    }

    @Override
    public void close() throws IOException {
        if (doc != null) {
            doc.close();
            doc = null;
        }
        pdfRenderer = null;
        totalPages = 0;
        currPage = 0;
    }

    public Object[] page(int p) throws IOException {
        if (p == FIRST_PAGE) {
            currPage = 0;
        }
        else if (p == LAST_PAGE) {
            currPage = totalPages - 1;
        }
        else if (p == PREV_PAGE) {
            currPage = Math.max(currPage - 1, 0);
        }
        else if (p == NEXT_PAGE) {
            currPage = Math.min(currPage + 1, totalPages - 1);
        }
        else {
            currPage = Math.min(Math.max(p, 1), totalPages) - 1;
        }

        BufferedImage image = pdfRenderer.renderImageWithDPI(currPage, dpi, ImageType.RGB);

        return new Object[] { image, currPage + 1 };
    }

    public BufferedImage dpi(int dpi) throws IOException {
        this.dpi = dpi;

        return pdfRenderer.renderImageWithDPI(currPage, dpi, ImageType.RGB);
    }
}
