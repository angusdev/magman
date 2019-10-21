package org.ellab.magman;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SwtUtils {
    public static void messageBox(Shell shell, int style, String message) {
        MessageBox dialog = new MessageBox(shell, style);
        dialog.setText(shell.getText());
        dialog.setMessage(message);
        dialog.open();
    }

    public static void errorBox(Shell shell, Exception ex) {
        MessageBox dialog = new MessageBox(shell, SWT.ERROR | SWT.OK);
        dialog.setText(shell.getText());
        dialog.setMessage(ex.getMessage());
        dialog.open();
    }
}
