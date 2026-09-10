package com.expensetracker.monthly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.util.RecurringExpenseManager;

import org.junit.Test;

import java.util.Calendar;

public class RecurringExpenseManagerTest {

    @Test
    public void testDueTodayOrPastInCycleEligible() {
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        // Due on the 1st of month (always past or today in any month)
        RecurringExpense recurring = new RecurringExpense(
                "Netflix", 649.0, 1L, null, "MONTHLY", 1, 0L, true, null);

        assertTrue(RecurringExpenseManager.shouldLogThisCycle(recurring, today));
    }

    @Test
    public void testInactiveNotEligible() {
        Calendar today = Calendar.getInstance();
        RecurringExpense recurring = new RecurringExpense(
                "Gym", 2000.0, 1L, null, "MONTHLY", 1, 0L, false, null);

        assertFalse(RecurringExpenseManager.shouldLogThisCycle(recurring, today));
    }

    @Test
    public void testAlreadyLoggedThisMonthNotEligible() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, 15);

        // Last logged on 1st of this month
        Calendar loggedCal = (Calendar) today.clone();
        loggedCal.set(Calendar.DAY_OF_MONTH, 1);

        RecurringExpense recurring = new RecurringExpense(
                "Rent", 25000.0, 1L, null, "MONTHLY", 5, loggedCal.getTimeInMillis(), true, null);

        assertFalse(RecurringExpenseManager.shouldLogThisCycle(recurring, today));
    }

    @Test
    public void testLoggedPreviousMonthIsEligible() {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);

        Calendar prevMonth = (Calendar) today.clone();
        prevMonth.add(Calendar.MONTH, -1);
        prevMonth.set(Calendar.DAY_OF_MONTH, 1);

        RecurringExpense recurring = new RecurringExpense(
                "Electricity", 1200.0, 1L, null, "MONTHLY", 1, prevMonth.getTimeInMillis(), true, null);

        assertTrue(RecurringExpenseManager.shouldLogThisCycle(recurring, today));
    }

    @Test
    public void testFrequencyDisplayAndParsing() {
        assertEquals("Monthly", RecurringExpense.getFrequencyDisplayName("MONTHLY"));
        assertEquals("Weekly", RecurringExpense.getFrequencyDisplayName("WEEKLY"));
        assertEquals("Yearly", RecurringExpense.getFrequencyDisplayName("YEARLY"));
        assertEquals("Monthly", RecurringExpense.getFrequencyDisplayName(null));

        assertEquals(RecurringExpense.FREQUENCY_MONTHLY, RecurringExpense.parseFrequencyFromDisplay("Monthly"));
        assertEquals(RecurringExpense.FREQUENCY_WEEKLY, RecurringExpense.parseFrequencyFromDisplay("Weekly"));
        assertEquals(RecurringExpense.FREQUENCY_YEARLY, RecurringExpense.parseFrequencyFromDisplay("Yearly"));
        assertEquals(RecurringExpense.FREQUENCY_MONTHLY, RecurringExpense.parseFrequencyFromDisplay("MONTHLY"));
        assertEquals(RecurringExpense.FREQUENCY_MONTHLY, RecurringExpense.parseFrequencyFromDisplay(null));
    }
}
