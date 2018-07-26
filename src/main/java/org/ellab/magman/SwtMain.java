package org.ellab.magman;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.ellab.magman.FileCollections.MagazineCollection;
import org.ellab.magman.FileItem.Problem;

public class SwtMain {
    private Display display;
    private Shell shell;
    private Text txtDirectory;
    private Spinner spinYear;
    private Button btnOk;
    private Button btnInvalid;
    private Button btnUnknown;
    private Button btnMissingDate;
    private Button btnMissingIssue;
    private Text txtFilter;
    private Composite compositeTree;
    private Tree tree;
    private Button btnBrowse;
    private Button btnUpDirectory;
    private Button btnGoDirectory;
    private ProgressBar progressBar;

    private Composite compositeName;
    private ToolBar toolBarName;
    private ToolItem btnNameA;
    private ToolItem btnNameB;
    private ToolItem btnNameC;
    private ToolItem btnNameD;
    private ToolItem btnNameE;
    private ToolItem btnNameF;
    private ToolItem btnNameG;
    private ToolItem btnNameH;
    private ToolItem btnNameI;
    private ToolItem btnNameJ;
    private ToolItem btnNameK;
    private ToolItem btnNameL;
    private ToolItem btnNameM;
    private ToolItem btnNameN;
    private ToolItem btnNameO;
    private ToolItem btnNameP;
    private ToolItem btnNameQ;
    private ToolItem btnNameR;
    private ToolItem btnNameS;
    private ToolItem btnNameT;
    private ToolItem btnNameU;
    private ToolItem btnNameV;
    private ToolItem btnNameW;
    private ToolItem btnNameX;
    private ToolItem btnNameY;
    private ToolItem btnNameZ;
    private ToolItem btnNameNumber;
    private ToolItem btnNameChinese;
    private Combo comboName;

    private Text txtMessage;
    private Composite compositeStatusbar;
    private Label lblVersion;
    private Link linkWebsite;

    private FileCollections fc;
    int currProgressDirIndex;
    int currProgressCurrDirFileIndex;
    int currProgressCurrDirFileTotal;

    public static void main(String[] args) {
        SwtMain main = new SwtMain(args);
        main.show();
    }

    public SwtMain(String[] args) {
        init();
        addEventHandler();
        addDropTarget();

        if (args.length > 0) {
            txtDirectory.setText(args[0]);
        }
    }

