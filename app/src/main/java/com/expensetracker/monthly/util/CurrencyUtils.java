package com.expensetracker.monthly.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final String PREF_NAME = "expense_tracker_prefs";
    private static final String KEY_CURRENCY_SYMBOL = "currency_symbol";
    private static final String DEFAULT_SYMBOL = "$";

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,##0.00");

    public static String getCurrencySymbol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENCY_SYMBOL, DEFAULT_SYMBOL);
    }

    public static void setCurrencySymbol(Context context, String symbol) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply();
    }

    public static String formatAmount(double amount, String currencySymbol) {
        return currencySymbol + " " + AMOUNT_FORMAT.format(amount);
    }

    public static String formatAmount(double amount, Context context) {
        return formatAmount(amount, getCurrencySymbol(context));
    }
}
