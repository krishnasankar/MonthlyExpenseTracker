package com.expensetracker.monthly.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;
import com.expensetracker.monthly.data.repository.ExpenseRepository;
import com.expensetracker.monthly.util.DateUtils;

import java.util.Calendar;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final MutableLiveData<Calendar> selectedMonth = new MutableLiveData<>();

    private final LiveData<Double> totalSpend;
    private final LiveData<Integer> expenseCount;
    private final LiveData<List<CategorySpendSummary>> categorySpend;
    private final LiveData<List<ExpenseWithDetails>> recentExpenses;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);

        // Default to current month
        Calendar current = Calendar.getInstance();
        current.set(Calendar.DAY_OF_MONTH, 1);
        selectedMonth.setValue(current);
        refreshBudget();

        totalSpend = Transformations.switchMap(selectedMonth, month -> {
            long start = DateUtils.getStartOfMonthMillis(month);
            long end = DateUtils.getEndOfMonthMillis(month);
            return repository.getTotalSpendForMonth(start, end);
        });

        expenseCount = Transformations.switchMap(selectedMonth, month -> {
            long start = DateUtils.getStartOfMonthMillis(month);
            long end = DateUtils.getEndOfMonthMillis(month);
            return repository.getExpenseCountForMonth(start, end);
        });

        categorySpend = Transformations.switchMap(selectedMonth, month -> {
            long start = DateUtils.getStartOfMonthMillis(month);
            long end = DateUtils.getEndOfMonthMillis(month);
            return repository.getMonthlyCategorySpend(start, end);
        });

        recentExpenses = Transformations.switchMap(selectedMonth, month -> {
            long start = DateUtils.getStartOfMonthMillis(month);
            long end = DateUtils.getEndOfMonthMillis(month);
            return repository.getRecentExpensesForMonth(start, end, 5);
        });
    }

    public LiveData<Calendar> getSelectedMonth() {
        return selectedMonth;
    }

    public void previousMonth() {
        Calendar cal = selectedMonth.getValue();
        if (cal != null) {
            Calendar newCal = (Calendar) cal.clone();
            newCal.add(Calendar.MONTH, -1);
            selectedMonth.setValue(newCal);
        }
    }

    public void nextMonth() {
        Calendar cal = selectedMonth.getValue();
        if (cal != null) {
            Calendar newCal = (Calendar) cal.clone();
            newCal.add(Calendar.MONTH, 1);
            selectedMonth.setValue(newCal);
        }
    }

    public void setMonth(int year, int monthOfYear) {
        Calendar newCal = Calendar.getInstance();
        newCal.set(Calendar.YEAR, year);
        newCal.set(Calendar.MONTH, monthOfYear);
        newCal.set(Calendar.DAY_OF_MONTH, 1);
        selectedMonth.setValue(newCal);
    }

    public LiveData<Double> getTotalSpend() {
        return totalSpend;
    }

    public LiveData<Integer> getExpenseCount() {
        return expenseCount;
    }

    public LiveData<List<CategorySpendSummary>> getCategorySpend() {
        return categorySpend;
    }

    public LiveData<List<ExpenseWithDetails>> getRecentExpenses() {
        return recentExpenses;
    }

    private final MutableLiveData<Double> monthlyBudget = new MutableLiveData<>();

    public LiveData<Double> getMonthlyBudget() {
        return monthlyBudget;
    }

    public void refreshBudget() {
        double budget = com.expensetracker.monthly.util.BudgetUtils.getMonthlyBudget(getApplication());
        monthlyBudget.setValue(budget);
    }

    public void setMonthlyBudget(double budget) {
        com.expensetracker.monthly.util.BudgetUtils.setMonthlyBudget(getApplication(), budget);
        monthlyBudget.setValue(budget);
    }

    public void clearMonthlyBudget() {
        com.expensetracker.monthly.util.BudgetUtils.clearMonthlyBudget(getApplication());
        monthlyBudget.setValue(0.0);
    }
}
