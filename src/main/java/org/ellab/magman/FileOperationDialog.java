package org.ellab.magman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.ellab.magman.FileCollections.MagazineCollection;

public class FileOperationDialog extends Dialog {
    protected Object result;
    protected Shell shell;
    private Table table;

    private static final int COL_GROUP = 0;
    private static final int COL_SRC = 1;
    private static final int COL_DEST = 2;
    private static final int COL_STATUS = 3;

    public static class Item {
        private String path;
        private MagazineCollection mc;
        private FileItem.Type type;
        private String src;
        private String dest;
        private String oridest;

        public Item(String path, String src, MagazineCollection mc, FileItem.Type type, String dest) {
            this.path = path;
            this.mc = mc;
            this.type = type;
            this.src = src;
            this.oridest = dest;
            this.dest = dest;
        }
    }

    private TableColumn tblclmnGroup;
    private TableColumn tblclmnFrom;
    private TableColumn tblclmnTo;
    private TableColumn tblclmnStatus;

    private Button btnPrevMonth;
    private Button btnNextMonth;
    private Button btnMonthly;
    private Button btnBiMonthly;

    public FileOperationDialog(Shell parent, int style) {
        super(parent, style);
    }

    public Object open(List<Item> files) {
        createContents();
        init(files);

        shell.open();
        shell.layout();

        Rectangle parentSize = getParent().getBounds();
        Rectangle shellSize = shell.getBounds();
        shellSize.width = Math.min(shellSize.width, parentSize.width - 100);
        shell.setSize(shellSize.width, shellSize.height);
        int locationX = (parentSize.width - shellSize.width) / 2 + parentSize.x;
        int locationY = (parentSize.height - shellSize.height) / 2 + parentSize.y;
        shell.setLocation(new Point(locationX, locationY));

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(800, 400);
        shell.setText("Magman");
        shell.setLayout(new GridLayout(1, false));

        table = new Table(shell, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION | SWT.MULTI);
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onTableSelected(e);
            }
        });
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        tblclmnGroup = new TableColumn(table, SWT.NONE);
        tblclmnGroup.setWidth(100);
        tblclmnGroup.setText("Group");

        tblclmnFrom = new TableColumn(table, SWT.NONE);
        tblclmnFrom.setWidth(100);
        tblclmnFrom.setText("From");

        tblclmnTo = new TableColumn(table, SWT.NONE);
        tblclmnTo.setWidth(100);
        tblclmnTo.setText("To");

        tblclmnStatus = new TableColumn(table, SWT.NONE);
        tblclmnStatus.setWidth(100);
        tblclmnStatus.setText("Status");

        Composite composite_1 = new Composite(shell, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        composite_1.setLayout(new RowLayout(SWT.HORIZONTAL));

        btnPrevMonth = new Button(composite_1, SWT.NONE);
        btnPrevMonth.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processMonthly(1, -1);
            }
        });
        btnPrevMonth.setEnabled(false);
        btnPrevMonth.setText("Prev Month");

        btnNextMonth = new Button(composite_1, SWT.NONE);
        btnNextMonth.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processMonthly(1, 1);
            }
        });
        btnNextMonth.setEnabled(false);
        btnNextMonth.setText("Next Month");

        btnMonthly = new Button(composite_1, SWT.NONE);
        btnMonthly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processMonthly(2, null);
            }
        });
        btnMonthly.setEnabled(false);
        btnMonthly.setText("Monthly");

        btnBiMonthly = new Button(composite_1, SWT.NONE);
        btnBiMonthly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processMonthly(3, null);
            }
        });
        btnBiMonthly.setEnabled(false);
        btnBiMonthly.setText("Bi-monthly");

        Label lblSep = new Label(composite_1, SWT.SEPARATOR | SWT.VERTICAL);
        RowData layoutData = new RowData();
        layoutData.height = btnPrevMonth.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        lblSep.setLayoutData(layoutData);

        Button btnReset = new Button(composite_1, SWT.NONE);
        btnReset.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Arrays.stream(table.getItems()).forEach(i -> i.setText(COL_DEST, ((Item) i.getData()).oridest));
                tblclmnTo.pack();
            }
        });
        btnReset.setText("Reset");

        Composite composite = new Composite(shell, SWT.NONE);
        FillLayout fl_composite = new FillLayout(SWT.HORIZONTAL);
        fl_composite.spacing = 30;
        composite.setLayout(fl_composite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

        Button btnRename = new Button(composite, SWT.NONE);
        btnRename.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                renameFiles();
            }
        });
        btnRename.setText("&Rename All");

        Button btnClose = new Button(composite, SWT.NONE);
        btnClose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.dispose();
            }
        });
        btnClose.setText("&Close");
    }

    private void init(List<Item> files) {
        files.forEach(f -> {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setData(f);
            item.setChecked(f.dest != null);
            item.setText(COL_GROUP, f.mc != null ? f.mc.getName() : "");
            item.setText(COL_SRC, f.src);
            item.setText(COL_DEST, f.dest != null ? f.dest : "");
        });
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            // don't pack last
            table.getColumn(i).pack();
        }
    }

    private void onTableSelected(SelectionEvent e) {
        btnPrevMonth.setEnabled(false);
        btnNextMonth.setEnabled(false);
        btnMonthly.setEnabled(false);
        btnBiMonthly.setEnabled(false);

        if (Arrays.stream(table.getSelection())
                .anyMatch(i -> FileItem.Type.Monthly.equals(((Item) i.getData()).type))) {
            btnPrevMonth.setEnabled(true);
            btnNextMonth.setEnabled(true);
            btnMonthly.setEnabled(true);
            btnBiMonthly.setEnabled(true);
        }
    };

    /**
     * Process the monthly filename.
     * 
     * @param action
     *            1 - add / minus month, 2 - change to monthly, 3 - change to bi-monthly
     */
    private void processMonthly(int action, Integer add) {
        final Pattern pattern = Pattern.compile("(.*\\s)(\\d{4})(\\d{2})(\\-(\\d{2}))?(\\.[^\\.]+)$");
        Arrays.stream(table.getSelection()).filter(i -> FileItem.Type.Monthly.equals(((Item) i.getData()).type))
                .forEach(i -> {
                    Item it = (Item) i.getData();
                    Matcher m = pattern.matcher(it.dest);
                    if (m.matches()) {
                        final String prefix = m.group(1);
                        String year = m.group(2);
                        int m1 = Integer.parseInt(m.group(3));
                        int m2 = m.group(5) != null ? Integer.parseInt(m.group(5)) : -1;
                        final String ext = m.group(6);
                        if (action == 1) {
                            m1 = m1 + add;
                            m2 = m2 > 0 ? m2 + add : m2;
                            if (m1 <= 0) {
                                year = "" + (Integer.parseInt(year) - 1);
                                m1 = 12;
                            }
                            else if (m1 >= 13) {
                                year = "" + (Integer.parseInt(year) + 1);
                                m1 = 1;
                            }
                            if (m2 == 0) {
                                m2 = 12;
                            }
                            else if (m2 >= 13) {
                                m2 = 1;
                            }
                            it.dest = prefix + year + (m1 < 10 ? "0" : "") + m1
                                    + (m2 > 0 ? ("-" + (m2 < 10 ? "0" : "") + m2) : "") + ext;
                            i.setText(COL_DEST, it.dest);
                        }
                        else if (action == 2) {
                            it.dest = prefix + year + (m1 < 10 ? "0" : "") + m1 + ext;
                            i.setText(COL_DEST, it.dest);
                        }
                        else if (action == 3) {
                            if (m2 < 0) {
                                m2 = m1 + 1;
                                if (m2 >= 13) {
                                    m2 = 1;
                                }
                                it.dest = prefix + year + (m1 < 10 ? "0" : "") + m1
                                        + (m2 > 0 ? ("-" + (m2 < 10 ? "0" : "") + m2) : "") + ext;
                                i.setText(COL_DEST, it.dest);
                            }
                        }
                    }
                });
        tblclmnTo.pack();
    }

    private void renameFiles() {
        for (int i = 0; i < table.getItemCount(); i++) {
            TableItem ti = table.getItem(i);
            if (ti.getChecked()) {
                Item item = (Item) ti.getData();
                Path src = Paths.get(item.path);
                try {
                    Files.move(src, src.resolveSibling(item.dest));
                    ti.setText(COL_STATUS, "Renamed");
                }
                catch (IOException ex) {
                    ti.setText(COL_STATUS, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
}
