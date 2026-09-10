package com.expensetracker.monthly.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.data.model.RecurringExpenseWithDetails;

import java.util.List;

@Dao
public interface RecurringExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RecurringExpense recurringExpense);

    @Update
    void update(RecurringExpense recurringExpense);

    @Delete
    void delete(RecurringExpense recurringExpense);

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    void deleteById(long id);

    @Transaction
    @Query("SELECT * FROM recurring_expenses ORDER BY day_of_month ASC, id ASC")
    LiveData<List<RecurringExpenseWithDetails>> getAllRecurringWithDetailsLive();

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE is_active = 1 ORDER BY day_of_month ASC")
    List<RecurringExpenseWithDetails> getActiveRecurringWithDetailsSync();

    @Query("UPDATE recurring_expenses SET is_active = :isActive WHERE id = :id")
    void updateActiveState(long id, boolean isActive);

    @Query("UPDATE recurring_expenses SET last_logged_millis = :lastLoggedMillis WHERE id = :id")
    void updateLastLogged(long id, long lastLoggedMillis);

    @Query("SELECT * FROM recurring_expenses WHERE id = :id LIMIT 1")
    RecurringExpense getByIdSync(long id);
}
