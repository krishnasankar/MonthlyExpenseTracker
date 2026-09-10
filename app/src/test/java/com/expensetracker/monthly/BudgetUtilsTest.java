package com.expensetracker.monthly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.expensetracker.monthly.util.BudgetUtils;
import com.expensetracker.monthly.util.DateUtils;

import org.junit.Test;

import java.util.Calendar;

public class BudgetUtilsTest {

    @Test
    public void testCalculateRemaining() {
        double remainingUnder = BudgetUtils.calculateRemaining(50000.0, 32000.0);
        assertEquals(18000.0, remainingUnder, 0.001);

        double remainingExact = BudgetUtils.calculateRemaining(25000.0, 25000.0);
        assertEquals(0.0, remainingExact, 0.001);

        double remainingOver = BudgetUtils.calculateRemaining(20000.0, 24500.0);
        assertEquals(-4500.0, remainingOver, 0.001);
    }

    @Test
    public void testCalculatePercentage() {
        assertEquals(0, BudgetUtils.calculatePercentage(0.0, 500.0));
        assertEquals(0, BudgetUtils.calculatePercentage(50000.0, 0.0));
        assertEquals(50, BudgetUtils.calculatePercentage(50000.0, 25000.0));
        assertEquals(100, BudgetUtils.calculatePercentage(50000.0, 50000.0));
        assertEquals(100, BudgetUtils.calculatePercentage(50000.0, 65000.0)); // Capped at 100
    }

    @Test
    public void testCalculateDailySafeSpendWithRemaining() {
        Calendar currentMonth = Calendar.getInstance();
        int daysRemaining = BudgetUtils.getDaysRemainingInMonth(currentMonth);
        assertTrue("Days remaining in current month must be >= 1", daysRemaining >= 1);

        double remainingBudget = 30000.0;
        double dailySafe = BudgetUtils.calculateDailySafeSpend(remainingBudget, currentMonth);
        assertEquals(remainingBudget / daysRemaining, dailySafe, 0.001);
    }

    @Test
    public void testCalculateDailySafeSpendExhaustedOrOver() {
        Calendar currentMonth = Calendar.getInstance();
        assertEquals(0.0, BudgetUtils.calculateDailySafeSpend(0.0, currentMonth), 0.001);
        assertEquals(0.0, BudgetUtils.calculateDailySafeSpend(-500.0, currentMonth), 0.001);
    }

    @Test
    public void testPastMonthSafeSpendReturnsZero() {
        Calendar pastMonth = Calendar.getInstance();
        pastMonth.add(Calendar.MONTH, -2);
        assertEquals(0.0, BudgetUtils.calculateDailySafeSpend(10000.0, pastMonth), 0.001);
        assertEquals(0, BudgetUtils.getDaysRemainingInMonth(pastMonth));
    }

    @Test
    public void testCategoryBudgetCalculations() {
        // Category with budget = 5000, spend = 3200 (64% spent, 1800 remaining)
        double budget = 5000.0;
        double spent = 3200.0;
        double remaining = BudgetUtils.calculateRemaining(budget, spent);
        assertEquals(1800.0, remaining, 0.001);

        double pct = (spent / budget) * 100.0;
        assertEquals(64.0, pct, 0.001);

        // Category over budget: budget = 2000, spend = 2500 (125% spent, -500 remaining)
        double overBudget = 2000.0;
        double overSpent = 2500.0;
        double overRemaining = BudgetUtils.calculateRemaining(overBudget, overSpent);
        assertEquals(-500.0, overRemaining, 0.001);

        double overPct = (overSpent / overBudget) * 100.0;
        assertEquals(125.0, overPct, 0.001);
    }
}
