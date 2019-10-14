package org.ellab.magman;

import java.util.TreeSet;

public class FileItems {
    private FileItemStat stat = new FileItemStat();
    private TreeSet<FileItem> fileitems = new TreeSet<>();

    public void add(FileItem fi) {
        if (!fileitems.contains(fi)) {
            fileitems.add(fi);
            if (!fi.isMissing()) {
                stat.add(fi);
            }
        }
    }

    public FileItemStat getStat() {
        return stat;
    }

    public TreeSet<FileItem> getFileItems() {
        return fileitems;
    }

}
