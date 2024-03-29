package org.ellab.magman;

import java.io.File;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ellab.magman.FileItem.Type;

public class FileCollections {
    private static Set<String> SUPPORTED_TYPE = new HashSet<>();
    static {
        SUPPORTED_TYPE.add("pdf");
        SUPPORTED_TYPE.add("mp3");
        SUPPORTED_TYPE.add("epub");
        SUPPORTED_TYPE.add("mobi");
        SUPPORTED_TYPE.add("doc");
        SUPPORTED_TYPE.add("docx");
        SUPPORTED_TYPE.add("xls");
        SUPPORTED_TYPE.add("xlsx");
    }

    private FileItems allFiles = new FileItems();
    private Map<String, MagazineCollection> map = new TreeMap<>(new Comparator<String>() {
        @Override
        // empty string sort to bottom
        public int compare(String o1, String o2) {
            if (o1.length() == 0 && o2.length() == 0) {
                return 0;
            }
            else if (o1.length() == 0) {
                return 1;
            }
            else if (o2.length() == 0) {
                return -1;
            }
            else {
                return o1.compareTo(o2);
            }
        }

    });

    public class MagazineCollection {
        private String name;
        private String path;
        private boolean dummy = true;
        private FileItems files = new FileItems();
        private Map<String, Map<FileItem.Type, FileItems>> groupMap = new TreeMap<>();
        private Set<FileItem> invalidFiles = new TreeSet<>();

