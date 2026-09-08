package com.expensetracker.monthly.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Subcategory;

import java.util.List;

public class CategoryWithSubcategories {
    @Embedded
    public Category category;

    @Relation(
        parentColumn = "id",
        entityColumn = "category_id"
    )
    public List<Subcategory> subcategories;

    public Category getCategory() {
        return category;
    }

    public List<Subcategory> getSubcategories() {
        return subcategories;
    }
}
