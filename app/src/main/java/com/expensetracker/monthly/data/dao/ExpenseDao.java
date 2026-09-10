package com.expensetracker.monthly.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.expensetracker.monthly.data.entity.Expense;
import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.data.model.ExpenseAutofillSuggestion;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Expense expense);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Expense> expenses);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteById(long id);

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    LiveData<ExpenseWithDetails> getExpenseByIdLive(long id);

    @Transaction
    @Query("SELECT * FROM expenses WHERE date_millis >= :startMillis AND date_millis <= :endMillis ORDER BY date_millis DESC, id DESC")
    LiveData<List<ExpenseWithDetails>> getExpensesForDateRangeLive(long startMillis, long endMillis);

    @Transaction
    @Query("SELECT * FROM expenses WHERE date_millis >= :startMillis AND date_millis <= :endMillis ORDER BY date_millis DESC, id DESC")
    List<ExpenseWithDetails> getExpensesForDateRangeSync(long startMillis, long endMillis);

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY date_millis DESC, id DESC")
    List<ExpenseWithDetails> getAllExpensesSync();

    @Transaction
    @Query("SELECT * FROM expenses WHERE date_millis >= :startMillis AND date_millis <= :endMillis ORDER BY date_millis DESC, id DESC LIMIT :limit")
    LiveData<List<ExpenseWithDetails>> getRecentExpensesForDateRangeLive(long startMillis, long endMillis, int limit);

    @Query("SELECT c.id AS category_id, c.name AS category_name, c.color_hex AS color_hex, c.budget_amount AS budget_amount, " +
           "COALESCE(SUM(e.amount), 0.0) AS total_amount, COUNT(e.id) AS transaction_count " +
           "FROM categories c " +
           "INNER JOIN expenses e ON e.category_id = c.id " +
           "WHERE e.date_millis >= :startMillis AND e.date_millis <= :endMillis " +
           "GROUP BY c.id " +
           "ORDER BY total_amount DESC")
    LiveData<List<CategorySpendSummary>> getMonthlyCategorySpendLive(long startMillis, long endMillis);

    @Query("SELECT c.id AS category_id, c.name AS category_name, c.color_hex AS color_hex, c.budget_amount AS budget_amount, " +
           "COALESCE(SUM(e.amount), 0.0) AS total_amount, COUNT(e.id) AS transaction_count " +
           "FROM categories c " +
           "INNER JOIN expenses e ON e.category_id = c.id " +
           "WHERE e.date_millis >= :startMillis AND e.date_millis <= :endMillis " +
           "GROUP BY c.id " +
           "ORDER BY total_amount DESC")
    List<CategorySpendSummary> getMonthlyCategorySpendSync(long startMillis, long endMillis);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date_millis >= :startMillis AND date_millis <= :endMillis")
    LiveData<Double> getTotalSpendForDateRangeLive(long startMillis, long endMillis);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date_millis >= :startMillis AND date_millis <= :endMillis")
    double getTotalSpendForDateRangeSync(long startMillis, long endMillis);

    @Query("SELECT COUNT(*) FROM expenses WHERE date_millis >= :startMillis AND date_millis <= :endMillis")
    LiveData<Integer> getExpenseCountForDateRangeLive(long startMillis, long endMillis);

    @Query("SELECT COUNT(*) FROM expenses WHERE date_millis >= :startMillis AND date_millis <= :endMillis")
    int getExpenseCountForDateRangeSync(long startMillis, long endMillis);

    @Transaction
    @Query("SELECT e.* FROM expenses e " +
           "LEFT JOIN categories c ON e.category_id = c.id " +
           "WHERE e.date_millis >= :startMillis AND e.date_millis <= :endMillis " +
           "AND (e.title LIKE '%' || :searchQuery || '%' OR e.notes LIKE '%' || :searchQuery || '%' OR c.name LIKE '%' || :searchQuery || '%') " +
           "ORDER BY e.date_millis DESC, e.id DESC")
    LiveData<List<ExpenseWithDetails>> searchExpensesLive(long startMillis, long endMillis, String searchQuery);

    @Query("SELECT COUNT(*) FROM expenses WHERE category_id = :categoryId")
    int countExpensesByCategoryId(long categoryId);

    @Query("SELECT COUNT(*) FROM expenses WHERE subcategory_id = :subcategoryId")
    int countExpensesBySubcategoryId(long subcategoryId);

    @Query("DELETE FROM expenses")
    void deleteAll();

    @Query("SELECT title, category_id, subcategory_id, MAX(date_millis) AS max_date " +
           "FROM expenses " +
           "WHERE TRIM(title) != '' " +
           "GROUP BY LOWER(TRIM(title)) " +
           "ORDER BY max_date DESC")
    LiveData<List<ExpenseAutofillSuggestion>> getExpenseAutofillSuggestionsLive();
}
