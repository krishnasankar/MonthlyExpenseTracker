package com.expensetracker.monthly;

import static org.junit.Assert.assertEquals;

import com.expensetracker.monthly.util.ExportUtils;

import org.junit.Test;

public class ExportUtilsTest {

    @Test
    public void testEscapeCsvNormal() {
        assertEquals("Groceries", ExportUtils.escapeCsv("Groceries"));
        assertEquals("", ExportUtils.escapeCsv(null));
        assertEquals("", ExportUtils.escapeCsv(""));
    }

    @Test
    public void testEscapeCsvWithComma() {
        assertEquals("\"Apples, Bananas & Oranges\"", ExportUtils.escapeCsv("Apples, Bananas & Oranges"));
    }

    @Test
    public void testEscapeCsvWithQuotes() {
        assertEquals("\"Special \"\"Deluxe\"\" Item\"", ExportUtils.escapeCsv("Special \"Deluxe\" Item"));
    }

    @Test
    public void testEscapeCsvWithNewline() {
        assertEquals("\"Line 1\nLine 2\"", ExportUtils.escapeCsv("Line 1\nLine 2"));
    }
}
