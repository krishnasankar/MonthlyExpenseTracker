package com.expensetracker.monthly.data.model;

import java.util.Calendar;

public class MonthlySpendBarData {
    private final String shortMonthLabel;
    private final String fullMonthLabel;
    private final double totalSpend;
    private final boolean isCurrentSelection;
    private final Calendar monthCalendar;

    public MonthlySpendBarData(String shortMonthLabel, String fullMonthLabel, double totalSpend, boolean isCurrentSelection, Calendar monthCalendar) {
        this.shortMonthLabel = shortMonthLabel;
        this.fullMonthLabel = fullMonthLabel;
        this.totalSpend = totalSpend;
        this.isCurrentSelection = isCurrentSelection;
        this.monthCalendar = monthCalendar;
    }

    public String getShortMonthLabel() {
        return shortMonthLabel;
    }

    public String getFullMonthLabel() {
        return fullMonthLabel;
    }

    public double getTotalSpend() {
        return totalSpend;
    }

    public boolean isCurrentSelection() {
        return isCurrentSelection;
    }

    public Calendar getMonthCalendar() {
        return monthCalendar;
    }
}
