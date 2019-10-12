package org.ellab.magman;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String[] MONTH_NAME = new String[] { "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER" };

    public static boolean isValidYYYYMMDD(final String s) {
        if (!s.matches("^\\d{8}$")) {
            return false;
        }

        final DateFormat df = new SimpleDateFormat("yyyymmdd");
        try {
            return s.equals(df.format(df.parse(s)));
        }
        catch (ParseException ex) {
            return false;
        }
    }

    private static int[] indexOfWord(final String str, final String match) {
        final int pos = str.indexOf(match + " ");
        if (pos >= 0) {
            return new int[] { pos, pos + match.length() };
        }

        if (str.endsWith(match)) {
            return new int[] { str.length() - match.length(), str.length() };
        }

        return null;
    }

    // check if the file name matches with the magazine name
    // e.g. Java Official = Java Official 2019 July.pdf
    // also Java Official = Official Java 2019 July.pdf
    public static int[] fuzzyIndexOf(final String str, final String match) {
        int[] result = indexOfWord(str, match);
        if (result != null) {
            return result;
        }

        final String[] splited = match.split(" ");
        if (splited.length != 2) {
            return null;
        }

        return indexOfWord(str, splited[1] + " " + splited[0]);
    }

    public static String guessDateFromFilename(final String name, final FileItem.Type type) {
        // int[index, value, fromMonthName]
        int[] year = null;
        List<int[]> month = new ArrayList<>();
        List<int[]> day = new ArrayList<>();
        List<int[]> monthOrDay = new ArrayList<>();

        final String splited[] = name.split(" ");
        for (int i = 0; i < splited.length; i++) {
            final String s = splited[i];
            if (Utils.isValidYYYYMMDD(s)) {
                // yyyymmdd
                return s;
            }
            else if (s.matches("^\\d{4}$")) {
                if (year == null) {
                    year = new int[] { i, Integer.parseInt(s) };
                }
            }
            else {
                int strToMonth = Utils.strToMonth(s);
                if (strToMonth > 0) {
                    month.add(new int[] { i, strToMonth, 1 });
                }
                else if (s.matches("^\\d{2}$")) {
                    final int value = Integer.parseInt(s);

                    // yyyy 12
                    // yyyy 31
                    // 12 xx yyyy
                    // 31 xx yyyy
                    if (value > 12 && value <= 31) {
                        // > 12, must be day
                        day.add(new int[] { i, value });
                    }
                    else if (value >= 1 && value <= 12) {
                        if (year != null) {
                            if (month.size() == 0) {
                                // yyyy and no month, this should be month
                                month.add(new int[] { i, value, 0 });
                            }
                            else {
                                // yyyy mm, this should be day
                                day.add(new int[] { i, value, 0 });
                            }
                        }
                        else {
                            if (month.size() > 0) {
                                // no year, but has month, e.g. Feb (12) 2019
                                day.add(new int[] { i, value });
                            }
                            else {
                                // no year, this can be day or month, e.g. (12) 2019, or (11) 10 2019
                                monthOrDay.add(new int[] { i, value });
                            }
                        }
                    }
                }
            }
        }

        if (year == null || (month.size() + day.size() + monthOrDay.size()) == 0) {
            // no year or month, not a valid date
            return name;
        }

        if (month.size() == 0) {
            // no month, must be monthOrDay
            if (monthOrDay.size() == 0) {
                return name;
            }

            month.add(monthOrDay.get(0));
            monthOrDay.remove(0);
        }

        // now has year and has month
        int m = month.get(0)[1];
        if (type.equals(FileItem.Type.Monthly)) {
            return year[1] + (m < 10 ? "0" : "") + m;
        }
        else if (type.equals(FileItem.Type.Weekly) || type.equals(FileItem.Type.Biweekly)) {
            int d = day.size() > 0 ? day.get(0)[1] : 0;
            if (d == 0) {
                d = monthOrDay.size() > 0 ? monthOrDay.get(0)[1] : 0;
            }
            if (d > 0) {
                return year[1] + (m < 10 ? "0" : "") + m + (d < 10 ? "0" : "") + d;
            }
        }

        return name;

    }

    public static int strToMonth(String s) {
        if (s == null) {
            return -1;
        }

        s = s.toUpperCase();
        if (!s.matches("^[A-Z]{3,9}$")) {
            return -1;
        }

        for (int i = 0; i < MONTH_NAME.length; i++) {
            if (MONTH_NAME[i].startsWith(s)) {
                return i + 1;
            }
        }

        return -1;
    }
}
