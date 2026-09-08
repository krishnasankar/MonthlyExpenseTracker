package com.expensetracker.monthly.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.expensetracker.monthly.data.entity.Subcategory;

import java.util.List;

@Dao
public interface SubcategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Subcategory subcategory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Subcategory> subcategories);

    @Update
    void update(Subcategory subcategory);

    @Delete
    void delete(Subcategory subcategory);

    @Query("DELETE FROM subcategories WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM subcategories WHERE category_id = :categoryId ORDER BY name ASC")
    LiveData<List<Subcategory>> getSubcategoriesByCategoryIdLive(long categoryId);

    @Query("SELECT * FROM subcategories WHERE category_id = :categoryId ORDER BY name ASC")
    List<Subcategory> getSubcategoriesByCategoryIdSync(long categoryId);

    @Query("SELECT * FROM subcategories ORDER BY name ASC")
    LiveData<List<Subcategory>> getAllSubcategoriesLive();
}
