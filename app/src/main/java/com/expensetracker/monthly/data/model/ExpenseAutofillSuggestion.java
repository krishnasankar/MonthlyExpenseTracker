package com.expensetracker.monthly.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

public class ExpenseAutofillSuggestion {

    @NonNull
    @ColumnInfo(name = "title")
    public String title = "";

    @ColumnInfo(name = "category_id")
    public long categoryId;

    @Nullable
    @ColumnInfo(name = "subcategory_id")
    public Long subcategoryId;

    @ColumnInfo(name = "max_date")
    public long maxDate;

    public ExpenseAutofillSuggestion() {
    }

    @Ignore
    public ExpenseAutofillSuggestion(@NonNull String title, long categoryId, @Nullable Long subcategoryId, long maxDate) {
        this.title = title;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.maxDate = maxDate;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public long getCategoryId() {
        return categoryId;
    }

    @Nullable
    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public long getMaxDate() {
        return maxDate;
    }

    @NonNull
    @Override
    public String toString() {
        return title != null ? title : "";
    }
}
