package com.expensetracker.monthly.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "recurring_expenses",
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
        @Index(value = "subcategory_id")
    }
)
public class RecurringExpense {

    public static final String FREQUENCY_MONTHLY = "MONTHLY";
    public static final String FREQUENCY_WEEKLY = "WEEKLY";
    public static final String FREQUENCY_YEARLY = "YEARLY";

    public static String getFrequencyDisplayName(String frequency) {
        if (frequency == null) return "Monthly";
        switch (frequency.toUpperCase(java.util.Locale.US)) {
            case FREQUENCY_WEEKLY:
                return "Weekly";
            case FREQUENCY_YEARLY:
                return "Yearly";
            case FREQUENCY_MONTHLY:
            default:
                return "Monthly";
        }
    }

    public static String parseFrequencyFromDisplay(String display) {
        if (display == null) return FREQUENCY_MONTHLY;
        String trimmed = display.trim().toUpperCase(java.util.Locale.US);
        if ("WEEKLY".equals(trimmed)) {
            return FREQUENCY_WEEKLY;
        } else if ("YEARLY".equals(trimmed)) {
            return FREQUENCY_YEARLY;
        } else {
            return FREQUENCY_MONTHLY;
        }
    }

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "category_id")
    private long categoryId;

    @Nullable
    @ColumnInfo(name = "subcategory_id")
    private Long subcategoryId;

    @NonNull
    @ColumnInfo(name = "frequency", defaultValue = "'MONTHLY'")
    private String frequency = FREQUENCY_MONTHLY;

    @ColumnInfo(name = "day_of_month", defaultValue = "1")
    private int dayOfMonth = 1;

    @ColumnInfo(name = "last_logged_millis", defaultValue = "0")
    private long lastLoggedMillis = 0;

    @ColumnInfo(name = "is_active", defaultValue = "1")
    private boolean isActive = true;

    @Nullable
    @ColumnInfo(name = "notes")
    private String notes;

    public RecurringExpense(@NonNull String title, double amount, long categoryId,
                            @Nullable Long subcategoryId, @NonNull String frequency,
                            int dayOfMonth, long lastLoggedMillis, boolean isActive,
                            @Nullable String notes) {
        this.title = title;
        this.amount = amount;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.frequency = frequency != null ? frequency : FREQUENCY_MONTHLY;
        this.dayOfMonth = Math.max(1, Math.min(31, dayOfMonth));
        this.lastLoggedMillis = lastLoggedMillis;
        this.isActive = isActive;
        this.notes = notes;
    }

    @androidx.room.Ignore
    public RecurringExpense(@NonNull String title, double amount, long categoryId,
                            @Nullable Long subcategoryId, @NonNull String frequency,
                            int dayOfMonth, @Nullable String notes) {
        this(title, amount, categoryId, subcategoryId, frequency, dayOfMonth, 0L, true, notes);
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

    @NonNull
    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(@NonNull String frequency) {
        this.frequency = frequency;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = Math.max(1, Math.min(31, dayOfMonth));
    }

    public long getLastLoggedMillis() {
        return lastLoggedMillis;
    }

    public void setLastLoggedMillis(long lastLoggedMillis) {
        this.lastLoggedMillis = lastLoggedMillis;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
}
