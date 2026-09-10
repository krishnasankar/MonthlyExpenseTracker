package com.expensetracker.monthly.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.data.entity.Subcategory;

public class RecurringExpenseWithDetails {

    @Embedded
    public RecurringExpense recurringExpense;

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

    public RecurringExpense getRecurringExpense() {
        return recurringExpense;
    }

    public Category getCategory() {
        return category;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }
}
