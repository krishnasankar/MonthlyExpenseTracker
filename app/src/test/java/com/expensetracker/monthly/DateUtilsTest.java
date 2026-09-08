package com.expensetracker.monthly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.DateUtils;

import org.junit.Test;

import java.util.Calendar;

public class DateUtilsTest {

    @Test
    public void testStartAndEndOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2026);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 15);

        long start = DateUtils.getStartOfMonthMillis(cal);
        long end = DateUtils.getEndOfMonthMillis(cal);

        assertTrue("End of month should be after start of month", end > start);

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(start);
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, startCal.get(Calendar.MINUTE));

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(end);
        assertEquals(30, endCal.get(Calendar.DAY_OF_MONTH)); // September has 30 days
        assertEquals(23, endCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, endCal.get(Calendar.MINUTE));
    }

    @Test
    public void testDaysInMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2026);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        // 2026 is not a leap year, February has 28 days
        assertEquals(28, DateUtils.getDaysInMonth(cal));

        cal.set(Calendar.MONTH, Calendar.JANUARY);
        assertEquals(31, DateUtils.getDaysInMonth(cal));
    }

    @Test
    public void testCurrencyFormatting() {
        String formatted = CurrencyUtils.formatAmount(1250.75, "$");
        assertEquals("$ 1,250.75", formatted);

        String zeroFormatted = CurrencyUtils.formatAmount(0.0, "€");
        assertEquals("€ 0.00", zeroFormatted);

        String inrFormatted = CurrencyUtils.formatAmount(9850.50, "₹");
        assertEquals("₹ 9,850.50", inrFormatted);
    }
}
