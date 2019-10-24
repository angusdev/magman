package org.ellab.magman;

import java.text.DateFormat;
import java.text.DecimalFormat;
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

    public static String guessDateFromFilename(String name, final FileItem.Type type) {
        // int[index, value, fromMonthName]
        int[] year = null;
        List<int[]> month = new ArrayList<>();
        List<int[]> day = new ArrayList<>();
        List<int[]> monthOrDay = new ArrayList<>();

        // insert space between number and word
        name = name.replaceAll("(?<=[A-Za-z])(?=[0-9])|(?<=[0-9])(?=[A-Za-z])", " ");

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

        if (year == null || (month.size() + day.size() + monthOrDay.size()) == 0) {
            // no year or month, not a valid date
            return name;
        }

        if (month.size() == 0) {
            // no month, must be monthOrDay

            if (monthOrDay.size() == 0) {
                // no monthOrDay, not match
                return name;
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
                result[i] = Character.toLowerCase(ch);;
            }
        }
        
        return new String(result);
    }
}
