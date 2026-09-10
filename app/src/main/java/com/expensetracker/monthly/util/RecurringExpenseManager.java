package com.expensetracker.monthly.util;

import android.content.Context;

import com.expensetracker.monthly.data.database.AppDatabase;
import com.expensetracker.monthly.data.entity.Expense;
import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.data.model.RecurringExpenseWithDetails;
import com.expensetracker.monthly.ui.widget.MonthlyExpenseWidgetProvider;

import java.util.Calendar;
import java.util.List;

public class RecurringExpenseManager {

    /**
     * Determines whether a recurring expense should be auto-logged for the cycle containing the given calendar date.
     */
    public static boolean shouldLogThisCycle(RecurringExpense rec, Calendar now) {
        if (rec == null || !rec.isActive()) {
            return false;
        }

        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        int dueDay = rec.getDayOfMonth();
        if (currentDay < dueDay) {
            return false;
        }

        if (rec.getLastLoggedMillis() > 0) {
            Calendar lastLoggedCal = Calendar.getInstance();
            lastLoggedCal.setTimeInMillis(rec.getLastLoggedMillis());
            int lastYear = lastLoggedCal.get(Calendar.YEAR);
            int lastMonth = lastLoggedCal.get(Calendar.MONTH);
            if (lastYear == currentYear && lastMonth == currentMonth) {
                return false; // Already logged this month
            }
        }

        return true;
    }

    public static void checkAndProcessRecurringExpenses(Context context) {
        checkAndProcessRecurringExpenses(context, null);
    }

    /**
     * Inspects active recurring expenses and auto-logs them if their due date
     * for the current cycle has arrived and they have not yet been logged.
     */
    public static void checkAndProcessRecurringExpenses(Context context, Runnable onComplete) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                List<RecurringExpenseWithDetails> activeList = db.recurringExpenseDao().getActiveRecurringWithDetailsSync();

                if (activeList != null && !activeList.isEmpty()) {
                    Calendar now = Calendar.getInstance();
                    int currentYear = now.get(Calendar.YEAR);
                    int currentMonth = now.get(Calendar.MONTH);

                    boolean anyLogged = false;

                    for (RecurringExpenseWithDetails item : activeList) {
                        RecurringExpense rec = item.recurringExpense;
                        if (shouldLogThisCycle(rec, now)) {
                            int dueDay = rec.getDayOfMonth();
                            Calendar expenseDate = Calendar.getInstance();
                            expenseDate.set(Calendar.YEAR, currentYear);
                            expenseDate.set(Calendar.MONTH, currentMonth);
                            int maxDayInMonth = expenseDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                            expenseDate.set(Calendar.DAY_OF_MONTH, Math.min(dueDay, maxDayInMonth));
                            expenseDate.set(Calendar.HOUR_OF_DAY, 9);
                            expenseDate.set(Calendar.MINUTE, 0);
                            expenseDate.set(Calendar.SECOND, 0);

                            Expense expense = new Expense(
                                    rec.getTitle(),
                                    rec.getAmount(),
                                    expenseDate.getTimeInMillis(),
                                    rec.getCategoryId(),
                                    rec.getSubcategoryId(),
                                    "Auto-logged recurring commitment"
                            );

                            db.expenseDao().insert(expense);
                            db.recurringExpenseDao().updateLastLogged(rec.getId(), System.currentTimeMillis());
                            anyLogged = true;
                        }
                    }

                    if (anyLogged) {
                        MonthlyExpenseWidgetProvider.updateAllWidgets(appContext);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
}
