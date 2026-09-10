package com.expensetracker.monthly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.expensetracker.monthly.data.model.MonthlySpendBarData;

import org.junit.Test;

import java.util.Calendar;

public class MonthlySpendBarDataTest {

    @Test
    public void testMonthlySpendBarDataGetters() {
        Calendar cal = Calendar.getInstance();
        MonthlySpendBarData data = new MonthlySpendBarData("Mar", "March 2026", 15420.50, true, cal);

        assertEquals("Mar", data.getShortMonthLabel());
        assertEquals("March 2026", data.getFullMonthLabel());
        assertEquals(15420.50, data.getTotalSpend(), 0.001);
        assertTrue(data.isCurrentSelection());
        assertEquals(cal, data.getMonthCalendar());
    }
}
