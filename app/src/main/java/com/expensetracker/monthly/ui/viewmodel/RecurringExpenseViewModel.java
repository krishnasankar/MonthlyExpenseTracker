package com.expensetracker.monthly.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.data.model.RecurringExpenseWithDetails;
import com.expensetracker.monthly.data.repository.ExpenseRepository;

import java.util.List;

public class RecurringExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final LiveData<List<RecurringExpenseWithDetails>> allRecurring;

    public RecurringExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allRecurring = repository.getAllRecurringExpenses();
    }

    public LiveData<List<RecurringExpenseWithDetails>> getAllRecurringExpenses() {
        return allRecurring;
    }

    public void insert(RecurringExpense item, Runnable onComplete) {
        repository.insertRecurringExpense(item, onComplete);
    }

    public void insertRecurringExpense(RecurringExpense item, Runnable onComplete) {
        insert(item, onComplete);
    }

    public void update(RecurringExpense item, Runnable onComplete) {
        repository.updateRecurringExpense(item, onComplete);
    }

    public void updateRecurringExpense(RecurringExpense item, Runnable onComplete) {
        update(item, onComplete);
    }

    public void delete(RecurringExpense item, Runnable onComplete) {
        repository.deleteRecurringExpense(item, onComplete);
    }

    public void deleteRecurringExpense(RecurringExpense item, Runnable onComplete) {
        delete(item, onComplete);
    }

    public void setActive(long id, boolean isActive, Runnable onComplete) {
        repository.setRecurringExpenseActive(id, isActive, onComplete);
    }

    public void setRecurringExpenseActive(long id, boolean isActive, Runnable onComplete) {
        setActive(id, isActive, onComplete);
    }
}
