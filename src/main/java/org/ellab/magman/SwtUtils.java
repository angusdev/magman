package org.ellab.magman;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SwtUtils {
    public static int messageBox(Shell shell, int style, String message) {
        MessageBox dialog = new MessageBox(shell, style);
        dialog.setText(shell.getText());
        dialog.setMessage(message);
        return dialog.open();
    }

    public static int errorBox(Shell shell, Exception ex) {
        MessageBox dialog = new MessageBox(shell, SWT.ERROR | SWT.OK);
        dialog.setText(shell.getText());
        dialog.setMessage(ex.getClass().getSimpleName().replaceAll("Exception$", "") + "\n\n" + ex.getMessage());
        return dialog.open();
    }
}
