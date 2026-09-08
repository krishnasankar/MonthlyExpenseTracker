package com.expensetracker.monthly.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
    tableName = "categories",
    indices = {@Index(value = {"name"}, unique = true)}
)
public class Category {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "color_hex")
    private String colorHex;

    @ColumnInfo(name = "icon_name")
    private String iconName;

    public Category(@NonNull String name, @NonNull String colorHex, String iconName) {
        this.name = name;
        this.colorHex = colorHex;
        this.iconName = iconName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(@NonNull String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id &&
                Objects.equals(name, category.name) &&
                Objects.equals(colorHex, category.colorHex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, colorHex);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
