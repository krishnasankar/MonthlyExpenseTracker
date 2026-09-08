package com.expensetracker.monthly.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.expensetracker.monthly.data.entity.Expense;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;
import com.expensetracker.monthly.data.repository.ExpenseRepository;
import com.expensetracker.monthly.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;

    private final MutableLiveData<Calendar> selectedMonth = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Long> selectedCategoryFilter = new MutableLiveData<>(-1L);

    private final MediatorLiveData<List<ExpenseWithDetails>> expenses = new MediatorLiveData<>();
    private LiveData<List<ExpenseWithDetails>> currentSource = null;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);

        Calendar current = Calendar.getInstance();
        current.set(Calendar.DAY_OF_MONTH, 1);
        selectedMonth.setValue(current);

        expenses.addSource(selectedMonth, month -> reloadExpenses());
        expenses.addSource(searchQuery, query -> reloadExpenses());
        expenses.addSource(selectedCategoryFilter, catId -> reloadExpenses());
    }

    private void reloadExpenses() {
        Calendar month = selectedMonth.getValue();
        if (month == null) return;

        long start = DateUtils.getStartOfMonthMillis(month);
        long end = DateUtils.getEndOfMonthMillis(month);
        String query = searchQuery.getValue();
        Long filterCatId = selectedCategoryFilter.getValue();

        if (currentSource != null) {
            expenses.removeSource(currentSource);
        }

        if (query != null && !query.trim().isEmpty()) {
            currentSource = repository.searchExpenses(start, end, query.trim());
        } else {
            currentSource = repository.getExpensesForMonth(start, end);
        }

        expenses.addSource(currentSource, list -> {
            if (filterCatId != null && filterCatId > 0 && list != null) {
                List<ExpenseWithDetails> filtered = new ArrayList<>();
                for (ExpenseWithDetails item : list) {
                    if (item.expense.getCategoryId() == filterCatId) {
                        filtered.add(item);
                    }
                }
                expenses.setValue(filtered);
            } else {
                expenses.setValue(list);
            }
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

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setCategoryFilter(long categoryId) {
        selectedCategoryFilter.setValue(categoryId);
    }

    public LiveData<List<ExpenseWithDetails>> getExpenses() {
        return expenses;
    }

    public void insertExpense(Expense expense, Runnable onComplete) {
        repository.insertExpense(expense, onComplete);
    }

    public void updateExpense(Expense expense, Runnable onComplete) {
        repository.updateExpense(expense, onComplete);
    }

    public void deleteExpense(Expense expense, Runnable onComplete) {
        repository.deleteExpense(expense, onComplete);
    }

    public void deleteExpenseById(long id, Runnable onComplete) {
        repository.deleteExpenseById(id, onComplete);
    }
}
