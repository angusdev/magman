package org.ellab.magman;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final String[] MONTH_NAME = new String[] { "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER" };

    private static final Map<String, Integer> QUARTER_NAME = new HashMap<>();
    static {
        QUARTER_NAME.put("SPRING", 1);
        QUARTER_NAME.put("SUMMER", 2);
        QUARTER_NAME.put("FALL", 3);
        QUARTER_NAME.put("AUTUMN", 3);
        QUARTER_NAME.put("WINTER", 4);
    }

    private static Map<String, Integer> EDITION_MAP = new HashMap<>();
    static {
        EDITION_MAP.put("four", 4);
        EDITION_MAP.put("six", 6);
        EDITION_MAP.put("seven", 7);
        EDITION_MAP.put("ten", 10);
        EDITION_MAP.put("eleven", 11);
        EDITION_MAP.put("thirteen", 13);
        EDITION_MAP.put("fourteen", 14);
        EDITION_MAP.put("fifteen", 15);
        EDITION_MAP.put("sixteen", 16);
        EDITION_MAP.put("seventeen", 17);
        EDITION_MAP.put("eighteen", 18);
        EDITION_MAP.put("nineteen", 19);
        new HashSet<>(EDITION_MAP.entrySet()).stream().forEach(e -> EDITION_MAP.put(e.getKey() + "th", e.getValue()));
        EDITION_MAP.put("one", 1);
        EDITION_MAP.put("first", 1);
        EDITION_MAP.put("two", 2);
        EDITION_MAP.put("second", 2);
        EDITION_MAP.put("three", 3);
        EDITION_MAP.put("third", 3);
        EDITION_MAP.put("five", 5);
        EDITION_MAP.put("fifth", 5);
        EDITION_MAP.put("eight", 8);
        EDITION_MAP.put("eighth", 8);
        EDITION_MAP.put("nine", 9);
        EDITION_MAP.put("ninth", 9);
        EDITION_MAP.put("twelve", 12);
        EDITION_MAP.put("twelfth", 12);
        EDITION_MAP.put("twenty", 20);
        EDITION_MAP.put("twentieth", 20);
    }

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

    private static int[] indexOfWord(final String str, final String substr) {
        final int pos = str.indexOf(substr + " ");
        if (pos >= 0) {
            return new int[] { pos, pos + substr.length() };
        }

        if (str.endsWith(substr)) {
            return new int[] { str.length() - substr.length(), str.length() };
        }

        return null;
    }

    // check if the file name matches with the magazine name
    // e.g. Java Official = Java Official 2019 July.pdf
    // also Java Official = Official Java 2019 July.pdf
    public static int[] fuzzyIndexOf(String str, String substr) {
        str = str.toUpperCase();
        substr = substr.toUpperCase();

        int[] result = indexOfWord(str, substr);
        if (result != null) {
            return result;
        }

        final String[] splited = str.split(" ");
        if (splited.length > 1) {
            for (int i = 0; i < splited.length - 1; i++) {
                String rearranged = "";
                for (int j = 0; j < i; j++) {
                    rearranged += (j > 0 ? " " : "") + splited[j];
                }
                rearranged += (i > 0 ? " " : "") + splited[i + 1] + " " + splited[i];
                for (int j = i + 2; j < splited.length; j++) {
                    rearranged += " " + splited[j];
                }
                result = indexOfWord(rearranged, substr);
                if (result != null) {
                    return result;
                }
            }
        }

        String substrNoSpace = substr.replaceAll(" ", "");
        result = indexOfWord(str, substrNoSpace);
        if (result != null) {
            return result;
        }

        if (str.indexOf(" & ") > 0 && substr.indexOf(" AND ") > 0) {
            result = fuzzyIndexOf(str.replace(" & ", " AND "), substr);
            // adjust the length as it is replaced
            result[1] -= 2;
        }
        else if (str.indexOf(" AND ") > 0 && substr.indexOf(" &") > 0) {
            result = fuzzyIndexOf(str.replace(" AND ", " & "), substr);
            // adjust the length as it is replaced
            result[1] += 2;
        }

        return result;
    }

    public static String guessDateFromFilename(String oriname, final FileItem.Type type) {
        // int[index, value, fromMonthName/fromQuarterName]
        int[] year = null;
        int[] issue = null;
        List<int[]> month = new ArrayList<>();
        List<int[]> day = new ArrayList<>();
        List<int[]> monthOrDay = new ArrayList<>();
        List<int[]> quarter = new ArrayList<>();

        // insert space between number and word
        String name = oriname.replaceAll("(?<=[A-Za-z])(?=[0-9])|(?<=[0-9])(?=[A-Za-z])", " ");

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
            else if (s.matches("^ISSUES?$")) {
                if (i < splited.length - 1 && splited[i + 1].matches("^\\d+$")) {
                    issue = new int[] { i + 1, Integer.parseInt(splited[i + 1]) };
                }
            }
            else {
                int strToMonth = Utils.strToMonth(s);
                if (strToMonth > 0) {
                    month.add(new int[] { i, strToMonth, 1 });
                }
                else if (QUARTER_NAME.containsKey(s)) {
                    quarter.add(new int[] { i, QUARTER_NAME.get(s), 1 });
                }
                else if (s.matches("^\\d{1,2}$")) {
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

        // since there is quarter name, will treat it as quarter no matter what type is
        // it is common to have quarter issue for monthly magazine
        if (year != null && quarter.size() > 0) {
            return year[1] + "Q" + quarter.get(0)[1] + (quarter.size() > 1 ? "-" + quarter.get(1)[1] : "");
        }

        if (type.equals(FileItem.Type.Issue) && issue != null) {
            return "#" + issue[1] + " " + year[1];
        }

        if (year == null || (month.size() + day.size() + monthOrDay.size()) == 0) {
            // no year or month, not a valid date
            return oriname;
        }

        if (month.size() == 0) {
            // no month, must be monthOrDay

            if (monthOrDay.size() == 0) {
                // no monthOrDay, not match
                return oriname;
            }

            if (monthOrDay.size() == 1) {
                // only 1 monthOrDay, this must be month
                month.add(monthOrDay.get(0));
                monthOrDay.remove(0);
            }
            else if (monthOrDay.get(0)[0] < year[0]) {
                // more than 1 monthOrDay, and this is in front of year, it should be dd mm yyyy
                month.add(monthOrDay.get(1));
                monthOrDay.remove(1);
            }
            else {
                // yyyy mm dd
                month.add(monthOrDay.get(0));
                monthOrDay.remove(0);
            }
        }

        // now has year and has month
        int m = month.get(0)[1];
        if (type.equals(FileItem.Type.Quarterly)) {
            return year[1] + "Q" + ((m - 1) / 3 + 1);
        }
        else if (type.equals(FileItem.Type.Monthly)) {
            String s = year[1] + (m < 10 ? "0" : "") + m;
            if (month.size() > 1) {
                // month range
                int m2 = month.get(1)[1];
                s += "-" + (m2 < 10 ? "0" : "") + m2;
            }

            return s;
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

        return oriname;
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

    public static String kmg(final long n, final boolean upperCase, final String prefix, final String suffix) {
        DecimalFormat dec = new DecimalFormat("0.00");
        String s;
        double n2;

        if ((n2 = n / 1099511627776.0) > 1) {
            s = dec.format(n2).concat((prefix == null ? "" : prefix)).concat((upperCase ? "T" : "t"))
                    .concat(suffix == null ? "" : suffix);
        }
        else if ((n2 = n / 1073741824.0) > 1) {
            s = dec.format(n2).concat((prefix == null ? "" : prefix)).concat((upperCase ? "G" : "g"))
                    .concat(suffix == null ? "" : suffix);
        }
        else if ((n2 = n / 1048576.0) > 1) {
            s = dec.format(n2).concat((prefix == null ? "" : prefix)).concat((upperCase ? "M" : "m"))
                    .concat(suffix == null ? "" : suffix);
        }
        else if ((n2 = n / 1024.0) > 1) {
            s = dec.format(n2).concat((prefix == null ? "" : prefix)).concat((upperCase ? "K" : "k"))
                    .concat(suffix == null ? "" : suffix);
        }
        else {
            s = ("" + n).concat((prefix == null ? "" : prefix)).concat(suffix == null ? "" : suffix);
        }

        return s;
    }

    public static String capitalize(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }

        boolean prevIsSpace = true;
        char[] result = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.toString(ch).trim().length() == 0) {
                // is space
                result[i] = ch;
                prevIsSpace = true;
            }
            else if (prevIsSpace) {
                result[i] = Character.toUpperCase(ch);
                prevIsSpace = false;
            }
            else {
                result[i] = Character.toLowerCase(ch);
                ;
            }
        }

        return new String(result);
    }

    public static String cleanFilename(String name) {
        name = name.replaceAll("[\\.\\(\\)\\[\\]\\-+=_,;]", " ").replaceAll("\\s\\s+", " ").trim();

        final Pattern editionPattern = Pattern.compile("(.*\\s)([a-zA-Z]+)\\sed(ition)?([\\s$].*)",
                Pattern.CASE_INSENSITIVE);
        final Matcher m = editionPattern.matcher(name);
        if (m.matches()) {
            final Integer edition = EDITION_MAP.get(m.group(2).toLowerCase());
            if (edition != null) {
                final String ordinal = edition == 1 ? "st" : (edition == 2 ? "nd" : (edition == 3 ? "rd" : "th"));
                name = m.group(1) + edition + ordinal + " Edition" + m.group(4);
            }
        }

        return name;
    }

    public static String makeCleanFilename(String name) {
        final String ext = name.lastIndexOf('.') > 0
                ? name.substring(name.lastIndexOf('.') + 1, name.length()).trim().toLowerCase()
                : null;
        name = name.replaceFirst("[.][^.]+$", "").trim();

        return capitalize(cleanFilename(name)) + "." + ext;
    }
}
