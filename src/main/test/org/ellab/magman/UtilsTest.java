package org.ellab.magman;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void testStrToMonth() {
        assertEquals(-1, Utils.strToMonth(null));
        assertEquals(-1, Utils.strToMonth(""));
        assertEquals(-1, Utils.strToMonth("1"));

        assertEquals(1, Utils.strToMonth("jan"));
        assertEquals(1, Utils.strToMonth("january"));
        assertEquals(1, Utils.strToMonth("Jan"));
        assertEquals(1, Utils.strToMonth("JAN"));
        assertEquals(1, Utils.strToMonth("JANU"));
        assertEquals(2, Utils.strToMonth("feb"));
        assertEquals(2, Utils.strToMonth("february"));
        assertEquals(3, Utils.strToMonth("mar"));
        assertEquals(3, Utils.strToMonth("march"));
        assertEquals(4, Utils.strToMonth("apr"));
        assertEquals(4, Utils.strToMonth("april"));
        assertEquals(5, Utils.strToMonth("may"));
        assertEquals(6, Utils.strToMonth("jun"));
        assertEquals(6, Utils.strToMonth("june"));
        assertEquals(7, Utils.strToMonth("jul"));
        assertEquals(7, Utils.strToMonth("July"));
        assertEquals(8, Utils.strToMonth("aug"));
        assertEquals(8, Utils.strToMonth("august"));
        assertEquals(9, Utils.strToMonth("sep"));
        assertEquals(9, Utils.strToMonth("sept"));
        assertEquals(9, Utils.strToMonth("september"));
        assertEquals(10, Utils.strToMonth("oct"));
        assertEquals(10, Utils.strToMonth("october"));
        assertEquals(11, Utils.strToMonth("nov"));
        assertEquals(11, Utils.strToMonth("november"));
        assertEquals(12, Utils.strToMonth("dec"));
        assertEquals(12, Utils.strToMonth("december"));
    }

    @Test
    public void testGuessDateFromFilename() {
        assertEquals("20191011", Utils.guessDateFromFilename("20191011", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("2019 Oct 11", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("2019 October 11", FileItem.Type.Weekly));
        assertEquals("20191008", Utils.guessDateFromFilename("8 Oct 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11 Oct 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11 October 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11Oct2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11 2019 Oct", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11 2019 October", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("Oct 11 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("October 11 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("2019 10 11", FileItem.Type.Weekly));
        assertEquals("20191021", Utils.guessDateFromFilename("2019 10 21", FileItem.Type.Weekly));
        assertEquals("20191008", Utils.guessDateFromFilename("8 10 2019", FileItem.Type.Weekly));
        assertEquals("20191021", Utils.guessDateFromFilename("21 10 2019", FileItem.Type.Weekly));
        assertEquals("20190231", Utils.guessDateFromFilename("31 02 2019", FileItem.Type.Weekly));

        assertEquals("201910", Utils.guessDateFromFilename("October 2019", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("2019 October", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("8 Oct 2019", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("11 Oct 2019", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("11 October 2019", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("11Oct2019", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("8 10 2019", FileItem.Type.Monthly));

        assertEquals("201910-11", Utils.guessDateFromFilename("Oct Nov 2019", FileItem.Type.Monthly));
        assertEquals("201910-11", Utils.guessDateFromFilename("11 Oct Nov 2019", FileItem.Type.Monthly));

        assertEquals("2019Q1", Utils.guessDateFromFilename("1 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q1", Utils.guessDateFromFilename("2 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q1", Utils.guessDateFromFilename("3 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q2", Utils.guessDateFromFilename("4 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q2", Utils.guessDateFromFilename("5 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q2", Utils.guessDateFromFilename("6 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q3", Utils.guessDateFromFilename("7 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q3", Utils.guessDateFromFilename("8 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q3", Utils.guessDateFromFilename("9 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q4", Utils.guessDateFromFilename("10 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q4", Utils.guessDateFromFilename("11 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q4", Utils.guessDateFromFilename("12 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q1", Utils.guessDateFromFilename("11 Jan 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q1", Utils.guessDateFromFilename("11 Mar 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q2", Utils.guessDateFromFilename("11 Apr 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q2", Utils.guessDateFromFilename("11 Jun 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q3", Utils.guessDateFromFilename("11 Jul 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q3", Utils.guessDateFromFilename("11 Sep 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q4", Utils.guessDateFromFilename("11 Oct 2019", FileItem.Type.Quarterly));
        assertEquals("2019Q4", Utils.guessDateFromFilename("11 Dec 2019", FileItem.Type.Quarterly));
    }

    private String helperTestFuzzyIndexOf(String str, String match) {
        int[] pos = Utils.fuzzyIndexOf(str, match);
        if (pos == null) {
            return null;
        }
        else {
            return str.substring(pos[0], pos[1]);
        }
    }

    @Test
    public void testFuzzyIndexOf() {
        assertNull(helperTestFuzzyIndexOf("abcd", "abc"));
        assertNull(helperTestFuzzyIndexOf("abc", "abcd"));

        assertEquals("abc", helperTestFuzzyIndexOf("abc", "abc"));
        assertEquals("abc", helperTestFuzzyIndexOf("x abc", "abc"));
        assertEquals("abc", helperTestFuzzyIndexOf("abc y", "abc"));
        assertEquals("abc", helperTestFuzzyIndexOf("x abc y", "abc"));

        assertEquals("abc", helperTestFuzzyIndexOf("abc def", "abc"));
        assertEquals("def", helperTestFuzzyIndexOf("abc def", "def"));
        assertEquals("abc def", helperTestFuzzyIndexOf("abc def", "abc def"));
        assertEquals("abc def", helperTestFuzzyIndexOf("x abc def", "abc def"));
        assertEquals("abc def", helperTestFuzzyIndexOf("abc def y", "abc def"));
        assertEquals("abc def", helperTestFuzzyIndexOf("x abc def y", "abc def"));
        assertEquals("def abc", helperTestFuzzyIndexOf("def abc", "abc def"));
        assertEquals("def abc", helperTestFuzzyIndexOf("x def abc", "abc def"));
        assertEquals("def abc", helperTestFuzzyIndexOf("def abc y", "abc def"));
        assertEquals("def abc", helperTestFuzzyIndexOf("x def abc y", "abc def"));

        assertEquals("two one three", helperTestFuzzyIndexOf("two one three", "one two three"));
        assertEquals("one three two", helperTestFuzzyIndexOf("one three two", "one two three"));
        assertNull(helperTestFuzzyIndexOf("three two one", "one two three"));

        assertEquals("two one", helperTestFuzzyIndexOf("two one three four", "one two"));
        assertEquals("one two", helperTestFuzzyIndexOf("one two three four", "one two"));
        assertEquals("three two", helperTestFuzzyIndexOf("one three two four", "two three"));
        assertEquals("two three", helperTestFuzzyIndexOf("one two three four", "two three"));
        assertEquals("four three", helperTestFuzzyIndexOf("one two four three", "three four"));
        assertEquals("three four", helperTestFuzzyIndexOf("one two three four", "three four"));
        assertNull(helperTestFuzzyIndexOf("four one two three", "three four"));

        assertEquals("two and three", helperTestFuzzyIndexOf("one two and three four", "two and three"));
        assertEquals("two and three", helperTestFuzzyIndexOf("one two and three four", "two & three"));
        assertEquals("two and three", helperTestFuzzyIndexOf("one two and three four", "two and three"));
        assertEquals("two and three", helperTestFuzzyIndexOf("one two and three four", "two & three"));
        assertEquals("two & three", helperTestFuzzyIndexOf("one two & three four", "two and three"));
        assertEquals("two & three", helperTestFuzzyIndexOf("one two & three four", "two & three"));
        assertEquals("two & three", helperTestFuzzyIndexOf("one two & three four", "two and three"));
        assertEquals("two & three", helperTestFuzzyIndexOf("one two & three four", "two & three"));
        
        assertEquals("onetwo", helperTestFuzzyIndexOf("onetwo", "one two"));
        assertEquals("twothree", helperTestFuzzyIndexOf("one twothree four", "two three"));
        assertNull(helperTestFuzzyIndexOf("onetwothreefour", "two three"));
        
    }

}
