package com.expensetracker.monthly.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.expensetracker.monthly.data.dao.CategoryDao;
import com.expensetracker.monthly.data.dao.ExpenseDao;
import com.expensetracker.monthly.data.dao.SubcategoryDao;
import com.expensetracker.monthly.data.database.AppDatabase;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Expense;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.data.model.CategoryWithSubcategories;
import com.expensetracker.monthly.data.model.ExpenseAutofillSuggestion;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;

import java.util.Calendar;
import java.util.List;

public class ExpenseRepository {

    private final CategoryDao categoryDao;
    private final SubcategoryDao subcategoryDao;
    private final ExpenseDao expenseDao;
    private final AppDatabase database;

    public ExpenseRepository(Application application) {
        database = AppDatabase.getInstance(application);
        categoryDao = database.categoryDao();
        subcategoryDao = database.subcategoryDao();
        expenseDao = database.expenseDao();
    }

    // --- Category Operations ---

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategoriesLive();
    }

    public LiveData<List<CategoryWithSubcategories>> getCategoriesWithSubcategories() {
        return categoryDao.getCategoriesWithSubcategoriesLive();
    }

    public void insertCategory(Category category, OnCategoryInsertedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = categoryDao.insert(category);
            if (listener != null) {
                listener.onInserted(id);
            }
        });
    }

    public void deleteCategory(long categoryId, OnDeleteCheckListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = expenseDao.countExpensesByCategoryId(categoryId);
            if (count > 0) {
                if (listener != null) {
                    listener.onResult(false, count);
                }
            } else {
                categoryDao.deleteById(categoryId);
                if (listener != null) {
                    listener.onResult(true, 0);
                }
            }
        });
    }

    // --- Subcategory Operations ---

    public LiveData<List<Subcategory>> getSubcategoriesForCategory(long categoryId) {
        return subcategoryDao.getSubcategoriesByCategoryIdLive(categoryId);
    }

    public void insertSubcategory(Subcategory subcategory, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            subcategoryDao.insert(subcategory);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteSubcategory(long subcategoryId, OnDeleteCheckListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = expenseDao.countExpensesBySubcategoryId(subcategoryId);
            if (count > 0) {
                if (listener != null) {
                    listener.onResult(false, count);
                }
            } else {
                subcategoryDao.deleteById(subcategoryId);
                if (listener != null) {
                    listener.onResult(true, 0);
                }
            }
        });
    }

    // --- Expense Operations ---

    public LiveData<List<ExpenseWithDetails>> getExpensesForMonth(long startMillis, long endMillis) {
        return expenseDao.getExpensesForDateRangeLive(startMillis, endMillis);
    }

    public LiveData<List<ExpenseWithDetails>> getRecentExpensesForMonth(long startMillis, long endMillis, int limit) {
        return expenseDao.getRecentExpensesForDateRangeLive(startMillis, endMillis, limit);
    }

    public LiveData<List<ExpenseWithDetails>> searchExpenses(long startMillis, long endMillis, String query) {
        return expenseDao.searchExpensesLive(startMillis, endMillis, query);
    }

    public LiveData<List<CategorySpendSummary>> getMonthlyCategorySpend(long startMillis, long endMillis) {
        return expenseDao.getMonthlyCategorySpendLive(startMillis, endMillis);
    }

    public LiveData<Double> getTotalSpendForMonth(long startMillis, long endMillis) {
        return expenseDao.getTotalSpendForDateRangeLive(startMillis, endMillis);
    }

    public LiveData<Integer> getExpenseCountForMonth(long startMillis, long endMillis) {
        return expenseDao.getExpenseCountForDateRangeLive(startMillis, endMillis);
    }

    public void insertExpense(Expense expense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.insert(expense);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void updateExpense(Expense expense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.update(expense);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteExpense(Expense expense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.delete(expense);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteExpenseById(long id, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.deleteById(id);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public LiveData<List<ExpenseAutofillSuggestion>> getExpenseAutofillSuggestions() {
        return expenseDao.getExpenseAutofillSuggestionsLive();
    }

    public interface OnCategoryInsertedListener {
        void onInserted(long categoryId);
    }

    public interface OnDeleteCheckListener {
        void onResult(boolean deleted, int activeExpenseCount);
    }
}
