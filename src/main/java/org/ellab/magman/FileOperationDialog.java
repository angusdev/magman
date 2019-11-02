package org.ellab.magman;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
    private Shell shell;
    private Table table;

    private FileCollections fc;

    private static final int COL_GROUP = 0;
    private static final int COL_FREQ = 1;
    private static final int COL_SRC = 2;
    private static final int COL_DEST = 3;
    private static final int COL_STATUS = 4;

    private static final int OPER_PREV_NEXT = 1;
    private static final int OPER_EXTEND_REDUCE = 2;
    private static final int OPER_WEEKLY = 11;
    private static final int OPER_MONTHLY = 12;
    private static final int OPER_QUARTERLY = 13;
    private static final int OPER_ISSUE = 14;
    private static final int OPER_NONE = 15;
    private static final Map<Integer, FileItem.Type> OPER_TO_FILEITEM_TYPE = new HashMap<>();
    static {
        OPER_TO_FILEITEM_TYPE.put(OPER_WEEKLY, FileItem.Type.Weekly);
        OPER_TO_FILEITEM_TYPE.put(OPER_MONTHLY, FileItem.Type.Monthly);
        OPER_TO_FILEITEM_TYPE.put(OPER_QUARTERLY, FileItem.Type.Quarterly);
        OPER_TO_FILEITEM_TYPE.put(OPER_ISSUE, FileItem.Type.Issue);
    }

    public static class Item {
        private String path;
        private MagazineCollection mc;
        private FileItem.Type type;
        private FileItem.Type oritype;
        private String src;
        private String dest;
        private String oridest;

        public Item(String path, String src, FileItem fi, MagazineCollection mc) {
            this.path = path;
            this.mc = mc;
            this.oritype = fi != null ? fi.getType() : null;
            this.type = this.oritype;
            this.src = src;
            this.oridest = fi != null ? fi.getFilename() : null;
            this.dest = this.oridest;
        }

        public Item(String path, String src, String dest) {
            this.path = path;
            this.src = src;
            this.oridest = dest;
            this.dest = dest;
        }
    }

    private TableColumn tblclmnGroup;
    private TableColumn tblclmnFreq;
    private TableColumn tblclmnFrom;
    private TableColumn tblclmnTo;
    private TableColumn tblclmnStatus;

    private Button btnPrev;
    private Button btnNext;
    private Button btnExtend;
    private Button btnReduce;
    private Button btnWeekly;
    private Button btnMonthly;
    private Button btnQuarterly;
    private Button btnIssue;
    private Button btnNone;

    public FileOperationDialog(Shell parent, int style) {
        super(parent, style);
    }

    public void open(String[] files, FileCollections fc) {
        this.fc = fc;

        List<FileOperationDialog.Item> renameList = new ArrayList<>();
        Arrays.stream(files).forEach(file -> {
            final String oriName = new File(file).getName();
            FileItem fi = fc.guessFilename(oriName, null);
            if (fi != null) {
                renameList.add(new Item(file, oriName, fi, fc.mc(fi.getParentId())));
            }
            else {
                renameList.add(new Item(file, oriName, Utils.makeCleanFilename(oriName)));
            }
        });

        renameList.sort(new Comparator<FileOperationDialog.Item>() {
            @Override
            public int compare(FileOperationDialog.Item a, FileOperationDialog.Item b) {
                if (a.mc == null) {
                    return -1;
                }
                else if (b.mc == null) {
                    return 1;
                }
                else if (!a.mc.getName().equals(b.mc.getName())) {
                    return a.mc.getName().compareTo(b.mc.getName());
                }
                else if (a.type == null) {
                    return -1;
                }
                else if (!a.type.equals(b.type)) {
                    return a.type.ordinal() - b.type.ordinal();
                }
                else {
                    return a.dest.compareTo(b.dest);
                }
            }
        });

        open(renameList);
    }

    private void open(List<Item> files) {
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
    }

    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(800, 400);
        shell.setText("Magman");
        shell.setLayout(new GridLayout(1, false));

        table = new Table(shell, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION | SWT.MULTI);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Point point = new Point(e.x, e.y);
                TableItem ti = table.getItem(point);
                if (ti != null) {
                    try {
                        String path = ((Item) ti.getData()).path;
                        if (path.toLowerCase().endsWith(".pdf")) {
                            new PreviewDialog(shell,
                                    SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE | SWT.CLOSE | SWT.MAX | SWT.MIN)
                                            .open(path);
                        }
                    }
                    catch (IOException ex) {
                        SwtUtils.errorBox(shell, ex);
                    }
                }
            }
        });
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

        tblclmnFreq = new TableColumn(table, SWT.NONE);
        tblclmnFreq.setWidth(100);
        tblclmnFreq.setText("Frequency");

        tblclmnFrom = new TableColumn(table, SWT.NONE);
        tblclmnFrom.setWidth(100);
        tblclmnFrom.setText("From");

        tblclmnTo = new TableColumn(table, SWT.NONE);
        tblclmnTo.setWidth(100);
        tblclmnTo.setText("To");

        tblclmnStatus = new TableColumn(table, SWT.NONE);
        tblclmnStatus.setWidth(100);
        tblclmnStatus.setText("Status");

        Composite compositeDateButtons = new Composite(shell, SWT.NONE);
        compositeDateButtons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        RowLayout rl_compositeDateButtons = new RowLayout(SWT.HORIZONTAL);
        rl_compositeDateButtons.spacing = 1;
        compositeDateButtons.setLayout(rl_compositeDateButtons);

        btnPrev = new Button(compositeDateButtons, SWT.NONE);
        btnPrev.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_PREV_NEXT, -1);
            }
        });
        btnPrev.setEnabled(false);
        btnPrev.setText("&Prev");

        btnNext = new Button(compositeDateButtons, SWT.NONE);
        btnNext.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_PREV_NEXT, 1);
            }
        });
        btnNext.setEnabled(false);
        btnNext.setText("&Next");

        btnReduce = new Button(compositeDateButtons, SWT.NONE);
        btnReduce.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_EXTEND_REDUCE, -1);
            }
        });
        btnReduce.setEnabled(false);
        btnReduce.setText("Re&duce");

        btnExtend = new Button(compositeDateButtons, SWT.NONE);
        btnExtend.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_EXTEND_REDUCE, 1);
            }
        });
        btnExtend.setEnabled(false);
        btnExtend.setText("&Extend");

        Label lblSep1 = new Label(compositeDateButtons, SWT.SEPARATOR | SWT.VERTICAL);
        RowData layoutDataSep1 = new RowData();
        layoutDataSep1.height = btnPrev.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        lblSep1.setLayoutData(layoutDataSep1);

        btnWeekly = new Button(compositeDateButtons, SWT.NONE);
        btnWeekly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_WEEKLY, null);
            }
        });
        btnWeekly.setEnabled(false);
        btnWeekly.setText("&Weekly");

        btnMonthly = new Button(compositeDateButtons, SWT.NONE);
        btnMonthly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_MONTHLY, null);
            }
        });
        btnMonthly.setEnabled(false);
        btnMonthly.setText("&Monthly");

        btnQuarterly = new Button(compositeDateButtons, SWT.NONE);
        btnQuarterly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_QUARTERLY, null);
            }
        });
        btnQuarterly.setEnabled(false);
        btnQuarterly.setText("&Quarterly");

        btnIssue = new Button(compositeDateButtons, SWT.NONE);
        btnIssue.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_ISSUE, null);
            }
        });
        btnIssue.setEnabled(false);
        btnIssue.setText("&Issue");

        btnNone = new Button(compositeDateButtons, SWT.NONE);
        btnNone.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                processDate(OPER_NONE, null);
            }
        });
        btnNone.setEnabled(false);
        btnNone.setText("&None");

        Label lblSep2 = new Label(compositeDateButtons, SWT.SEPARATOR | SWT.VERTICAL);
        RowData layoutDataSep2 = new RowData();
        layoutDataSep2.height = btnPrev.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        lblSep2.setLayoutData(layoutDataSep2);

        Button btnResetAll = new Button(compositeDateButtons, SWT.NONE);
        btnResetAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Arrays.stream(table.getItems()).forEach(i -> {
                    Item item = (Item) i.getData();
                    item.type = item.oritype;
                    item.dest = item.oridest;
                    i.setText(COL_FREQ, item.type != null ? item.type.toString() : "");
                    i.setText(COL_DEST, item.dest != null ? item.dest : "");
                });
                tblclmnFreq.pack();
                tblclmnTo.pack();
            }
        });
        btnResetAll.setText("Rese&t All");

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

    private void init(List<Item> items) {
        table.setRedraw(true);

        items.forEach(item -> {
            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setData(item);
            ti.setChecked(item.dest != null);
            ti.setText(COL_GROUP, item.mc != null ? item.mc.getName() : "");
            ti.setText(COL_FREQ, item.type != null ? item.type.toString() : "");
            ti.setText(COL_SRC, item.src);
            ti.setText(COL_DEST, item.dest != null ? item.dest : "");
        });

        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            // don't pack last
            table.getColumn(i).pack();
        }

        table.setRedraw(true);
    }

    private void onTableSelected(SelectionEvent e) {
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        btnReduce.setEnabled(false);
        btnExtend.setEnabled(false);
        btnWeekly.setEnabled(false);
        btnMonthly.setEnabled(false);
        btnQuarterly.setEnabled(false);
        btnIssue.setEnabled(false);
        btnNone.setEnabled(false);

        if (table.getSelection().length > 0) {
            btnWeekly.setEnabled(true);
            btnMonthly.setEnabled(true);
            btnQuarterly.setEnabled(true);
            btnIssue.setEnabled(true);
            btnNone.setEnabled(true);
        }
        if (Arrays.stream(table.getSelection()).anyMatch(i -> ((Item) i.getData()).type != null)) {
            btnPrev.setEnabled(true);
            btnNext.setEnabled(true);
            btnReduce.setEnabled(true);
            btnExtend.setEnabled(true);
        }
    };

    private static String padzero(final int i) {
        return (i < 10 ? "0" : "") + i;
    }

    private static int[] normalizeMonth(final int year, final int month) {
        if (month <= 0) {
            return new int[] { year - 1, 12 };
        }
        else if (month > 12) {
            return new int[] { year + 1, 1 };
        }
        else {
            return new int[] { year, month };
        }
    }

    private static int[] normalizeQuarter(final int year, final int quarter) {
        if (quarter <= 0) {
            return new int[] { year - 1, 4 };
        }
        else if (quarter > 4) {
            return new int[] { year + 1, 1 };
        }
        else {
            return new int[] { year, quarter };
        }
    }

    private static String calendarToYYYYMMDD(Calendar c) {
        return c.get(Calendar.YEAR) + padzero(c.get(Calendar.MONTH) + 1) + padzero(c.get(Calendar.DAY_OF_MONTH));
    }

    private void processDate(int action, Integer offset) {
        table.setRedraw(false);
        for (TableItem ti : table.getSelection()) {
            Item item = (Item) ti.getData();
            if (action == OPER_NONE) {
                item.type = null;
                item.dest = Utils.makeCleanFilename(item.src);
            }
            else if (OPER_TO_FILEITEM_TYPE.get(action) != null) {
                processChangeType(item, OPER_TO_FILEITEM_TYPE.get(action));
            }
            else if (item.type == null) {
                continue;
            }
            else if (FileItem.Type.Weekly.equals(item.type) || FileItem.Type.Biweekly.equals(item.type)) {
                processWeekly(ti, action, offset);
            }
            else if (FileItem.Type.Monthly.equals(item.type)) {
                processMonthly(ti, action, offset);
            }
            else if (FileItem.Type.Quarterly.equals(item.type)) {
                processQuarterly(ti, action, offset);
            }
            ti.setText(COL_FREQ, item.type != null ? item.type.toString() : "");
            ti.setText(COL_DEST, item.dest);
        }
        tblclmnFreq.pack();
        tblclmnTo.pack();
        table.setRedraw(true);

    }

    private void processChangeType(Item item, FileItem.Type type) {
        FileItem fi = fc.guessFilename(item.src, type);
        if (fi != null) {
            item.type = type;
            item.dest = fi.getFilename();
        }
    }

    private void processWeekly(TableItem ti, int action, Integer offset) {
        final Pattern patternWeekly = Pattern
                .compile("(.*\\s)(\\d{4})(\\d{2})(\\d{2})(\\-(\\d{4})(\\d{2})(\\d{2}))?(\\.[^\\.]+)$");
        Item it = (Item) ti.getData();
        Matcher m = patternWeekly.matcher(it.dest);
        if (m.matches()) {
            final String prefix = m.group(1);
            int y1 = Integer.parseInt(m.group(2));
            int m1 = Integer.parseInt(m.group(3));
            int d1 = Integer.parseInt(m.group(4));
            int y2 = m.group(6) != null ? Integer.parseInt(m.group(6)) : -1;
            int m2 = m.group(7) != null ? Integer.parseInt(m.group(7)) : -1;
            int d2 = m.group(8) != null ? Integer.parseInt(m.group(8)) : -1;
            final String ext = m.group(9);

            if (action == OPER_PREV_NEXT) {
                Calendar c1 = GregorianCalendar.getInstance();
                c1.set(Calendar.YEAR, y1);
                c1.set(Calendar.MONTH, m1 - 1);
                c1.set(Calendar.DAY_OF_MONTH, d1);
                c1.add(Calendar.DAY_OF_MONTH, offset * 7);

                Calendar c2 = null;
                if (y2 > 0) {
                    c2 = GregorianCalendar.getInstance();
                    c2.set(Calendar.YEAR, y2);
                    c2.set(Calendar.MONTH, m2 - 1);
                    c2.set(Calendar.DAY_OF_MONTH, d2);
                    c2.add(Calendar.DAY_OF_MONTH, offset * 7);
                }

                it.dest = prefix + calendarToYYYYMMDD(c1) + (c2 != null ? ("-" + calendarToYYYYMMDD(c2)) : "") + ext;
            }
            else if (action == OPER_EXTEND_REDUCE) {
                if (y2 <= 0) {
                    if (offset < 0) {
                        // no y2,m2,d2, will not reduce
                        return;
                    }
                    y2 = y1;
                    m2 = m1;
                    d2 = d1;
                }
                Calendar c1 = GregorianCalendar.getInstance();
                c1.set(Calendar.YEAR, y1);
                c1.set(Calendar.MONTH, m1 - 1);
                c1.set(Calendar.DAY_OF_MONTH, d1);
                Calendar c2 = GregorianCalendar.getInstance();
                c2.set(Calendar.YEAR, y2);
                c2.set(Calendar.MONTH, m2 - 1);
                c2.set(Calendar.DAY_OF_MONTH, d2);
                c2.add(Calendar.DAY_OF_MONTH, offset * 7);

                if (c2.compareTo(c1) > 0) {
                    it.dest = prefix + calendarToYYYYMMDD(c1) + "-" + calendarToYYYYMMDD(c2) + ext;
                }
                else {
                    it.dest = prefix + calendarToYYYYMMDD(c1) + ext;
                }
            }
        }
    }

    private void processMonthly(TableItem ti, int action, Integer offset) {
        final Pattern patternMonthly = Pattern.compile("(.*\\s)(\\d{4})(\\d{2})(\\-(\\d{2}))?(\\.[^\\.]+)$");
        Item it = (Item) ti.getData();
        Matcher m = patternMonthly.matcher(it.dest);
        if (m.matches()) {
            final String prefix = m.group(1);
            int y1 = Integer.parseInt(m.group(2));
            int m1 = Integer.parseInt(m.group(3));
            int m2 = m.group(5) != null ? Integer.parseInt(m.group(5)) : -1;
            final String ext = m.group(6);

            if (action == OPER_PREV_NEXT) {
                int[] ym = normalizeMonth(y1, m1 + offset);
                m2 = m2 > 0 ? normalizeMonth(y1, m2 + offset)[1] : m2;
                it.dest = prefix + ym[0] + padzero(ym[1]) + (m2 > 0 ? "-" + padzero(m2) : "") + ext;
            }
            else if (action == OPER_EXTEND_REDUCE) {
                // if no m2, only extend but no reduce, otherwise 201910 will become 201910-09
                m2 = normalizeMonth(y1, m2 > 0 ? m2 + offset : (offset > 0 ? (m1 + offset) : m1))[1];
                if (m2 != m1) {
                    it.dest = prefix + y1 + padzero(m1) + "-" + padzero(m2) + ext;
                }
                else {
                    it.dest = prefix + y1 + padzero(m1) + ext;
                }
            }
        }
    }

    private void processQuarterly(TableItem ti, int action, Integer offset) {
        final Pattern patternQuarterly = Pattern.compile("(.*\\s)(\\d{4})Q([1-4])(\\-([1-4]))?(\\.[^\\.]+)$");
        Item it = (Item) ti.getData();
        Matcher m = patternQuarterly.matcher(it.dest);
        if (m.matches()) {
            final String prefix = m.group(1);
            int y1 = Integer.parseInt(m.group(2));
            int q1 = Integer.parseInt(m.group(3));
            int q2 = m.group(5) != null ? Integer.parseInt(m.group(5)) : -1;
            final String ext = m.group(6);

            if (action == OPER_PREV_NEXT) {
                int[] yq = normalizeQuarter(y1, q1 + offset);
                q2 = q2 > 0 ? normalizeQuarter(y1, q2 + offset)[1] : q2;
                it.dest = prefix + yq[0] + "Q" + yq[1] + (q2 > 0 ? "-" + q2 : "") + ext;
            }
            else if (action == OPER_EXTEND_REDUCE) {
                // if no q2, only extend but no reduce, otherwise 2019Q4 will become 2019Q4-3
                q2 = normalizeQuarter(y1, q2 > 0 ? q2 + offset : (offset > 0 ? (q1 + offset) : q1))[1];
                if (q2 != q1) {
                    it.dest = prefix + y1 + "Q" + q1 + "-" + normalizeQuarter(y1, q2)[1] + ext;
                }
                else {
                    it.dest = prefix + y1 + "Q" + q1 + ext;
                }
            }
        }
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
