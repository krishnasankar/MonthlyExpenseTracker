package com.expensetracker.monthly.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class BudgetUtils {

    private static final String PREF_NAME = "expense_tracker_prefs";
    private static final String KEY_MONTHLY_BUDGET = "monthly_budget_amount";
    public static final double NO_BUDGET = 0.0;

    public static double getMonthlyBudget(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_MONTHLY_BUDGET, (float) NO_BUDGET);
    }

    public static void setMonthlyBudget(Context context, double budget) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, (float) Math.max(0.0, budget)).apply();
    }

    public static void clearMonthlyBudget(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_MONTHLY_BUDGET).apply();
    }

    public static boolean hasMonthlyBudget(Context context) {
        return getMonthlyBudget(context) > 0.0;
    }

    /**
     * Calculates remaining budget. Positive means under budget, negative means over budget.
     */
    public static double calculateRemaining(double budget, double totalSpend) {
        return budget - totalSpend;
    }

    /**
     * Calculates spending percentage (0 - 100+).
     */
    public static int calculatePercentage(double budget, double totalSpend) {
        if (budget <= 0.0) return 0;
        return (int) Math.min(100, Math.round((totalSpend / budget) * 100.0));
    }

    /**
     * Calculates the safe daily spend limit based on remaining budget and days left in the month.
     * If the month is in the past, or no days left, returns remaining (or 0 if exhausted).
     * If budget is already exceeded, returns 0.0.
     */
    public static double calculateDailySafeSpend(double remainingBudget, Calendar monthCalendar) {
        if (remainingBudget <= 0.0 || monthCalendar == null) {
            return 0.0;
        }

        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);
        int monthYear = monthCalendar.get(Calendar.YEAR);
        int targetMonth = monthCalendar.get(Calendar.MONTH);

        // If checking a past month
        if (monthYear < nowYear || (monthYear == nowYear && targetMonth < nowMonth)) {
            return 0.0;
        }

        // If checking a future month, entire month's days are available
        if (monthYear > nowYear || (monthYear == nowYear && targetMonth > nowMonth)) {
            int totalDays = DateUtils.getDaysInMonth(monthCalendar);
            return totalDays > 0 ? (remainingBudget / totalDays) : 0.0;
        }

        // Current month: days remaining including today
        int totalDays = DateUtils.getDaysInMonth(now);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int daysRemaining = Math.max(1, totalDays - currentDay + 1);

        return remainingBudget / daysRemaining;
    }

    /**
     * Returns the number of days remaining in the month (including today).
     */
    public static int getDaysRemainingInMonth(Calendar monthCalendar) {
        if (monthCalendar == null) return 0;

        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);
        int monthYear = monthCalendar.get(Calendar.YEAR);
        int targetMonth = monthCalendar.get(Calendar.MONTH);

        if (monthYear < nowYear || (monthYear == nowYear && targetMonth < nowMonth)) {
            return 0;
        }

        if (monthYear > nowYear || (monthYear == nowYear && targetMonth > nowMonth)) {
            return DateUtils.getDaysInMonth(monthCalendar);
        }

        int totalDays = DateUtils.getDaysInMonth(now);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        return Math.max(1, totalDays - currentDay + 1);
    }
}
