package com.expensetracker.monthly.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
    tableName = "subcategories",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "category_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "category_id"),
        @Index(value = {"category_id", "name"}, unique = true)
    }
)
public class Subcategory {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "category_id")
    private long categoryId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    public Subcategory(long categoryId, @NonNull String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subcategory that = (Subcategory) o;
        return id == that.id &&
                categoryId == that.categoryId &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryId, name);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
