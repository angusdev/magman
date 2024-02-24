package org.ellab.magman;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileItem implements Comparable<FileItem> {
    public static enum Problem {
        ExtraSpace, NoSpace, WrongDow
    };

    public static enum Type {
        Weekly(1), Biweekly(2), Monthly(3), Quarterly(4), SemiAnnually(5), Issue(6);

        private final int type;

        private Type(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public boolean isDate() {
            return this.equals(Weekly) || this.equals(Monthly);
        }
    }

    private static Pattern PATTERN_YYYYMMDD = Pattern
            .compile("^(([a-zA-Z]+)\\s+)?((\\d{4})(\\d{2})(\\d{2})(\\-(\\d{4})(\\d{2})(\\d{2}))?).*");
    private static Pattern PATTERN_YYYYMM = Pattern
            .compile("^(([a-zA-Z]+)\\s+)?((\\d{4})(\\d{2})(\\-(\\d{2}))?)( ([^\\s]+))?.*");
    private static Pattern PATTERN_YYYYQ = Pattern
            .compile("^(([a-zA-Z]+)\\s+)?((\\d{4})Q([1234])(\\-([1234]))?)( ([^\\s]+))?.*");
    private static Pattern PATTERN_YYYYH = Pattern
            .compile("^(([a-zA-Z]+)\\s+)?((\\d{4})H([12])(\\-([12]))?)( ([^\\s]+))?.*");
    private static Pattern PATTERN_ISSUE = Pattern.compile("^(([a-zA-Z]+)\\s+)?#(\\d+).*");

    private String parent;
    private String parentId;
    private String filename;
    private String filePath;
    private String parentPath;
    private boolean alien;
    private boolean valid;
    private boolean missing;
    private boolean dummy;
    private boolean latestOfType;
    private boolean earliestOfType;
    private String group;
    private String dateStr;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Type type;
    private Set<Problem> problems = new TreeSet<>();

    private FileItem() {
    }

    public FileItem(Path file, String parentId) {
        this.filename = file.getFileName().toString();
        this.parent = file.getParent().getFileName().toString();
        this.parentId = parentId;
        this.filePath = file.toAbsolutePath().toString();
        this.parentPath = file.getParent().toAbsolutePath().toString();

        process();
    }

    public static FileItem createDummyItem(String filename, Path parentPath, String parentId) {
        FileItem fi = new FileItem();

        fi.filename = filename;
        fi.parent = parentPath.getFileName().toString();
        parentPath.getFileName().toString();
        fi.parentId = parentId;
        fi.parentPath = fi.filePath = parentPath.toAbsolutePath().toString();

        fi.dummy = true;

        return fi;
    }

    public static FileItem createMissingFileItem(FileItem o, LocalDate dateFrom, LocalDate dateTo) {
        FileItem fi = new FileItem();
        fi.parent = o.parent;
        fi.parentId = o.parentId;
        fi.group = o.group;
        fi.filename = "Missing";
        fi.type = o.type;
        fi.dateFrom = dateFrom;
        fi.dateTo = dateTo;
        fi.missing = true;

        if (Type.Monthly.equals(fi.type)) {
            var yyyyMM = DateTimeFormatter.ofPattern("yyyyMM");
            var mm = DateTimeFormatter.ofPattern("MM");
            var yyyy = DateTimeFormatter.ofPattern("yyyy");
            fi.dateStr = yyyyMM.format(fi.dateFrom);
            if (!fi.dateFrom.equals(fi.dateTo)) {
                if (yyyy.format(fi.dateFrom).equals(yyyy.format(fi.dateTo))) {
                    fi.dateStr += "-" + mm.format(fi.dateTo);
                }
                else {
                    fi.dateStr += "-" + yyyyMM.format(fi.dateTo);
                }
            }
        }
        else if (Type.Weekly.equals(fi.type) || Type.Biweekly.equals(fi.type)) {
            fi.dateStr = DateTimeFormatter.ofPattern("yyyyMMdd").format(fi.dateFrom);
            if (!fi.dateFrom.equals(fi.dateTo)) {
                fi.dateStr += "-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(fi.dateTo);
            }
        }
        else if (Type.Quarterly.equals(fi.type)) {
            fi.dateStr = fi.dateFrom.getYear() + "Q" + ((fi.dateFrom.getMonthValue() / 3) + 1);
            if (!fi.dateFrom.equals(fi.dateTo)) {
                fi.dateStr += "-" + ((fi.dateTo.getMonthValue() / 3) + 1);
            }
        }
        else if (Type.SemiAnnually.equals(fi.type)) {
            fi.dateStr = fi.dateFrom.getYear() + "H" + ((fi.dateFrom.getMonthValue() / 6) + 1);
            if (!fi.dateFrom.equals(fi.dateTo)) {
                fi.dateStr += "-" + ((fi.dateTo.getMonthValue() / 6) + 1);
            }
        }

        fi.filename = fi.parent + " " + (fi.group != null && fi.group.length() > 0 ? fi.group + " " : "") + fi.dateStr;
        return fi;
    }

    public static FileItem createMissingFileItem(FileItem o, int issueFrom, int issueTo) {
        FileItem fi = new FileItem();
        fi.parent = o.parent;
        fi.parentId = o.parentId;
        fi.group = o.group;
        fi.filename = "Missing";
        fi.type = o.type;
        fi.missing = true;

        if (Type.Issue.equals(fi.type)) {
            fi.dateStr = "" + issueFrom + (issueFrom != issueTo ? ("-" + issueTo) : "");
            fi.dateTo = fi.dateFrom = LocalDate.of(issueTo, 1, 1);
        }
        else {
            return null;
        }

        fi.filename = fi.parent + " " + (fi.group != null && fi.group.length() > 0 ? fi.group + " " : "") + "#"
                + fi.dateStr;
        return fi;
    }

    public void addProblem(Problem p, boolean valid) {
        problems.add(p);
        if (!valid) {
            this.valid = valid;
        }
    }

    @Override
    public int compareTo(FileItem o) {
        if (o == null) {
            return 1;
        }

        int thisValidOrder = this.alien ? 2 : this.valid || this.missing ? 0 : 1;
        int thatValidOrder = o.alien ? 2 : o.valid || o.missing ? 0 : 1;

        if (thisValidOrder != thatValidOrder) {
            return thisValidOrder - thatValidOrder;
        }

        if (!valid && !missing) {
            return this.filename.compareTo(o.filename);
        }

        if (!this.parent.equals(o.parent)) {
            return this.parent.compareTo(o.parent);
        }

        boolean groupEquals = this.group == null ? o.group == null : this.group.equals(o.group);
        if (!groupEquals) {
            return this.group == null ? -1 : o.group == null ? 1 : this.group.compareTo(o.group);
        }

        if (!this.type.equals(o.type)) {
            return this.type.getType() - o.type.getType();
        }

        int dateResult = this.dateFrom.compareTo(o.dateFrom);
        if (dateResult == 0) {
            dateResult = this.dateTo.compareTo(o.dateTo);
        }
        if (dateResult == 0) {
            return this.filename.compareTo(o.filename);
        }
        else {
            return dateResult;
        }
    }

    private void process() {
        String str = filename;

        // remove extension
        int pos = str.lastIndexOf('.');
        if (pos >= 0) {
            str = str.substring(0, pos);
        }

        if (str.startsWith(parent + " ")) {
            str = str.substring(parent.length() + 1);
            if (str.startsWith(" ")) {
                // System.out.println(filename + " - extraSpace");
                problems.add(Problem.ExtraSpace);
                str = str.trim();
            }
        }
        else if (str.startsWith(parent)) {
            str = str.substring(parent.length());
            problems.add(Problem.NoSpace);
        }
        else {
            alien = true;
            return;
        }

        // pos = str.indexOf(' '); if (pos >= 0) { group = str.substring(0, pos); str = str.substring(pos + 1); }

        Matcher m = PATTERN_YYYYMMDD.matcher(str);
        if (m.find()) {
            // System.out.println("Weekly");
            // for (int i = 0; m.matches() && i <= m.groupCount(); i++) {
            // System.out.println(i + " - " + m.group(i));
            // }
            group = m.group(2) != null ? m.group(2) : "";
            dateStr = m.group(3);

            int y1 = 0, m1 = 0, d1 = 0, y2 = 0, m2 = 0, d2 = 0;
            if (m.group(7) != null) {
                y1 = Integer.parseInt(m.group(4));
                m1 = Integer.parseInt(m.group(5));
                d1 = Integer.parseInt(m.group(6));
                y2 = Integer.parseInt(m.group(8));
                m2 = Integer.parseInt(m.group(9));
                d2 = Integer.parseInt(m.group(10));
            }
            else {
                y2 = y1 = Integer.parseInt(m.group(4));
                m2 = m1 = Integer.parseInt(m.group(5));
                d2 = d1 = Integer.parseInt(m.group(6));
            }

            try {
                dateFrom = LocalDate.of(y1, m1, d1);
                dateTo = LocalDate.of(y2, m2, d2);
                valid = true;
                type = Type.Weekly;
            }
            catch (DateTimeException ex) {
                dateFrom = null;
                dateTo = null;
            }

            return;
        }

        m = PATTERN_YYYYMM.matcher(str);
        if (m.find()) {
            // System.out.println("Monthly");
            // for (int i = 0; m.matches() && i <= m.groupCount(); i++) {
            // System.out.println(i + " - " + m.group(i));
            // }
            valid = true;
            group = m.group(2) != null ? m.group(2) : "";
            type = Type.Monthly;
            dateStr = m.group(3);

            int y1 = 0, m1 = 0, y2 = 0, m2 = 0, d1 = 1, d2 = 1;
            if (m.group(6) != null) {
                y1 = Integer.parseInt(m.group(4));
                m1 = Integer.parseInt(m.group(5));
                m2 = Integer.parseInt(m.group(7));
                y2 = m2 < m1 ? y1 + 1 : y1;
            }
            else {
                y2 = y1 = Integer.parseInt(m.group(4));
                m2 = m1 = Integer.parseInt(m.group(5));
                if ("Xmas".equals(m.group(7))) {
                    d2 = d1 = 25;
                }
            }
            dateFrom = LocalDate.of(y1, m1, d1);
            dateTo = LocalDate.of(y2, m2, d2);

            return;
        }

        m = PATTERN_YYYYQ.matcher(str);
        if (m.find()) {
            // System.out.println("Quarterly");
            // for (int i = 0; m.matches() && i <= m.groupCount(); i++) {
            // System.out.println(i + " - " + m.group(i));
            // }
            valid = true;
            group = m.group(2) != null ? m.group(2) : "";
            type = Type.Quarterly;
            dateStr = m.group(3);

            int y1 = 0, m1 = 0, y2 = 0, m2 = 0, d1 = 1, d2 = 1;
            if (m.group(6) != null) {
                y1 = Integer.parseInt(m.group(4));
                m1 = Integer.parseInt(m.group(5)) * 3 - 1;
                m2 = Integer.parseInt(m.group(7)) * 3 - 1;
                y2 = m2 < m1 ? y1 + 1 : y1;
            }
            else {
                y2 = y1 = Integer.parseInt(m.group(4));
                m2 = m1 = Integer.parseInt(m.group(5)) * 3 - 1;
            }
            dateFrom = LocalDate.of(y1, m1, d1);
            dateTo = LocalDate.of(y2, m2, d2);

            return;
        }

        m = PATTERN_YYYYH.matcher(str);
        if (m.find()) {
            // System.out.println("SemiAnnual");
            // for (int i = 0; m.matches() && i <= m.groupCount(); i++) {
            // System.out.println(i + " - " + m.group(i));
            // }
            valid = true;
            group = m.group(2) != null ? m.group(2) : "";
            type = Type.SemiAnnually;
            dateStr = m.group(3);

            int y1 = 0, m1 = 0, y2 = 0, m2 = 0, d1 = 1, d2 = 1;
            if (m.group(6) != null) {
                y1 = Integer.parseInt(m.group(4));
                m1 = Integer.parseInt(m.group(5)) * 6 - 1;
                m2 = Integer.parseInt(m.group(7)) * 6 - 1;
                y2 = m2 < m1 ? y1 + 1 : y1;
            }
            else {
                y2 = y1 = Integer.parseInt(m.group(4));
                m2 = m1 = Integer.parseInt(m.group(5)) * 6 - 1;
            }
            dateFrom = LocalDate.of(y1, m1, d1);
            dateTo = LocalDate.of(y2, m2, d2);

            return;
        }

        m = PATTERN_ISSUE.matcher(str);
        if (m.find()) {
            // System.out.println("Issue");
            // for (int i = 0; m.matches() && i <= m.groupCount(); i++) {
            // System.out.println(i + " - " + m.group(i));
            // }
            valid = true;
            group = m.group(2) != null ? m.group(2) : "";
            type = Type.Issue;
            dateStr = m.group(3);
            dateTo = dateFrom = LocalDate.of(Integer.parseInt(dateStr), 1, 1);

            return;
        }

        // System.out.println("invalid:" + str);
    }

    public boolean isDateType() {
        return type != null && !type.equals(Type.Issue);
    }

    public boolean isOk() {
        return valid && problems.size() == 0;
    }

    public String getParent() {
        return parent;
    }

    public String getParentId() {
        return parentId;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getParentPath() {
        return parentPath;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isAlien() {
        return alien;
    }

    public boolean isFile() {
        return !missing && !dummy;
    }

    public boolean isMissing() {
        return missing;
    }

    public boolean isDummy() {
        return dummy;
    }

    public boolean isEarliestOfType() {
        return earliestOfType;
    }

    public void setEarliestOfType(boolean earliestOfType) {
        this.earliestOfType = earliestOfType;
    }

    public boolean isLatestOfType() {
        return latestOfType;
    }

    public void setLatestOfType(boolean latestOfType) {
        this.latestOfType = latestOfType;
    }

    public String getGroup() {
        return group;
    }

    public String getDateStr() {
        return dateStr;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Set<Problem> getProblems() {
        return problems;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FileItem [parent=");
        builder.append(parent);
        builder.append(", filename=");
        builder.append(filename);
        builder.append(", valid=");
        builder.append(valid);
        builder.append(", missing=");
        builder.append(missing);
        builder.append(", alien=");
        builder.append(alien);
        builder.append(", latestOfType=");
        builder.append(latestOfType);
        builder.append(", group=");
        builder.append(group);
        builder.append(", dateStr=");
        builder.append(dateStr);
        builder.append(", dateFrom=");
        builder.append(dateFrom);
        builder.append(", dateTo=");
        builder.append(dateTo);
        builder.append(", type=");
        builder.append(type);
        builder.append(", problems=");
        builder.append(problems);
        builder.append("]");
        return builder.toString();
    }

}
