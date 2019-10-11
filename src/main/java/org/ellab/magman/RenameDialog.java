package org.ellab.magman;

import java.util.List;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class RenameDialog extends Dialog {

    protected Object result;
    protected Shell shlMagmanRename;
    private Table table;

    public RenameDialog(Shell parent, int style) {
        super(parent, style);
    }

    public Object open(List<String[]> files) {
        createContents();
        init(files);
        shlMagmanRename.open();
        shlMagmanRename.layout();
        Display display = getParent().getDisplay();
        while (!shlMagmanRename.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    private void createContents() {
        shlMagmanRename = new Shell(getParent(), getStyle());
        shlMagmanRename.setSize(450, 300);
        shlMagmanRename.setText("Magman - Rename");
        shlMagmanRename.setLayout(new GridLayout(1, false));

        table = new Table(shlMagmanRename, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
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

        Composite composite = new Composite(shlMagmanRename, SWT.NONE);
        FillLayout fl_composite = new FillLayout(SWT.HORIZONTAL);
        fl_composite.spacing = 30;
        composite.setLayout(fl_composite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

        Button btnRename = new Button(composite, SWT.NONE);
        btnRename.setText("Rename");

        Button btnCancel = new Button(composite, SWT.NONE);
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shlMagmanRename.dispose();
            }
        });
        btnCancel.setText("Cancel");
    }

    private void init(List<String[]> files) {
        files.forEach(f -> {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setChecked(f[2] != null);
            item.setText(0, f[0] != null ? f[0] : "");
            item.setText(1, f[1]);
            item.setText(2, f[2] != null ? f[2] : "");
        });
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
    }
}