    private void init() {
        display = new Display();
        shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setImage(new Image(display, SwtMain.class.getResourceAsStream("/magman.ico")));
        shell.setSize(750, 600);
        shell.setText("MagMan");
        shell.setLayout(new GridLayout(5, false));

        Label lblDirectory = new Label(shell, SWT.NONE);
        lblDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDirectory.setText("&Directory");

        txtDirectory = new Text(shell, SWT.BORDER);
        txtDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnGoDirectory = new Button(shell, SWT.NONE);
        btnGoDirectory.setEnabled(false);
        btnGoDirectory.setText("&Go");

        btnUpDirectory = new Button(shell, SWT.NONE);
        btnUpDirectory.setEnabled(false);
        btnUpDirectory.setText("Up");

        btnBrowse = new Button(shell, SWT.NONE);
        btnBrowse.setText("&...");

        progressBar = new ProgressBar(shell, SWT.NONE);
        progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));

        Composite compositeFilter = new Composite(shell, SWT.NONE);
        compositeFilter.setLayout(new GridLayout(9, false));
        compositeFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));

        Label lblYear = new Label(compositeFilter, SWT.NONE);
        lblYear.setBounds(0, 0, 59, 14);
        lblYear.setText("&Year");

        spinYear = new Spinner(compositeFilter, SWT.BORDER);
        spinYear.setMinimum(1);
        spinYear.setMaximum(100);
        spinYear.setSelection(3);

        btnOk = new Button(compositeFilter, SWT.CHECK);
        btnOk.setText("Ok");

        btnInvalid = new Button(compositeFilter, SWT.CHECK);
        btnInvalid.setSelection(true);
        btnInvalid.setText("Invalid");

        btnUnknown = new Button(compositeFilter, SWT.CHECK);
        btnUnknown.setSelection(true);
        btnUnknown.setText("Unknown");

        btnMissingDate = new Button(compositeFilter, SWT.CHECK);
        btnMissingDate.setSelection(true);
        btnMissingDate.setText("Missing (Date)");

        btnMissingIssue = new Button(compositeFilter, SWT.CHECK);
        btnMissingIssue.setSelection(true);
        btnMissingIssue.setText("Missing (Issue)");

        Label lboFilter = new Label(compositeFilter, SWT.NONE);
        lboFilter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lboFilter.setText("&Filter");

        txtFilter = new Text(compositeFilter, SWT.BORDER);
        txtFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        compositeName = new Composite(shell, SWT.NONE);
        compositeName.setLayout(new GridLayout(2, false));
        compositeName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));

        toolBarName = new ToolBar(compositeName, SWT.NONE);
        toolBarName.setBounds(-16, 35, 80, 19);

        btnNameNumber = new ToolItem(toolBarName, SWT.NONE);
        btnNameNumber.setText("#");
        btnNameA = new ToolItem(toolBarName, SWT.NONE);
        btnNameA.setText("A");
        btnNameB = new ToolItem(toolBarName, SWT.NONE);
        btnNameB.setText("B");
        btnNameC = new ToolItem(toolBarName, SWT.NONE);
        btnNameC.setText("C");
        btnNameD = new ToolItem(toolBarName, SWT.NONE);
        btnNameD.setText("D");
        btnNameE = new ToolItem(toolBarName, SWT.NONE);
        btnNameE.setText("E");
        btnNameF = new ToolItem(toolBarName, SWT.NONE);
        btnNameF.setText("F");
        btnNameG = new ToolItem(toolBarName, SWT.NONE);
        btnNameG.setText("G");
        btnNameH = new ToolItem(toolBarName, SWT.NONE);
        btnNameH.setText("H");
        btnNameI = new ToolItem(toolBarName, SWT.NONE);
        btnNameI.setText("I");
        btnNameJ = new ToolItem(toolBarName, SWT.NONE);
        btnNameJ.setText("J");
        btnNameK = new ToolItem(toolBarName, SWT.NONE);
        btnNameK.setText("K");
        btnNameL = new ToolItem(toolBarName, SWT.NONE);
        btnNameL.setText("L");
        btnNameM = new ToolItem(toolBarName, SWT.NONE);
        btnNameM.setText("M");
        btnNameN = new ToolItem(toolBarName, SWT.NONE);
        btnNameN.setText("N");
        btnNameO = new ToolItem(toolBarName, SWT.NONE);
        btnNameO.setText("O");
        btnNameP = new ToolItem(toolBarName, SWT.NONE);
        btnNameP.setText("P");
        btnNameQ = new ToolItem(toolBarName, SWT.NONE);
        btnNameQ.setText("Q");
        btnNameR = new ToolItem(toolBarName, SWT.NONE);
        btnNameR.setText("R");
        btnNameS = new ToolItem(toolBarName, SWT.NONE);
        btnNameS.setText("S");
        btnNameT = new ToolItem(toolBarName, SWT.NONE);
        btnNameT.setText("T");
        btnNameU = new ToolItem(toolBarName, SWT.NONE);
        btnNameU.setText("U");
        btnNameV = new ToolItem(toolBarName, SWT.NONE);
        btnNameV.setText("V");
        btnNameW = new ToolItem(toolBarName, SWT.NONE);
        btnNameW.setText("W");
        btnNameX = new ToolItem(toolBarName, SWT.NONE);
        btnNameX.setText("X");
        btnNameY = new ToolItem(toolBarName, SWT.NONE);
        btnNameY.setText("Y");
        btnNameZ = new ToolItem(toolBarName, SWT.NONE);
        btnNameZ.setText("Z");
        btnNameChinese = new ToolItem(toolBarName, SWT.NONE);
        btnNameChinese.setText("\u4E2D");
        btnNameChinese.setData("btnNameChinese");

        comboName = new Combo(compositeName, SWT.READ_ONLY);
        comboName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        compositeTree = new Composite(shell, SWT.NONE);
        compositeTree.setLayout(new FillLayout(SWT.HORIZONTAL));
        compositeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));

        tree = new Tree(compositeTree, SWT.BORDER | SWT.FULL_SELECTION);
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);

        TreeColumn trclmnPath = new TreeColumn(tree, SWT.NONE);
        trclmnPath.setWidth(100);
        trclmnPath.setText("Path");

        TreeColumn trclmnGroup = new TreeColumn(tree, SWT.NONE);
        trclmnGroup.setWidth(100);
        trclmnGroup.setText("Group");

        TreeColumn trclmnDate = new TreeColumn(tree, SWT.NONE);
        trclmnDate.setWidth(100);
        trclmnDate.setText("Date");

        TreeColumn trclmnStatus = new TreeColumn(tree, SWT.NONE);
        trclmnStatus.setWidth(100);
        trclmnStatus.setText("Status");

        txtMessage = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        txtMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
        GridData gd_txtMessage = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
        gd_txtMessage.heightHint = 100;
        txtMessage.setLayoutData(gd_txtMessage);

        compositeStatusbar = new Composite(shell, SWT.NONE);
        GridLayout gl_compositeStatusbar = new GridLayout(2, false);
        gl_compositeStatusbar.marginHeight = 0;
        gl_compositeStatusbar.marginWidth = 0;
        compositeStatusbar.setLayout(gl_compositeStatusbar);
        compositeStatusbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));

        lblVersion = new Label(compositeStatusbar, SWT.NONE);
        lblVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblVersion.setText("Java " + System.getProperty("java.version") + ", " + System.getProperty("os.name")
                + ", SWT " + SWT.getVersion());

        linkWebsite = new Link(compositeStatusbar, SWT.NONE);
        linkWebsite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
        linkWebsite.setText("<a>@GitHub</a>");
    }

    public void show() {
        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = shell.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        shell.setLocation(x, y);

        shell.open();
        // shell.pack();

        changeDirectory();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        try {
            display.dispose();
        }
        catch (SWTException ex) {
            ;
        }
    }

    private void addEventHandler() {
        btnBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dd = new DirectoryDialog(shell);
                dd.setText("Open");
                String dir = txtDirectory.getText();
                if (dir == null || dir.trim().length() == 0) {
                    dir = File.listRoots()[0].getAbsolutePath();
                }
                dd.setFilterPath(dir);
                String selected = dd.open();
                if (selected != null) {
                    txtDirectory.setText(selected);
                }
            }
        });

        txtDirectory.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                btnGoDirectory.setEnabled(txtDirectory.getText().length() > 0);
                btnUpDirectory.setEnabled(txtDirectory.getText().length() > 0);
            }
        });

        txtDirectory.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ((Text) e.getSource()).selectAll();
            }
        });

        txtDirectory.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    changeDirectory();
                }
            }
        });

        btnGoDirectory.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changeDirectory();
            }
        });

        btnUpDirectory.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    File file = new File(txtDirectory.getText());
                    txtDirectory.setText(file.getParentFile().getCanonicalPath());
                    changeDirectory();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        spinYear.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                populateTree();
            }
        });

        Button[] filterButtons = { btnOk, btnInvalid, btnUnknown, btnMissingDate, btnMissingIssue };
        Arrays.stream(filterButtons).forEach(b -> {
            b.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    populateTree();
                }
            });
        });

        txtFilter.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                populateTree();
            }
        });

        txtFilter.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ((Text) e.getSource()).selectAll();
            }
        });

        ToolItem[] nameButtons = { btnNameA, btnNameB, btnNameC, btnNameD, btnNameE, btnNameF, btnNameG, btnNameH,
                btnNameI, btnNameJ, btnNameK, btnNameL, btnNameM, btnNameN, btnNameO, btnNameP, btnNameQ, btnNameR,
                btnNameS, btnNameT, btnNameU, btnNameV, btnNameW, btnNameX, btnNameY, btnNameZ, btnNameNumber,
                btnNameChinese };
        Arrays.stream(nameButtons).forEach(btn -> btn.addSelectionListener((new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                scrollTreeTo(((ToolItem) e.getSource()).getText());
            }
        })));

        comboName.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo combo = (Combo) e.getSource();
                scrollTreeTo(combo.getText());
            }
        });

    }

    private void addDropTarget() {
        int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
        DropTarget target = new DropTarget(shell, operations);

        final FileTransfer fileTransfer = FileTransfer.getInstance();
        Transfer[] types = new Transfer[] { fileTransfer };
        target.setTransfer(types);

        target.addDropListener(new DropTargetListener() {
            public void dragEnter(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT) {
                    if ((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    }
                    else {
                        event.detail = DND.DROP_NONE;
                    }
                }
                for (int i = 0; i < event.dataTypes.length; i++) {
                    if (fileTransfer.isSupportedType(event.dataTypes[i])) {
                        event.currentDataType = event.dataTypes[i];
                        // files should only be copied
                        if (event.detail != DND.DROP_COPY) {
                            event.detail = DND.DROP_NONE;
                        }
                        break;
                    }
                }
            }

            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
            }

            public void dragOperationChanged(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT) {
                    if ((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    }
                    else {
                        event.detail = DND.DROP_NONE;
                    }
                }
                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    if (event.detail != DND.DROP_COPY) {
                        event.detail = DND.DROP_NONE;
                    }
                }
            }

            public void dragLeave(DropTargetEvent event) {
            }

            public void dropAccept(DropTargetEvent event) {
            }

            public void drop(DropTargetEvent event) {
                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    Arrays.stream((String[]) event.data).forEach(f -> {
                        MagazineCollection mag = fc.items().stream().filter(
                                mc -> mc.getName().length() > 0 && new File(f).getName().startsWith(mc.getName()))
                                .reduce(null,
                                        (a, b) -> a == null || b.getName().length() > a.getName().length() ? b : a);

                        if (mag != null) {
                            try {
                                Path temp = Files.move(Paths.get(f),
                                        Paths.get(mag.getPath() + "/" + new File(f).getName()));

                                if (temp != null) {
                                    log("Moved " + Paths.get(f) + " to "
                                            + Paths.get(mag.getPath() + "/" + new File(f).getName()) + " successfully");
                                }
                                else {
                                    log("Failed to move the file");
                                }
                            }
                            catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        else {
                            log("Cannot find a match of " + Paths.get(f));
                        }
                    });
                }
            }
        });
    }

    private void log(String s) {
        System.out.println(s);
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                txtMessage.append(s + '\n');
            }
        });
    }

    private void showMessage(int style, String msg) {
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                MessageBox dialog = new MessageBox(shell, style | SWT.OK);
                dialog.setText(shell.getText());
                dialog.setMessage(msg);
                dialog.open();
            }
        });
    }

    private void changeDirectory() {
        if (txtDirectory.getText().length() == 0) {
            return;
        }

        Path path = Paths.get(txtDirectory.getText());
        tree.removeAll();
        comboName.removeAll();
        fc = new FileCollections();
        new Thread() {
            @Override
            public void run() {
                try {
                    if (!path.toFile().exists()) {
                        showMessage(SWT.ERROR, "The directory does not exist");
                        return;
                    }
                    processDirectory(path);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    showMessage(SWT.ERROR, ex.getMessage());
                }
            }
        }.start();
    }

    private void processDirectory(final Path path) throws Exception {
        int rootNameCount = path.getNameCount();
        final int totalDirCount = (int) Arrays.stream(path.toFile().listFiles()).filter(f -> f.isDirectory()).count();
        currProgressDirIndex = 0;
        currProgressCurrDirFileTotal = 0;
        currProgressCurrDirFileIndex = 0;

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.getNameCount() == rootNameCount + 1) {
                    ++currProgressDirIndex;
                    log(currProgressDirIndex + " " + dir);
                    currProgressCurrDirFileTotal = dir.toFile().list().length;
                    currProgressCurrDirFileIndex = 0;
                }

                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                int nameCount = file.getNameCount();

                if (nameCount == rootNameCount + 2) {
                    // Progress only take account into 1st level sub-directory
                    ++currProgressCurrDirFileIndex;
                }

                String filename = file.getFileName().toString();
                if (filename.toLowerCase().endsWith(".pdf") || filename.toLowerCase().endsWith(".mp3")) {
                    String parentFullName = "";
                    Path parent = file;
                    for (int i = nameCount; i > rootNameCount + 1; i--) {
                        parentFullName = parent.getParent().getFileName().toString()
                                + (parentFullName.length() == 0 ? "" : "/" + parentFullName);
                        parent = parent.getParent();
                    }
                    FileItem fi = new FileItem(file, parentFullName);
                    fc.add(fi);
                }

                // log.println(
                // total + "," + currProgressDir + "," + currProgressDirTotal + "," + currProgressDirFile);
                // log.println(Math.round((((currProgressDir - 1) * currProgressDirTotal + currProgressDirFile)
                // / (float) currProgressDirTotal) * 100.0f / total));

                /*-
                 * progress formula
                 * 
                 *                                 [file completed]
                 *    [directory completed] + ---------------------------
                 *                            [curr directory file count]
                 * ---------------------------------------------------------
                 *                  [total directory count]
                 */
                if (currProgressCurrDirFileTotal > 0 && totalDirCount > 0) {
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setSelection(Math.round(((currProgressDirIndex - 1)
                                    + (currProgressCurrDirFileIndex) / (float) currProgressCurrDirFileTotal) * 100.0f
                                    / totalDirCount));
                        }
                    });
                }

                return FileVisitResult.CONTINUE;
            }
        });

        fc.analysis();

        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                tree.setRedraw(false);
                populateTree();
                int totalWidth = 0;
                for (int i = 1; i < tree.getColumnCount(); i++) {
                    tree.getColumn(i).pack();
                    totalWidth += tree.getColumn(i).getWidth();
                }
                int width = tree.getClientArea().width - totalWidth;
                tree.getColumn(0).setWidth(Math.max(100, width));
                tree.setRedraw(true);

                // populate drop down
                fc.items().stream().forEach(mc -> {
                    if (mc.getName().length() > 0) {
                        comboName.add(mc.getName());
                    }
                });
            }
        });
    }

    private boolean matchFilter(String text, String filterLower) {
        return (filterLower == null || filterLower.trim().length() == 0
                || text.toLowerCase().indexOf(filterLower) >= 0);
    }

    private void populateTree() {
        final String filter = txtFilter.getText().toLowerCase();
        int year = spinYear.getSelection();
        final LocalDate from = LocalDate.now().minusYears(year).withDayOfYear(1);

        final boolean ok = btnOk.getSelection();
        final boolean missingDate = btnMissingDate.getSelection();
        final boolean missingIssue = btnMissingIssue.getSelection();
        final boolean invalid = btnInvalid.getSelection();
        final boolean unknown = btnUnknown.getSelection();

        tree.removeAll();
        tree.setRedraw(false);
        for (MagazineCollection mc : fc.items()) {
            TreeItem parent = new TreeItem(tree, SWT.None);
            parent.setText(mc.getName().length() > 0 ? mc.getName() : mc.getPath());
            mc.files().stream()
                    .filter(fi -> fi.isEarliestOfType() || fi.isLatestOfType() || !fi.isDateType()
                            || fi.getDateFrom().isAfter(from))
                    .filter(fi -> fi.isEarliestOfType() || fi.isLatestOfType() || matchFilter(fi.getFilename(), filter))
                    .filter(fi -> fi.isEarliestOfType() || fi.isLatestOfType() || ok || !fi.isOk())
                    .filter(fi -> fi.isEarliestOfType() || fi.isLatestOfType() || missingDate || !fi.isMissing()
                            || !fi.isDateType())
                    .filter(fi -> fi.isEarliestOfType() || fi.isLatestOfType() || missingIssue || !fi.isMissing()
                            || fi.isDateType())
                    .filter(fi -> fi.isEarliestOfType() || fi.isLatestOfType() || invalid || fi.isAlien())
                    .filter(fi -> fi.isEarliestOfType() || fi.isLatestOfType() || unknown || !fi.isValid())
                    .forEach(fi -> {
                        TreeItem t = new TreeItem(parent, 0);
                        t.setText(fi.getFilename());
                        if (fi.isValid() || fi.isMissing()) {
                            t.setText(1, fi.getGroup() == null ? "" : fi.getGroup());
                            t.setText(2, fi.getDateStr() == null ? "" : fi.getDateStr());
                            if (fi.getProblems().size() > 0) {
                                // valid but format problem
                                t.setBackground(new Color(display, new RGB(255, 255, 128)));
                            }
                            else if (fi.isMissing()) {
                                t.setForeground(new Color(display, new RGB(255, 0, 0)));
                                t.setText(3, "Missing");
                            }
                        }
                        else if (fi.isAlien()) {
                            t.setBackground(new Color(display, new RGB(255, 127, 127)));
                            t.setText(3, "Unknown");
                        }
                        else {
                            t.setBackground(new Color(display, new RGB(255, 192, 127)));
                        }

                        if (fi.getProblems().size() > 0) {
                            String pstr = (t.getText(3) + "").trim();
                            for (Problem p : fi.getProblems()) {
                                pstr += " " + p.toString();
                            }
                            pstr = pstr.trim();
                            t.setText(3, pstr);
                        }
                    });

            if (parent.getItemCount() == 0) {
                parent.dispose();
            }
        }

        for (TreeItem item : tree.getItems()) {
            item.setExpanded(true);
        }
        tree.setRedraw(true);
    }

    private void scrollTreeTo(String str) {
        // If more than 1 match, select the next after selection
        TreeItem selection = tree.getSelection().length > 0 ? tree.getSelection()[0] : null;

        List<TreeItem> list = Arrays.stream(tree.getItems()).filter(i -> {
            return i != selection
                    && ("#".equals(str) ? Character.isDigit(i.getText(0).charAt(0)) : i.getText(0).startsWith(str));
        }).sorted(new Comparator<TreeItem>() {
            @Override
            public int compare(TreeItem a, TreeItem b) {
                return a.getText(0).compareTo(b.getText(0));
            };
        }).collect(Collectors.toList());

        TreeItem item = null;
        int size = list.size();

        if (size == 0) {
            item = selection;
        }
        else if (size == 1 || selection == null) {
            item = list.get(0);
        }
        else {
            for (TreeItem i : list) {
                if (i.getText(0).compareTo(selection.getText(0)) > 0) {
                    item = i;
                    break;
                }
            }
            if (item == null) {
                item = list.get(0);
            }
        }

        if (item != null) {
            tree.setTopItem(item);
            tree.setSelection(item);
        }
    }
}
