package org.ellab.magman;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileItemStat {
    public class Stat {
        private int fileCount;
        private long fileSize;

        public int getFileCount() {
            return fileCount;
        }

        public long getFileSize() {
            return fileSize;
        }

    }

    private int currentYear;

    private Map<Integer, Stat> index = new HashMap<>();

    public FileItemStat() {
        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        index.put(0, new Stat()); // total
        index.put(7, new Stat());
        index.put(30, new Stat());
        index.put(currentYear, new Stat());
        index.put(currentYear - 1, new Stat());
        index.put(currentYear - 2, new Stat());
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public Stat getTotal() {
        return get(0);
    }

    public Stat get(int i) {
        Stat stat = index.get(i);
        if (stat == null) {
            return new Stat();
        }
        else {
            return stat;
        }
    }

    public void add(FileItem fi) {
        BasicFileAttributes attr;
        try {
            long fileSize = new File(fi.getFilePath()).length();

            index.get(0).fileCount++;
            index.get(0).fileSize += fileSize;

            Path file = Paths.get(fi.getFilePath());
            attr = Files.readAttributes(file, BasicFileAttributes.class);
            Date now = new Date();
            if (now.getTime() - attr.creationTime().toMillis() <= 86400000l * 7) {
                index.get(7).fileCount++;
                index.get(7).fileSize += fileSize;
            }
            if (now.getTime() - attr.creationTime().toMillis() <= 86400000l * 30) {
                index.get(30).fileCount++;
                index.get(30).fileSize += fileSize;
            }

            int fileYear = Integer.parseInt(attr.creationTime().toString().substring(0, 4));
            if (fileYear == currentYear) {
                index.get(fileYear).fileCount++;
                index.get(fileYear).fileSize += fileSize;
            }
            if (fileYear == currentYear - 1) {
                index.get(fileYear).fileCount++;
                index.get(fileYear).fileSize += fileSize;
            }
            if (fileYear == currentYear - 2) {
                index.get(fileYear).fileCount++;
                index.get(fileYear).fileSize += fileSize;
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
