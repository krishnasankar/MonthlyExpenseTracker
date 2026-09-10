package com.expensetracker.monthly.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.expensetracker.monthly.data.dao.CategoryDao;
import com.expensetracker.monthly.data.dao.ExpenseDao;
import com.expensetracker.monthly.data.dao.SubcategoryDao;
import com.expensetracker.monthly.data.dao.RecurringExpenseDao;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Expense;
import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.data.entity.Subcategory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {Category.class, Subcategory.class, Expense.class, RecurringExpense.class},
    version = 4,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "monthly_expenses.db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static final androidx.room.migration.Migration MIGRATION_2_3 = new androidx.room.migration.Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE categories ADD COLUMN budget_amount REAL NOT NULL DEFAULT 0.0");
        }
    };

    public static final androidx.room.migration.Migration MIGRATION_3_4 = new androidx.room.migration.Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS recurring_expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "category_id INTEGER NOT NULL, " +
                    "subcategory_id INTEGER, " +
                    "frequency TEXT NOT NULL DEFAULT 'MONTHLY', " +
                    "day_of_month INTEGER NOT NULL DEFAULT 1, " +
                    "last_logged_millis INTEGER NOT NULL DEFAULT 0, " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "notes TEXT, " +
                    "FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(subcategory_id) REFERENCES subcategories(id) ON DELETE SET NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_expenses_category_id ON recurring_expenses(category_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_expenses_subcategory_id ON recurring_expenses(subcategory_id)");
        }
    };

    public abstract CategoryDao categoryDao();
    public abstract SubcategoryDao subcategoryDao();
    public abstract ExpenseDao expenseDao();
    public abstract RecurringExpenseDao recurringExpenseDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .addCallback(sRoomDatabaseCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null) {
                    seedDefaultCategories(INSTANCE);
                }
            });
        }
    };

    public static void seedDefaultCategories(AppDatabase database) {
        CategoryDao categoryDao = database.categoryDao();
        SubcategoryDao subcategoryDao = database.subcategoryDao();

        // 1. Food & Dining
        long foodId = categoryDao.insert(new Category("Food & Dining", "#FF7043", "food"));
        subcategoryDao.insert(new Subcategory(foodId, "Groceries"));
        subcategoryDao.insert(new Subcategory(foodId, "Restaurants"));
        subcategoryDao.insert(new Subcategory(foodId, "Coffee & Snacks"));
        subcategoryDao.insert(new Subcategory(foodId, "Fast Food"));

        // 2. Transportation
        long transportId = categoryDao.insert(new Category("Transportation", "#42A5F5", "transport"));
        subcategoryDao.insert(new Subcategory(transportId, "Fuel / Gas"));
        subcategoryDao.insert(new Subcategory(transportId, "Public Transit"));
        subcategoryDao.insert(new Subcategory(transportId, "Taxi / Rideshare"));
        subcategoryDao.insert(new Subcategory(transportId, "Vehicle Maintenance"));
        subcategoryDao.insert(new Subcategory(transportId, "Parking"));

        // 3. Housing & Utilities
        long housingId = categoryDao.insert(new Category("Housing & Utilities", "#AB47BC", "housing"));
        subcategoryDao.insert(new Subcategory(housingId, "Rent / Mortgage"));
        subcategoryDao.insert(new Subcategory(housingId, "Electricity"));
        subcategoryDao.insert(new Subcategory(housingId, "Water"));
        subcategoryDao.insert(new Subcategory(housingId, "Internet & WiFi"));
        subcategoryDao.insert(new Subcategory(housingId, "Gas & Heating"));

        // 4. Entertainment
        long entertainmentId = categoryDao.insert(new Category("Entertainment", "#FFA726", "entertainment"));
        subcategoryDao.insert(new Subcategory(entertainmentId, "Streaming & Subscriptions"));
        subcategoryDao.insert(new Subcategory(entertainmentId, "Movies & Theater"));
        subcategoryDao.insert(new Subcategory(entertainmentId, "Gaming"));
        subcategoryDao.insert(new Subcategory(entertainmentId, "Concerts & Events"));

        // 5. Health & Wellness
        long healthId = categoryDao.insert(new Category("Health & Wellness", "#26A69A", "health"));
        subcategoryDao.insert(new Subcategory(healthId, "Doctor & Dental"));
        subcategoryDao.insert(new Subcategory(healthId, "Pharmacy & Medicine"));
        subcategoryDao.insert(new Subcategory(healthId, "Gym & Fitness"));

        // 6. Shopping
        long shoppingId = categoryDao.insert(new Category("Shopping", "#EC407A", "shopping"));
        subcategoryDao.insert(new Subcategory(shoppingId, "Clothing & Footwear"));
        subcategoryDao.insert(new Subcategory(shoppingId, "Electronics"));
        subcategoryDao.insert(new Subcategory(shoppingId, "Home & Kitchen"));

        // 7. Personal Care
        long personalId = categoryDao.insert(new Category("Personal Care", "#7E57C2", "personal"));
        subcategoryDao.insert(new Subcategory(personalId, "Haircut & Salon"));
        subcategoryDao.insert(new Subcategory(personalId, "Cosmetics & Skincare"));

        // 8. Education & Work
        long eduId = categoryDao.insert(new Category("Education & Work", "#5C6BC0", "education"));
        subcategoryDao.insert(new Subcategory(eduId, "Books & Courses"));
        subcategoryDao.insert(new Subcategory(eduId, "Software & Tools"));
        subcategoryDao.insert(new Subcategory(eduId, "Office Supplies"));

        // 9. Miscellaneous
        long miscId = categoryDao.insert(new Category("Miscellaneous", "#78909C", "misc"));
        subcategoryDao.insert(new Subcategory(miscId, "General"));
        subcategoryDao.insert(new Subcategory(miscId, "Gifts"));
        subcategoryDao.insert(new Subcategory(miscId, "Donations"));
    }
}
