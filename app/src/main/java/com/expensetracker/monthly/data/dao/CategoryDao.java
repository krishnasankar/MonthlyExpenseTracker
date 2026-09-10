package com.expensetracker.monthly.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.model.CategoryWithSubcategories;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Category> categories);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(long id);

    @Query("UPDATE categories SET budget_amount = :budgetAmount WHERE id = :categoryId")
    void updateCategoryBudget(long categoryId, double budgetAmount);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllCategoriesLive();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategoriesSync();

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    Category getCategoryByIdSync(long id);

    @Transaction
    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<CategoryWithSubcategories>> getCategoriesWithSubcategoriesLive();

    @Transaction
    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<CategoryWithSubcategories> getCategoriesWithSubcategoriesSync();

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(:name) AND id != :excludeId")
    int countCategoriesByNameExcludingId(String name, long excludeId);

    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(:name)")
    int countCategoriesByName(String name);
}
