package org.ellab.magman;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.ellab.magman.FileCollections.MagazineCollection;

public class SummaryDialog extends Dialog {
    protected Shell shell;

    private FileCollections fc;

    private Composite composite;

    public SummaryDialog(Shell parent, int style) {
        super(parent, style);
    }

    public void open(FileCollections fc) {
        this.fc = fc;

        createContents();
        refresh();

        shell.open();

        shell.layout();
        if (composite.getChildren().length > 0) {
            Control c = composite.getChildren()[composite.getChildren().length - 1];
            int shellRight = shell.getBounds().x + shell.getBounds().width;
            int cRight = c.getBounds().x + c.getBounds().width;
            if (shellRight > cRight) {
                shell.setSize(cRight, shell.getBounds().height);
            }
        }

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        Rectangle parentSize = shell.getMonitor().getClientArea();
        shell.setSize(Math.max(800, parentSize.width - 200), Math.max(600, parentSize.height - 200));
        shell.setText("Magman");
        shell.setLayout(new FillLayout(SWT.HORIZONTAL));

        composite = new Composite(shell, SWT.NONE);
        RowLayout rl_composite = new RowLayout(SWT.VERTICAL);
        rl_composite.fill = true;
        rl_composite.pack = false;
        composite.setLayout(rl_composite);
    }

    private void refresh() {
        final Color foreground = shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        final Color background = shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        for (MagazineCollection mc : fc.items()) {
            for (String group : mc.groups()) {
                for (FileItem.Type type : mc.group(group).keySet()) {
                    final String name = (mc.getName() + " " + group).trim() + " (" + type.toString().charAt(0) + ")";
                    String lastValid = "";
                    String lastMissing = "";
                    for (FileItem fi : mc.files().getFileItems()) {
                        if (group.equals(fi.getGroup()) && type.equals(fi.getType())) {
                            if (fi.isValid()) {
                                lastValid = fi.getDateStr();
                            }
                            else if (fi.isMissing()) {
                                lastMissing = fi.getDateStr();
                            }
                        }
                    }

                    final String str = (name + "\n" + lastValid + " " + lastMissing + "\n").trim();
                    final StyleRange[] range = new StyleRange[lastMissing.length() > 0 ? 2 : 1];
                    range[0] = new StyleRange();
                    range[0].start = 0;
                    range[0].length = name.length();
                    range[0].fontStyle = SWT.BOLD;
                    range[0].underline = true;
                    if (lastMissing.length() > 0) {
                        range[1] = new StyleRange(name.length() + 1 + lastValid.length() + 1, lastMissing.length(),
                                new Color(shell.getDisplay(), new RGB(255, 0, 0)), null);
                    }

                    final StyledText text = new StyledText(composite, SWT.NONE);
                    text.setText(str);
                    text.setStyleRanges(range);
                    text.setEditable(false);
                    text.setEnabled(false);
                    text.setForeground(foreground);
                    text.setBackground(background);
                }
            }
        }
    }
}