        public MagazineCollection(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public void add(FileItem fi) {
            files.add(fi);

            dummy &= fi.isDummy();

            if (fi.isValid()) {
                String group = fi.getGroup() == null ? "" : fi.getGroup();
                Map<FileItem.Type, FileItems> map = groupMap.get(group);
                if (map == null) {
                    map = new TreeMap<>();
                    groupMap.put(group, map);
                }
                FileItems fis = map.get(fi.getType());
                if (fis == null) {
                    fis = new FileItems();
                    map.put(fi.getType(), fis);
                }
                fis.add(fi);
            }
            else {
                invalidFiles.add(fi);
            }
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public boolean isDummy() {
            return dummy;
        }

        public Set<String> groups() {
            return groupMap.keySet();
        }

        public Map<FileItem.Type, FileItems> group(String group) {
            return groupMap.get(group);
        }

        public FileItems group(String group, FileItem.Type type) {
            return groupMap.get(group).get(type);
        }

        public FileItems files() {
            return files;
        }

        public String toStringPretty() {
            StringBuilder sb = new StringBuilder();
            sb.append(this).append("\n[\n");
            sb.append("name=").append(name).append("\n");
            sb.append("path=").append(path).append("\n");
            groupMap.entrySet().forEach(g -> {
                g.getValue().entrySet().forEach(e -> {
                    sb.append("  type=").append(e.getKey()).append("\n");
                    e.getValue().getFileItems().forEach(v -> sb.append("    ").append(v).append("\n"));
                });
            });
            sb.append("]\n");

            return sb.toString();
        }
    }

    public void add(FileItem fi) {
        allFiles.add(fi);

        MagazineCollection c = map.get(fi.getParentId());
        if (c == null) {
            c = new MagazineCollection(fi.getParentId(), fi.getParentPath());
            map.put(fi.getParentId(), c);
        }
        c.add(fi);
    }

    public FileItems files() {
        return allFiles;
    }

    public MagazineCollection mc(String key) {
        return map.get(key);
    }

    public Collection<MagazineCollection> items() {
        return map.values();
    }

    public void remove(MagazineCollection[] magazine) {
        Arrays.stream(magazine).forEach(m -> map.remove(m.getName()));

        // Reset the stat
        allFiles = new FileItems();
        map.forEach((m, mc) -> mc.files().getFileItems().stream().forEach(f -> allFiles.add(f)));
    }

    public static boolean isSupportedFilename(String name) {
        final String ext = name.lastIndexOf('.') > 0
                ? name.substring(name.lastIndexOf('.') + 1, name.length()).trim().toLowerCase()
                : "";

        return SUPPORTED_TYPE.contains(ext.toLowerCase());
    }

    public void analysis() {
        map.forEach((magazine, mc) -> analysis(mc));
    }

    public void analysis(String magazine) {
        MagazineCollection mc = map.get(magazine);
        if (mc != null) {
            analysis(mc);
        }
    }

    private void analysis(MagazineCollection mc) {
        Set<FileItem> missing = new HashSet<>();
        var asjustWeeklyToBiweekly = new Object() {
            boolean ordinal = false;
        };

        mc.groupMap.forEach((group, types) -> {
            types.forEach((type, fis) -> {
                // mark the first/last item of the type
                fis.getFileItems().first().setEarliestOfType(true);
                fis.getFileItems().last().setLatestOfType(true);

                if (type.equals(FileItem.Type.Weekly)) {
                    // Check if wrong day of week
                    final DayOfWeek maxDow = fis.getFileItems().stream()
                            .collect(Collectors.groupingBy(s -> s.getDateFrom().getDayOfWeek(), Collectors.counting()))
                            .entrySet().stream()
                            .max(Comparator.comparing(Entry<DayOfWeek, Long>::getValue).thenComparing(Entry::getKey))
                            .get().getKey();
                    // System.out.println("max=" + maxDow);
                    List<Long> daysList = new ArrayList<>();
                    FileItem prev = null;
                    for (FileItem fi : fis.getFileItems()) {
                        if (!fi.getDateFrom().getDayOfWeek().equals(maxDow)) {
                            fi.addProblem(FileItem.Problem.WrongDow, false);
                        }
                        if (prev != null) {
                            long days = ChronoUnit.DAYS.between(prev.getDateFrom(), fi.getDateFrom());
                            daysList.add(days);
                        }
                        prev = fi;
                    }

                    // Check if biweekly
                    final Optional<Map.Entry<Long, Long>> maxFreq = daysList.stream()
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream()
                            .max(Comparator.comparing(Entry::getValue));
                    if (maxFreq.isPresent() && maxFreq.get().getKey() == 14) {
                        fis.getFileItems().forEach(fi -> {
                            fi.setType(FileItem.Type.Biweekly);
                        });
                        asjustWeeklyToBiweekly.ordinal = true;
                    }
                }

                // add missing entries
                FileItem prev = null;
                for (FileItem fi : fis.getFileItems()) {
                    if (prev != null) {
                        if (FileItem.Type.Monthly.equals(type)) {
                            LocalDate calPrev = prev.getDateTo();
                            LocalDate calThis = fi.getDateFrom();
                            if (calPrev.getYear() * 12 + calPrev.getMonthValue() + 1 < calThis.getYear() * 12
                                    + calThis.getMonthValue()) {
                                calPrev = calPrev.plusMonths(1).withDayOfMonth(1);
                                calThis = calThis.minusMonths(1).withDayOfMonth(1);
                                FileItem n = FileItem.createMissingFileItem(prev, calPrev, calThis);
                                missing.add(n);
                            }
                        }
                        else if (FileItem.Type.Weekly.equals(type)) {
                            LocalDate calExpected = prev.getDateTo();
                            LocalDate calThis = fi.getDateFrom();
                            calExpected = calExpected.plusDays(7);
                            if (calThis.isAfter(calExpected)) {
                                calThis = calThis.minusDays(7);
                                FileItem n = FileItem.createMissingFileItem(prev, calExpected, calThis);
                                missing.add(n);
                            }
                        }
                        else if (FileItem.Type.Quarterly.equals(type)) {
                            LocalDate calExpected = prev.getDateTo();
                            calExpected = calExpected.plusMonths(3);
                            while (calExpected.isBefore(fi.getDateFrom())) {
                                FileItem n = FileItem.createMissingFileItem(prev, calExpected, calExpected);
                                missing.add(n);

                                calExpected = calExpected.plusMonths(3);
                            }
                        }
                        else if (FileItem.Type.SemiAnnually.equals(type)) {
                            LocalDate calExpected = prev.getDateTo();
                            calExpected = calExpected.plusMonths(6);
                            while (calExpected.isBefore(fi.getDateFrom())) {
                                FileItem n = FileItem.createMissingFileItem(prev, calExpected, calExpected);
                                missing.add(n);

                                calExpected = calExpected.plusMonths(6);
                            }
                        }
                        else if (FileItem.Type.Issue.equals(type)) {
                            int issuePrev = Integer.parseInt(prev.getDateStr());
                            int issueThis = Integer.parseInt(fi.getDateStr());
                            if (issueThis - issuePrev > 1) {
                                FileItem n = FileItem.createMissingFileItem(prev, issuePrev + 1, issueThis - 1);
                                missing.add(n);
                            }
                        }
                    }
                    prev = fi;
                }
            });

            if (asjustWeeklyToBiweekly.ordinal) {
                types.put(FileItem.Type.Biweekly, types.get(FileItem.Type.Weekly));
                types.remove(FileItem.Type.Weekly);
            }
        });

        missing.forEach(m -> add(m));
    }

    public FileItem guessFilename(String oriName, FileItem.Type typeHint) {
        FileItem renameItem = null;
        String renameTo = null;

        // extract the file name and extension by regexp
        final String ext = oriName.lastIndexOf('.') > 0
                ? oriName.substring(oriName.lastIndexOf('.') + 1, oriName.length()).toLowerCase()
                : null;
        String name = oriName.replaceFirst("[.][^.]+$", "");
        name = Utils.cleanFilename(name);

        final String searchName = name.toUpperCase();

        // match the collection
        final MagazineCollection mag = items().stream()
                .filter(mc -> !mc.isDummy() && mc.getName().length() > 0
                        && Utils.fuzzyIndexOf(searchName, mc.getName().toUpperCase()) != null)
                .reduce(null, (a, b) -> a == null || b.getName().length() > a.getName().length() ? b : a);

        // find the longest matched group (region)
        if (mag != null) {
            final String group = mag.groups().stream().reduce(null, (a, b) -> {
                String m = mag.getName().toUpperCase() + ((b != null && b.length() > 0) ? (" " + b) : "");
                if (Utils.fuzzyIndexOf(searchName, m) != null) {
                    return a == null || b.length() > a.length() ? b : a;
                }
                else {
                    return a;
                }
            });

            if (group == null) {
                // group will be null if all files are invalid
                return null;
            }

            // if not provided typeHint, get the most frequent type
            final Type type = typeHint != null ? typeHint
                    : (mag.group(group).keySet().stream().reduce((a, b) -> a.ordinal() < b.ordinal() ? a : b).get());

            final String prefix = mag.getName() + (group.length() > 0 ? (" " + group) : "");

            // remove the magazine name from the file name
            int[] pos = Utils.fuzzyIndexOf(searchName, prefix.toUpperCase());
            // pos must be non-null
            final String remainedName = searchName.substring(0, pos[0])
                    + searchName.substring(pos[1], searchName.length()).trim();

            final String guessedName = Utils.guessDateFromFilename(remainedName, type);

            if (guessedName.equals(remainedName)) {
                // didn't convert to date, capitalize the remaining part
                renameTo = prefix + " " + Utils.capitalize(remainedName) + "." + ext;
            }
            else {
                renameTo = prefix + " " + guessedName + "." + ext;
            }

            if (renameTo != null) {
                renameItem = new FileItem(Paths.get(mag.getPath() + File.separator + renameTo), mag.getName());
            }
        }

        return renameItem;
    }
}
