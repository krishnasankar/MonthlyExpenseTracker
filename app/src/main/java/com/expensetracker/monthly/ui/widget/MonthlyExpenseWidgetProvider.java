package com.expensetracker.monthly.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.dao.ExpenseDao;
import com.expensetracker.monthly.data.database.AppDatabase;
import com.expensetracker.monthly.ui.MainActivity;
import com.expensetracker.monthly.util.BudgetUtils;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.DateUtils;

import java.util.Calendar;

public class MonthlyExpenseWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateAllWidgets(context);
    }

    public static void updateAllWidgets(Context context) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
                ComponentName thisWidget = new ComponentName(appContext, MonthlyExpenseWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                if (appWidgetIds == null || appWidgetIds.length == 0) {
                    return;
                }

                Calendar now = Calendar.getInstance();
                long start = DateUtils.getStartOfMonthMillis(now);
                long end = DateUtils.getEndOfMonthMillis(now);

                ExpenseDao dao = AppDatabase.getInstance(appContext).expenseDao();
                double totalSpend = dao.getTotalSpendForDateRangeSync(start, end);
                int count = dao.getExpenseCountForDateRangeSync(start, end);

                String currencySymbol = CurrencyUtils.getCurrencySymbol(appContext);
                double budget = BudgetUtils.getMonthlyBudget(appContext);

                RemoteViews views = new RemoteViews(appContext.getPackageName(), R.layout.widget_monthly_expense);

                views.setTextViewText(R.id.tv_widget_month, DateUtils.formatMonthYear(now));
                views.setTextViewText(R.id.tv_widget_total, CurrencyUtils.formatAmount(totalSpend, currencySymbol));

                if (budget > 0.0) {
                    double remaining = BudgetUtils.calculateRemaining(budget, totalSpend);
                    int percent = BudgetUtils.calculatePercentage(budget, totalSpend);
                    views.setProgressBar(R.id.progress_widget_budget, 100, Math.min(100, percent), false);

                    int daysLeft = BudgetUtils.getDaysRemainingInMonth(now);
                    if (remaining >= 0) {
                        views.setTextViewText(R.id.tv_widget_budget_status,
                                CurrencyUtils.formatAmount(remaining, currencySymbol) + " left • " + daysLeft + "d remaining");
                    } else {
                        views.setTextViewText(R.id.tv_widget_budget_status,
                                "Over budget by " + CurrencyUtils.formatAmount(Math.abs(remaining), currencySymbol));
                    }
                } else {
                    views.setProgressBar(R.id.progress_widget_budget, 100, 0, false);
                    views.setTextViewText(R.id.tv_widget_budget_status, count + " transaction" + (count == 1 ? "" : "s") + " this month");
                }

                // Click on widget body -> Open Dashboard in MainActivity
                Intent openAppIntent = new Intent(appContext, MainActivity.class);
                openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingOpen = PendingIntent.getActivity(
                        appContext,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                views.setOnClickPendingIntent(R.id.widget_root, pendingOpen);

                // Click on '+' Quick Add button -> Open MainActivity with extra to show Add Expense dialog
                Intent addExpenseIntent = new Intent(appContext, MainActivity.class);
                addExpenseIntent.setAction(MainActivity.ACTION_OPEN_ADD_EXPENSE);
                addExpenseIntent.putExtra(MainActivity.EXTRA_OPEN_ADD_EXPENSE, true);
                addExpenseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingAdd = PendingIntent.getActivity(
                        appContext,
                        1,
                        addExpenseIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                views.setOnClickPendingIntent(R.id.btn_widget_add, pendingAdd);

                for (int widgetId : appWidgetIds) {
                    appWidgetManager.updateAppWidget(widgetId, views);
                }
            } catch (Exception ignored) {
            }
        });
    }
}
