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
import com.expensetracker.monthly.data.model.ExpenseWithDetails;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

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

    // --- Demo Data Helper ---

    public void seedDemoExpensesForMonth(Calendar monthCalendar, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CategoryWithSubcategories> cats = categoryDao.getCategoriesWithSubcategoriesSync();
            if (cats == null || cats.isEmpty()) {
                AppDatabase.seedDefaultCategories(database);
                cats = categoryDao.getCategoriesWithSubcategoriesSync();
            }

            if (cats != null && !cats.isEmpty()) {
                Random random = new Random();
                int year = monthCalendar.get(Calendar.YEAR);
                int month = monthCalendar.get(Calendar.MONTH);

                String[][] sampleItems = {
                    {"Supermarket Groceries", "45.80", "Food & Dining", "Groceries"},
                    {"Starbucks Latte", "5.75", "Food & Dining", "Coffee & Snacks"},
                    {"Italian Bistro Dinner", "68.20", "Food & Dining", "Restaurants"},
                    {"Gasoline Refill", "52.00", "Transportation", "Fuel / Gas"},
                    {"Subway Monthly Pass", "30.00", "Transportation", "Public Transit"},
                    {"Uber Ride Downtown", "18.50", "Transportation", "Taxi / Rideshare"},
                    {"Apartment Rent", "850.00", "Housing & Utilities", "Rent / Mortgage"},
                    {"Electric Bill", "74.30", "Housing & Utilities", "Electricity"},
                    {"High-Speed Internet", "59.99", "Housing & Utilities", "Internet & WiFi"},
                    {"Netflix Subscription", "15.99", "Entertainment", "Streaming & Subscriptions"},
                    {"Movie Cinema Tickets", "28.00", "Entertainment", "Movies & Theater"},
                    {"Pharmacy Medicine", "22.50", "Health & Wellness", "Pharmacy & Medicine"},
                    {"Monthly Gym Membership", "40.00", "Health & Wellness", "Gym & Fitness"},
                    {"New Sneakers", "85.00", "Shopping", "Clothing & Footwear"},
                    {"Amazon Home Essentials", "39.40", "Shopping", "Home & Kitchen"},
                    {"Hair Salon", "35.00", "Personal Care", "Haircut & Salon"},
                    {"Online Tech Course", "19.99", "Education & Work", "Books & Courses"}
                };

                for (int i = 0; i < sampleItems.length; i++) {
                    String[] item = sampleItems[i];
                    String title = item[0];
                    double amount = Double.parseDouble(item[1]);
                    String catName = item[2];
                    String subName = item[3];

                    Category targetCat = null;
                    Subcategory targetSub = null;

                    for (CategoryWithSubcategories c : cats) {
                        if (c.category.getName().equalsIgnoreCase(catName)) {
                            targetCat = c.category;
                            if (c.subcategories != null) {
                                for (Subcategory s : c.subcategories) {
                                    if (s.getName().equalsIgnoreCase(subName)) {
                                        targetSub = s;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }

                    if (targetCat == null) {
                        targetCat = cats.get(0).category;
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    cal.set(Calendar.DAY_OF_MONTH, Math.min(maxDay, (i * 2) % maxDay + 1));
                    cal.set(Calendar.HOUR_OF_DAY, 10 + (i % 10));
                    cal.set(Calendar.MINUTE, (i * 13) % 60);

                    Long subId = targetSub != null ? targetSub.getId() : null;
                    Expense expense = new Expense(title, amount, cal.getTimeInMillis(), targetCat.getId(), subId, "Demo entry");
                    expenseDao.insert(expense);
                }
            }

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public interface OnCategoryInsertedListener {
        void onInserted(long categoryId);
    }

    public interface OnDeleteCheckListener {
        void onResult(boolean deleted, int activeExpenseCount);
    }
}
