package com.expensetracker.monthly.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

public class CategorySpendSummary {

    @ColumnInfo(name = "category_id")
    public long categoryId;

    @ColumnInfo(name = "category_name")
    public String categoryName;

    @ColumnInfo(name = "color_hex")
    public String colorHex;

    @ColumnInfo(name = "total_amount")
    public double totalAmount;

    @ColumnInfo(name = "transaction_count")
    public int transactionCount;

    // Computed dynamically
    @Ignore
    public float percentage;

    public long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
