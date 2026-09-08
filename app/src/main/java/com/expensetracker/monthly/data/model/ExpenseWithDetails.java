package com.expensetracker.monthly.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Expense;
import com.expensetracker.monthly.data.entity.Subcategory;

public class ExpenseWithDetails {

    @Embedded
    public Expense expense;

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    public Category category;

    @Relation(
        parentColumn = "subcategory_id",
        entityColumn = "id"
    )
    public Subcategory subcategory;

    public Expense getExpense() {
        return expense;
    }

    public Category getCategory() {
        return category;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }
}
