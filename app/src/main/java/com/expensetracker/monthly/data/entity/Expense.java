package com.expensetracker.monthly.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
    tableName = "expenses",
    foreignKeys = {
        @ForeignKey(
            entity = Category.class,
            parentColumns = "id",
            childColumns = "category_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Subcategory.class,
            parentColumns = "id",
            childColumns = "subcategory_id",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {
        @Index(value = "category_id"),
        @Index(value = "subcategory_id"),
        @Index(value = "date_millis")
    }
)
public class Expense {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "date_millis")
    private long dateMillis;

    @ColumnInfo(name = "category_id")
    private long categoryId;

    @Nullable
    @ColumnInfo(name = "subcategory_id")
    private Long subcategoryId;

    @Nullable
    @ColumnInfo(name = "notes")
    private String notes;

    public Expense(@NonNull String title, double amount, long dateMillis, long categoryId, @Nullable Long subcategoryId, @Nullable String notes) {
        this.title = title;
        this.amount = amount;
        this.dateMillis = dateMillis;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    @Nullable
    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(@Nullable Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return id == expense.id &&
                Double.compare(expense.amount, amount) == 0 &&
                dateMillis == expense.dateMillis &&
                categoryId == expense.categoryId &&
                Objects.equals(title, expense.title) &&
                Objects.equals(subcategoryId, expense.subcategoryId) &&
                Objects.equals(notes, expense.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, amount, dateMillis, categoryId, subcategoryId, notes);
    }
}
