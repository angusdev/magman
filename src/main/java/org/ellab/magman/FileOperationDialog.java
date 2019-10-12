package org.ellab.magman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
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
        private String src;
        private String dest;

        public Item(String path, String src, MagazineCollection mc, String dest) {
            super();
            this.path = path;
            this.mc = mc;
            this.src = src;
            this.dest = dest;
        }
    }

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

        table = new Table(shell, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tblclmnGroup = new TableColumn(table, SWT.NONE);
        tblclmnGroup.setWidth(100);
        tblclmnGroup.setText("Group");

        TableColumn tblclmnFrom = new TableColumn(table, SWT.NONE);
        tblclmnFrom.setWidth(100);
        tblclmnFrom.setText("From");

        TableColumn tblclmnTo = new TableColumn(table, SWT.NONE);
        tblclmnTo.setWidth(100);
        tblclmnTo.setText("To");

        TableColumn tblclmnStatus = new TableColumn(table, SWT.NONE);
        tblclmnStatus.setWidth(100);
        tblclmnStatus.setText("Status");

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
        btnRename.setText("Rename");

        Button btnCancel = new Button(composite, SWT.NONE);
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.dispose();
            }
        });
        btnCancel.setText("Cancel");
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
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
    }

    private void renameFiles() {
        for (int i = 0; i < table.getItemCount(); i++) {
            TableItem ti = table.getItem(i);
            if (ti.getChecked()) {
                Item item = (Item) ti.getData();
                System.out.println(item.path);
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
