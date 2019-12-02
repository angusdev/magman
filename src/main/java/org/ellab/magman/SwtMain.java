package org.ellab.magman;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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

    private CLabel lblStatTotal;
    private CLabel lblStatD7;
    private CLabel lblStatD30;
    private CLabel lblStatY0;
    private CLabel lblStatY1;
    private CLabel lblStatY2;

    private Composite compositeStatusbar;
    private Label lblVersion;
    private Link linkWebsite;

    private Map<String, ToolItem> nameButtons = new HashMap<>();

    private FileCollections fc;
    int currProgressDirIndex;
    int currProgressCurrDirFileIndex;
    int currProgressCurrDirFileTotal;
    private Composite compositeDrop;
    private CLabel lblDropRename;
    private DropTarget dropRename;
    private Label lblIncludeDir;
    private Text txtIncludeDir;
    private Composite compositeStat;
    private Button btnSummary;

    public FileCollections getFileCollections() {
        return fc;
    }

    public static void main(String[] args) {
        SwtMain main = new SwtMain(args);
        main.show();
    }

    public SwtMain(String[] args) {
        init();
        addEventHandler();
        addDropTarget();
        addDropRename();

        if (args.length > 0) {
            txtDirectory.setText(args[0]);
        }

        if (args.length > 1) {
            txtIncludeDir.setText(args[1]);
        }
    }

    private void init() {
        display = new Display();
        shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setImage(new Image(display, SwtMain.class.getResourceAsStream("/magman.ico")));
        shell.setSize(950, 600);
        shell.setText("MagMan");
        shell.setLayout(new GridLayout(5, false));

        Label lblDirectory = new Label(shell, SWT.NONE);
        lblDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDirectory.setText("&Directory");

        txtDirectory = new Text(shell, SWT.BORDER);
        txtDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnGoDirectory = new Button(shell, SWT.NONE);
        btnGoDirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
        btnGoDirectory.setEnabled(false);
        btnGoDirectory.setText("&Go");

        btnUpDirectory = new Button(shell, SWT.NONE);
        btnUpDirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
        btnUpDirectory.setEnabled(false);
        btnUpDirectory.setText("Up");

        btnBrowse = new Button(shell, SWT.NONE);
        btnBrowse.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
        btnBrowse.setText("&...");

        lblIncludeDir = new Label(shell, SWT.NONE);
        lblIncludeDir.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblIncludeDir.setText("&Includes");

        txtIncludeDir = new Text(shell, SWT.BORDER);
        txtIncludeDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

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

        Label lblFilter = new Label(compositeFilter, SWT.NONE);
        lblFilter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblFilter.setText("&Filter");

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
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                if (tree.getSelectionCount() > 0) {
                    TreeItem ti = tree.getSelection()[0];
                    if (ti.getData() instanceof FileItem) {
                        FileItem fi = (FileItem) ti.getData();
                        if (fi.isDummy()) {
                            MagazineCollection mc = (MagazineCollection) ti.getParentItem().getData();
                            try {
                                refreshMagazine(new MagazineCollection[] { mc });
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        tree.addMenuDetectListener(new MenuDetectListener() {
            public void menuDetected(MenuDetectEvent e) {
                if (tree.getSelectionCount() <= 0) {
                    e.doit = false;
                }
            }
        });
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

        Menu menu = new Menu(tree);
        tree.setMenu(menu);

        MenuItem mntmOpenPath = new MenuItem(menu, SWT.NONE);
        mntmOpenPath.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selection = tree.getSelection();
                if (selection.length > 0) {
                    TreeItem ti = selection[0];
                    while (ti.getParentItem() != null) {
                        ti = ti.getParentItem();
                    }
                    Object data = ti.getData();
                    if (data instanceof MagazineCollection) {
                        MagazineCollection mc = (MagazineCollection) data;
                        Program.launch(mc.getPath());
                    }
                }
            }
        });
        mntmOpenPath.setText("&Open File Location");

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem mntmExport = new MenuItem(menu, SWT.NONE);
        mntmExport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(shell, SWT.SAVE);
                fd.setText("Export to...");

                String dir = txtDirectory.getText();
                if (dir == null || dir.trim().length() == 0) {
                    dir = File.listRoots()[0].getAbsolutePath();
                }
                fd.setFilterPath(dir);
                fd.setFileName("magman.csv");
                fd.setFilterExtensions(new String[] { "*.csv" });

                String selected = fd.open();
                if (selected != null) {
                    try {
                        if (!new File(selected).exists()
                                || SwtUtils.messageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO,
                                        "File already exist, confirm to overwrite?") == SWT.YES) {
                            exportToCsv(selected);
                            SwtUtils.messageBox(shell, SWT.ICON_INFORMATION, "File exported");
                        }
                    }
                    catch (Exception ex) {
                        SwtUtils.errorBox(shell, ex);
                    }
                }
            }
        });
        mntmExport.setText("&Export");

        txtMessage = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        txtMessage.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        GridData gd_txtMessage = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
        gd_txtMessage.heightHint = 100;
        txtMessage.setLayoutData(gd_txtMessage);

        compositeDrop = new Composite(shell, SWT.NONE);
        compositeDrop.setLayout(new GridLayout(1, false));
        compositeDrop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

        lblDropRename = new CLabel(compositeDrop, SWT.BORDER);
        lblDropRename.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        lblDropRename.setAlignment(SWT.CENTER);
        lblDropRename.setText("Drop files\r\nhere\r\nto rename");

        dropRename = new DropTarget(lblDropRename, DND.DROP_COPY | DND.DROP_DEFAULT);
        
                btnSummary = new Button(compositeDrop, SWT.NONE);
                btnSummary.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        new SummaryDialog(shell, SWT.TITLE | SWT.RESIZE | SWT.CLOSE | SWT.MAX | SWT.MIN).open(fc);
                    }
                });
                btnSummary.setText("Summary");

        compositeStat = new Composite(shell, SWT.NONE);
        GridLayout gl_compositeStat = new GridLayout(6, false);
        gl_compositeStat.marginWidth = 0;
        compositeStat.setLayout(gl_compositeStat);
        GridData gd_compositeStat = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
        gd_compositeStat.minimumWidth = 5;
        compositeStat.setLayoutData(gd_compositeStat);

        lblStatTotal = new CLabel(compositeStat, SWT.BORDER);
        lblStatTotal.setText("Total: ");

        lblStatD7 = new CLabel(compositeStat, SWT.BORDER);
        lblStatD7.setText("7 Days: ");

        lblStatD30 = new CLabel(compositeStat, SWT.BORDER);
        lblStatD30.setText("30 Days: ");

        lblStatY0 = new CLabel(compositeStat, SWT.BORDER);
        lblStatY0.setText("          ");

        lblStatY1 = new CLabel(compositeStat, SWT.BORDER);
        lblStatY1.setText("          ");

        lblStatY2 = new CLabel(compositeStat, SWT.BORDER);
        lblStatY2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblStatY2.setText("          ");

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

        txtIncludeDir.addListener(SWT.Traverse, new Listener() {
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

        ToolItem[] nameButtonsArray = { btnNameA, btnNameB, btnNameC, btnNameD, btnNameE, btnNameF, btnNameG, btnNameH,
                btnNameI, btnNameJ, btnNameK, btnNameL, btnNameM, btnNameN, btnNameO, btnNameP, btnNameQ, btnNameR,
                btnNameS, btnNameT, btnNameU, btnNameV, btnNameW, btnNameX, btnNameY, btnNameZ,
                btnNameNumber /* , btnNameChinese */ };
        nameButtons = Arrays.stream(nameButtonsArray).collect(Collectors.toMap(ToolItem::getText, Function.identity()));
        nameButtons.put("UTF8", btnNameChinese);
        nameButtons.values().stream().forEach(b -> b.setEnabled(false));
        nameButtons.values().stream().forEach(b -> b.addSelectionListener((new SelectionAdapter() {
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

        DropTargetListener dropListener = (new BaseDropTargetHandler(fileTransfer, this) {
            @Override
            public void drop(DropTargetEvent event) {
                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            Arrays.stream((String[]) event.data).forEach(f -> {
                                MagazineCollection mag = fc.items().stream()
                                        .filter(mc -> mc.getName().length() > 0
                                                && new File(f).getName().startsWith(mc.getName()))
                                        .reduce(null, (a,
                                                b) -> a == null || b.getName().length() > a.getName().length() ? b : a);

                                if (mag != null) {
                                    try {
                                        Path temp = Files.move(Paths.get(f),
                                                Paths.get(mag.getPath() + "/" + new File(f).getName()));

                                        if (temp != null) {
                                            log("Moved " + Paths.get(f) + " to "
                                                    + Paths.get(mag.getPath() + "/" + new File(f).getName())
                                                    + " successfully");
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
                    });
                }
            }
        });
        target.addDropListener(dropListener);
    }

    private void addDropRename() {
        final FileTransfer fileTransfer = FileTransfer.getInstance();
        Transfer[] types = new Transfer[] { fileTransfer };
        dropRename.setTransfer(types);

        DropTargetListener dropListener = (new BaseDropTargetHandler(fileTransfer, this) {
            @Override
            public void drop(DropTargetEvent event) {
                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            new FileOperationDialog(shell,
                                    SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE | SWT.CLOSE | SWT.MAX | SWT.MIN)
                                            .open((String[]) event.data, fc);
                        }
                    });
                }
            }
        });

        dropRename.addDropListener(dropListener);

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

        final Path path = Paths.get(txtDirectory.getText());
        final String includeDir = txtIncludeDir.getText();

        tree.removeAll();
        comboName.removeAll();
        nameButtons.values().stream().forEach(b -> b.setEnabled(false));

        fc = new FileCollections();
        new Thread() {
            @Override
            public void run() {
                try {
                    if (!path.toFile().exists()) {
                        showMessage(SWT.ERROR, "The directory does not exist");
                        return;
                    }
                    refreshAll(path, includeDir);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    showMessage(SWT.ERROR, ex.getMessage());
                }
            }
        }.start();
    }

    private void refreshAll(final Path path, final String includePartialMatch) throws Exception {
        processDirectory(path, includePartialMatch, null);

        fc.analysis();

        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                refreshTree();
            }
        });
    }

    private void refreshMagazine(final MagazineCollection[] mcs) throws Exception {
        fc.remove(mcs);

        Arrays.stream(mcs).forEach(mc -> {
            try {
                processDirectory(Paths.get(mc.getPath()).getParent(), null, mc.getName());
                fc.analysis(mc.getName());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tree.setRedraw(false);
        refreshTree();
        scrollTreeTo(mcs[0].getName());
        tree.setRedraw(true);
    }

    private void processDirectory(final Path path, final String includePartialMatch, final String processThisPathOnly)
            throws Exception {
        final String finalIncludePartialDir = includePartialMatch != null && includePartialMatch.trim().length() > 0
                ? includePartialMatch.trim().toUpperCase()
                : null;

        int rootNameCount = path.getNameCount();
        final int totalDirCount = (int) Arrays.stream(path.toFile().listFiles()).filter(f -> f.isDirectory()
                && (finalIncludePartialDir == null || f.getName().toUpperCase().indexOf(finalIncludePartialDir) >= 0)
                && (processThisPathOnly == null || f.getName().equalsIgnoreCase(processThisPathOnly))).count();
        currProgressDirIndex = 0;
        currProgressCurrDirFileTotal = 0;
        currProgressCurrDirFileIndex = 0;

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final int nameCount = dir.getNameCount();

                if (nameCount > rootNameCount) {
                    final String suffix = dir.toString().substring(path.toString().length() + File.separator.length())
                            .toUpperCase();

                    if (processThisPathOnly != null && nameCount == rootNameCount + 1
                            && !suffix.equalsIgnoreCase(processThisPathOnly)) {
                        // if nameCount > rootNameCount + 1 means parent path already pass the checking, no need to skip
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    if (finalIncludePartialDir != null && suffix.indexOf(finalIncludePartialDir) < 0) {
                        fc.add(FileItem.createDummyItem("Skipped ...", dir, dir.getFileName().toString()));
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }

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
                final int nameCount = file.getNameCount();

                if ((finalIncludePartialDir != null || processThisPathOnly != null) && nameCount == rootNameCount + 1) {
                    // has include partial match, skip the file in root directory (rootNameCount + 1)
                    return FileVisitResult.CONTINUE;
                }

                if (nameCount > rootNameCount + 1) {
                    // Progress only take account into 1st level sub-directory
                    ++currProgressCurrDirFileIndex;
                }

                String filename = file.getFileName().toString();
                if (FileCollections.isSupportedFilename(filename)) {
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
    }

    private void refreshTree() {
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

        // populate drop down and enable buttons
        fc.items().stream().forEach(mc -> {
            if (mc.getName().length() > 0) {
                comboName.add(mc.getName());

                String prefix = mc.getName().substring(0, 1);
                if (prefix.matches("\\d")) {
                    prefix = "#";
                }
                else if (!Charset.forName("ISO-8859-1").newEncoder().canEncode(prefix)) {
                    prefix = "UTF8";
                }

                ToolItem b = nameButtons.get(prefix);
                if (b != null) {
                    b.setEnabled(true);
                }
            }
        });

        FileItemStat fs = fc.files().getStat();
        lblStatTotal.setText("Total: " + fs.getTotal().getFileCount() + " files, "
                + Utils.kmg(fc.files().getStat().getTotal().getFileSize(), true, " ", "B"));
        lblStatTotal.pack();
        lblStatD7.setText("7 Days: " + fs.get(7).getFileCount() + " files, "
                + Utils.kmg(fc.files().getStat().get(7).getFileSize(), true, " ", "B"));
        lblStatD7.pack();
        lblStatD30.setText("30 Days: " + fs.get(30).getFileCount() + " files, "
                + Utils.kmg(fc.files().getStat().get(30).getFileSize(), true, " ", "B"));
        lblStatD30.pack();
        lblStatY0.setText(fs.getCurrentYear() + ": " + fs.get(fs.getCurrentYear()).getFileCount() + " files, "
                + Utils.kmg(fc.files().getStat().get(fs.getCurrentYear()).getFileSize(), true, " ", "B"));
        lblStatY0.pack();
        lblStatY1.setText((fs.getCurrentYear() - 1) + ": " + fs.get(fs.getCurrentYear() - 1).getFileCount() + " files, "
                + Utils.kmg(fc.files().getStat().get(fs.getCurrentYear() - 1).getFileSize(), true, " ", "B"));
        lblStatY1.pack();
        lblStatY2.setText((fs.getCurrentYear() - 2) + ": " + fs.get(fs.getCurrentYear() - 2).getFileCount() + " files, "
                + Utils.kmg(fc.files().getStat().get(fs.getCurrentYear() - 2).getFileSize(), true, " ", "B"));
        lblStatY2.pack();

        lblStatTotal.getParent().layout();
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

        // need to be effectively final, so make it a size-1 array
        final TreeItem[] scrollTo = { null };

        tree.setRedraw(false);
        tree.removeAll();
        for (MagazineCollection mc : fc.items()) {
            TreeItem parent = new TreeItem(tree, SWT.None);
            parent.setData(mc);
            parent.setText(mc.getName().length() > 0 ? mc.getName() : mc.getPath());
            mc.files().getFileItems().stream()
                    .filter(fi -> fi.isDummy() || fi.isEarliestOfType() || fi.isLatestOfType() || !fi.isDateType()
                            || fi.getDateFrom().isAfter(from))
                    .filter(fi -> fi.isDummy() || fi.isEarliestOfType() || fi.isLatestOfType()
                            || matchFilter(fi.getFilename(), filter))
                    .filter(fi -> fi.isDummy() || fi.isEarliestOfType() || fi.isLatestOfType() || ok || !fi.isOk())
                    .filter(fi -> fi.isDummy() || fi.isEarliestOfType() || fi.isLatestOfType() || missingDate
                            || !fi.isMissing() || !fi.isDateType())
                    .filter(fi -> fi.isDummy() || fi.isEarliestOfType() || fi.isLatestOfType() || missingIssue
                            || !fi.isMissing() || fi.isDateType())
                    .filter(fi -> fi.isDummy() || fi.isEarliestOfType() || fi.isLatestOfType() || invalid
                            || fi.isAlien())
                    .filter(fi -> fi.isDummy() || fi.isEarliestOfType() || fi.isLatestOfType() || unknown
                            || !fi.isValid())
                    .forEach(fi -> {
                        TreeItem t = new TreeItem(parent, 0);
                        t.setData(fi);
                        if (fi.isDummy()) {
                            t.setText(fi.getFilename());
                            t.setForeground(new Color(display, new RGB(128, 128, 128)));
                        }
                        else {
                            if (scrollTo[0] == null) {
                                // scroll to first non dummy item
                                scrollTo[0] = parent;
                            }

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
                        }
                    });

            if (parent.getItemCount() == 0) {
                parent.dispose();
            }
        }

        for (TreeItem item : tree.getItems()) {
            item.setExpanded(true);
        }

        if (scrollTo[0] != null) {
            tree.setTopItem(scrollTo[0]);
            tree.setSelection(scrollTo[0]);
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

    private void exportToCsv(String file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(file));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, Charset.forName("UTF8")))) {
            // for excel
            byte[] enc = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            fos.write(enc);

            bw.write("Magazine,Path,Group,Date,Status");
            bw.newLine();

            for (TreeItem ti : tree.getItems()) {
                MagazineCollection mc = (MagazineCollection) ti.getData();
                for (TreeItem ti2 : ti.getItems()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = -1; i < tree.getColumnCount(); i++) {
                        // first column is the tree node name
                        if (i >= 0) {
                            sb.append(',');
                        }
                        String s = i < 0 ? mc.getName() : ti2.getText(i);
                        s = s.replace("\"", "\\\"");
                        if (s.contains(",")) {
                            // need to add quote "
                            sb.append('"').append(s).append('"');
                        }
                        else {
                            sb.append(s);
                        }
                    }
                    bw.write(sb.toString());
                    bw.newLine();
                }
            }
        }
    }
}
