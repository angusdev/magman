package org.ellab.magman;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

public class FileCollections {
    public class MagazineCollection {
        private String name;
        private String path;
        private Set<FileItem> files = new TreeSet<>();
        private Map<String, Map<FileItem.Type, TreeSet<FileItem>>> groupMap = new TreeMap<>();
        private Set<FileItem> invalidFiles = new TreeSet<>();

        public MagazineCollection(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public void add(FileItem fi) {
            files.add(fi);
            if (fi.isValid()) {
                String group = fi.getGroup() == null ? "" : fi.getGroup();
                Map<FileItem.Type, TreeSet<FileItem>> map = groupMap.get(group);
                if (map == null) {
                    map = new TreeMap<>();
                    groupMap.put(group, map);
                }
                TreeSet<FileItem> set = map.get(fi.getType());
                if (set == null) {
                    set = new TreeSet<>();
                    map.put(fi.getType(), set);
                }
                set.add(fi);
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

        public Set<String> groups() {
            return groupMap.keySet();
        }

        public Set<FileItem> group(String group, FileItem.Type type) {
            return groupMap.get(group).get(type);
        }

        public Set<FileItem> files() {
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
                    e.getValue().forEach(v -> sb.append("    ").append(v).append("\n"));
                });
            });
            sb.append("]\n");

            return sb.toString();
        }
    }

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

    public void add(FileItem fi) {
        MagazineCollection c = map.get(fi.getParentId());
        if (c == null) {
            c = new MagazineCollection(fi.getParentId(), fi.getParentPath());
            map.put(fi.getParentId(), c);
        }
        c.add(fi);
    }

    public Collection<MagazineCollection> items() {
        return map.values();
    }

    public void analysis() {
        Set<FileItem> missing = new HashSet<>();

        map.forEach((magazine, mc) -> {
            mc.groupMap.forEach((group, types) -> {
                types.forEach((type, fis) -> {
                    // mark the first/last item of the type
                    fis.first().setEarliestOfType(true);
                    fis.last().setLatestOfType(true);

                    if (type.equals(FileItem.Type.Weekly)) {
                        // Check if wrong day of week
                        final DayOfWeek maxDow = fis.stream()
                                .collect(Collectors.groupingBy(s -> s.getDateFrom().getDayOfWeek(),
                                        Collectors.counting()))
                                .entrySet().stream().max(Comparator.comparing(Entry::getValue)).get().getKey();
                        // System.out.println("max=" + maxDow);
                        List<Long> daysList = new ArrayList<>();
                        FileItem prev = null;
                        for (FileItem fi : fis) {
                            if (!fi.getDateFrom().getDayOfWeek().equals(maxDow)) {
                                fi.addProblem(FileItem.Problem.WrongDow, false);
                            }
                            if (prev != null) {
                                long days = ChronoUnit.DAYS.between(fi.getDateFrom(), prev.getDateFrom());
                                daysList.add(days);
                            }
                            prev = fi;
                        }

                        // Check if biweekly
                        final Optional<Map.Entry<Long, Long>> maxFreq = daysList.stream()
                                .collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream()
                                .max(Comparator.comparing(Entry::getValue));
                        if (maxFreq.isPresent() && maxFreq.get().getKey() == 14) {
                            fis.forEach(fi -> {
                                fi.setType(FileItem.Type.Biweekly);
                            });
                        }
                    }

                    // add missing entries
                    FileItem prev = null;
                    for (FileItem fi : fis) {
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
                                calExpected = calExpected
                                        .plusDays(FileItem.Type.Biweekly.equals(fi.getType()) ? 14 : 7);
                                if (calThis.isAfter(calExpected)) {
                                    calThis = calThis.minusDays(FileItem.Type.Biweekly.equals(fi.getType()) ? 14 : 7);
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
            });
        });

        missing.forEach(m -> add(m));
    }
}
