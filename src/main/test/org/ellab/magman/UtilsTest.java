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
        assertEquals("20191011", Utils.guessDateFromFilename("11 Oct 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11 October 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11 2019 Oct", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("11 2019 October", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("Oct 11 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("October 11 2019", FileItem.Type.Weekly));
        assertEquals("20191011", Utils.guessDateFromFilename("2019 10 11", FileItem.Type.Weekly));
        assertEquals("20191021", Utils.guessDateFromFilename("2019 10 21", FileItem.Type.Weekly));
        assertEquals("20191021", Utils.guessDateFromFilename("21 10 2019", FileItem.Type.Weekly));

        assertEquals("201910", Utils.guessDateFromFilename("October 2019", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("2019 October", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("11 Oct 2019", FileItem.Type.Monthly));
        assertEquals("201910", Utils.guessDateFromFilename("11 October 2019", FileItem.Type.Monthly));
        
    }

}
