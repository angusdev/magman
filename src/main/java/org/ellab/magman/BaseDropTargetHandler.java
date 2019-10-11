package org.ellab.magman;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.ellab.magman.FileCollections.MagazineCollection;

public abstract class BaseDropTargetHandler implements DropTargetListener {
    protected FileTransfer fileTransfer;
    private SwtMain main;

    public class DropResult {
        private List<MagazineCollection> success = new ArrayList<>();
        private List<String> fail = new ArrayList<>();

        public List<MagazineCollection> getSuccess() {
            return success;
        }

        public List<String> getFail() {
            return fail;
        }

    }

    public BaseDropTargetHandler(FileTransfer fileTransfer, SwtMain main) {
        this.fileTransfer = fileTransfer;
        this.main = main;
    }

    protected DropResult checkDropData(DropTargetEvent event) {
        System.out.println(event.data);
        DropResult result = new DropResult();
        Arrays.stream(event.dataTypes).forEach(dt -> {
            if (fileTransfer.isSupportedType(dt)) {
                Arrays.stream((String[]) event.data).forEach(f -> {
                    MagazineCollection mag = null;
                    if (new File(f).isFile()) {
                        mag = main.getFileCollections().items().stream().filter(
                                mc -> mc.getName().length() > 0 && new File(f).getName().startsWith(mc.getName()))
                                .reduce(null,
                                        (a, b) -> a == null || b.getName().length() > a.getName().length() ? b : a);
                    }

                    if (mag != null) {
                        result.success.add(mag);
                    }
                    else {
                        result.fail.add(f);
                    }
                });
            }
        });

        return result;
    }

    @Override
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

    @Override
    public void dragOver(DropTargetEvent event) {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
    }

    @Override
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

    @Override
    public void dragLeave(DropTargetEvent event) {
    }

    @Override
    public void dropAccept(DropTargetEvent event) {
    }
}
