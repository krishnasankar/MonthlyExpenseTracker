package com.expensetracker.monthly.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.expensetracker.monthly.R;
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

import com.expensetracker.monthly.data.dao.RecurringExpenseDao;
import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.data.model.RecurringExpenseWithDetails;

import java.util.Calendar;
import java.util.List;

public class ExpenseRepository {

    private final CategoryDao categoryDao;
    private final SubcategoryDao subcategoryDao;
    private final ExpenseDao expenseDao;
    private final RecurringExpenseDao recurringExpenseDao;
    private final AppDatabase database;
    private final Application application;

    public ExpenseRepository(Application application) {
        this.application = application;
        database = AppDatabase.getInstance(application);
        categoryDao = database.categoryDao();
        subcategoryDao = database.subcategoryDao();
        expenseDao = database.expenseDao();
        recurringExpenseDao = database.recurringExpenseDao();
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

    public void insertCategory(Category category, OnOperationResultListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = categoryDao.countCategoriesByName(category.getName().trim());
            if (count > 0) {
                if (listener != null) {
                    listener.onResult(false, application.getString(R.string.category_name_exists));
                }
                return;
            }
            try {
                long id = categoryDao.insert(category);
                category.setId(id);
                if (listener != null) {
                    listener.onResult(true, null);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onResult(false, e.getMessage());
                }
            }
        });
    }

    public void createCategory(Category category, OnEntityCreatedListener<Category> listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = categoryDao.countCategoriesByName(category.getName().trim());
            if (count > 0) {
                if (listener != null) {
                    listener.onCreated(null, false, application.getString(R.string.category_name_exists));
                }
                return;
            }
            try {
                long id = categoryDao.insert(category);
                category.setId(id);
                if (listener != null) {
                    listener.onCreated(category, true, null);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onCreated(null, false, e.getMessage());
                }
            }
        });
    }

    public void updateCategory(Category category, OnOperationResultListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int duplicateCount = categoryDao.countCategoriesByNameExcludingId(category.getName().trim(), category.getId());
            if (duplicateCount > 0) {
                if (listener != null) {
                    listener.onResult(false, application.getString(R.string.category_name_exists));
                }
                return;
            }
            try {
                categoryDao.update(category);
                if (listener != null) {
                    listener.onResult(true, null);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onResult(false, e.getMessage());
                }
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

    public void updateCategoryBudget(long categoryId, double budget, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.updateCategoryBudget(categoryId, Math.max(0.0, budget));
            if (onComplete != null) {
                onComplete.run();
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

    public void insertSubcategory(Subcategory subcategory, OnOperationResultListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = subcategoryDao.countSubcategoriesByName(subcategory.getCategoryId(), subcategory.getName().trim());
            if (count > 0) {
                if (listener != null) {
                    listener.onResult(false, application.getString(R.string.subcategory_name_exists));
                }
                return;
            }
            try {
                long id = subcategoryDao.insert(subcategory);
                subcategory.setId(id);
                if (listener != null) {
                    listener.onResult(true, null);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onResult(false, e.getMessage());
                }
            }
        });
    }

    public void createSubcategory(Subcategory subcategory, OnEntityCreatedListener<Subcategory> listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int count = subcategoryDao.countSubcategoriesByName(subcategory.getCategoryId(), subcategory.getName().trim());
            if (count > 0) {
                if (listener != null) {
                    listener.onCreated(null, false, application.getString(R.string.subcategory_name_exists));
                }
                return;
            }
            try {
                long id = subcategoryDao.insert(subcategory);
                subcategory.setId(id);
                if (listener != null) {
                    listener.onCreated(subcategory, true, null);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onCreated(null, false, e.getMessage());
                }
            }
        });
    }

    public void updateSubcategory(Subcategory subcategory, OnOperationResultListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int duplicateCount = subcategoryDao.countSubcategoriesByNameExcludingId(
                    subcategory.getCategoryId(),
                    subcategory.getName().trim(),
                    subcategory.getId()
            );
            if (duplicateCount > 0) {
                if (listener != null) {
                    listener.onResult(false, application.getString(R.string.subcategory_name_exists));
                }
                return;
            }
            try {
                subcategoryDao.update(subcategory);
                if (listener != null) {
                    listener.onResult(true, null);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onResult(false, e.getMessage());
                }
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
            com.expensetracker.monthly.ui.widget.MonthlyExpenseWidgetProvider.updateAllWidgets(application);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void updateExpense(Expense expense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.update(expense);
            com.expensetracker.monthly.ui.widget.MonthlyExpenseWidgetProvider.updateAllWidgets(application);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteExpense(Expense expense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.delete(expense);
            com.expensetracker.monthly.ui.widget.MonthlyExpenseWidgetProvider.updateAllWidgets(application);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteExpenseById(long id, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.deleteById(id);
            com.expensetracker.monthly.ui.widget.MonthlyExpenseWidgetProvider.updateAllWidgets(application);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public LiveData<List<ExpenseAutofillSuggestion>> getExpenseAutofillSuggestions() {
        return expenseDao.getExpenseAutofillSuggestionsLive();
    }

    // --- Recurring Expense Operations ---

    public LiveData<List<RecurringExpenseWithDetails>> getAllRecurringExpenses() {
        return recurringExpenseDao.getAllRecurringWithDetailsLive();
    }

    public void insertRecurringExpense(RecurringExpense recurringExpense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.insert(recurringExpense);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void updateRecurringExpense(RecurringExpense recurringExpense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.update(recurringExpense);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteRecurringExpense(RecurringExpense recurringExpense, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.delete(recurringExpense);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void setRecurringExpenseActive(long id, boolean isActive, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.updateActiveState(id, isActive);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public RecurringExpenseDao getRecurringExpenseDao() {
        return recurringExpenseDao;
    }

    // --- Export Operations ---

    public List<ExpenseWithDetails> getAllExpensesSync() {
        return expenseDao.getAllExpensesSync();
    }

    public List<ExpenseWithDetails> getExpensesForDateRangeSync(long startMillis, long endMillis) {
        return expenseDao.getExpensesForDateRangeSync(startMillis, endMillis);
    }

    public List<CategorySpendSummary> getMonthlyCategorySpendSync(long startMillis, long endMillis) {
        return expenseDao.getMonthlyCategorySpendSync(startMillis, endMillis);
    }

    public interface OnCategoryInsertedListener {
        void onInserted(long categoryId);
    }

    public interface OnDeleteCheckListener {
        void onResult(boolean deleted, int activeExpenseCount);
    }

    public interface OnOperationResultListener {
        void onResult(boolean success, String errorMessage);
    }

    public interface OnEntityCreatedListener<T> {
        void onCreated(T entity, boolean success, String errorMessage);
    }
}
