# Monthly Expense Tracker (Android)

A modern, clean, native Android application written in Java to manage personal expenses on a monthly basis. The application is built with 100% offline-first local persistence using Android Jetpack Room (SQLite), MVVM architecture, and Material Design 3.

---

## 🌟 Key Features

1. **Monthly Expense Management**:
   - Track expenses month by month with an intuitive `< Previous Month | Month Year | Next Month >` selector and date picker dialog.
   - Record expenses with Title, Amount, Date, Category, dynamic optional Subcategory, and Notes.
   - Instant search and category filtering chips to quickly find past expenses.
   - Tap any expense to edit details or delete with confirmation.

2. **Hierarchical Categories & Subcategories**:
   - Predefined everyday categories seeded automatically:
     - **Food & Dining**: Groceries, Restaurants, Coffee & Snacks, Fast Food
     - **Transportation**: Fuel / Gas, Public Transit, Taxi / Rideshare, Vehicle Maintenance, Parking
     - **Housing & Utilities**: Rent / Mortgage, Electricity, Water, Internet & WiFi, Gas & Heating
     - **Entertainment**: Streaming & Subscriptions, Movies & Theater, Gaming, Concerts & Events
     - **Health & Wellness**: Doctor & Dental, Pharmacy & Medicine, Gym & Fitness
     - **Shopping**: Clothing & Footwear, Electronics, Home & Kitchen
     - **Personal Care**: Haircut & Salon, Cosmetics & Skincare
     - **Education & Work**: Books & Courses, Software & Tools, Office Supplies
     - **Miscellaneous**: General, Gifts, Donations
   - **Settings Management**:
     - Add custom categories with color palette selection.
     - Add subcategories under any category.
     - Delete subcategories (with individual chip delete buttons).
     - Delete categories (with safety confirmation).

3. **Analytics Dashboard**:
   - **Metrics Cards**: Total Expenses, Daily Average, Transaction Count, and Highest Spend Category for the selected month.
   - **Interactive Donut Chart**: Custom Canvas-rendered donut chart with smooth arcs, hole cutout, category color segments, and interactive touch selection.
   - **Category Breakdown**: Progress bars showing the exact percentage and dollar amount spent per category.
   - **Recent Transactions**: Quick preview of the most recent expenses in the selected month with direct link to the full list.

4. **Preferences & Dark Mode**:
   - **Dark Mode Support**: Full support for System Default, Light Mode, and Dark Mode themes with OLED-optimized contrast.
   - **Default Currency (INR)**: Pre-configured with Indian Rupee (`₹`) as the default currency on launch, with multi-currency options (`$`, `€`, `£`, `₹`, `¥`).

5. **Minimalist Branding & Modern Identity**:
   - **Custom Vector Logo**: High-fidelity, scalable Android Vector Drawable combining monthly calendar geometry, an upward 'M' financial trendline, and an expense coin token.
   - **Adaptive Launcher Icons**: Dual-layer adaptive launcher icon with vibrant brand gradient background and centered safe-zone foreground (`ic_launcher` & `ic_launcher_round`).
   - **In-App Integration**: Prominently displayed in the top `MaterialToolbar` across all screens and within an "About App" branding card in Settings.

---

## 🏗️ Architecture & Tech Stack

- **Platform**: Native Android (Java 17 / 21)
- **Architecture Pattern**: MVVM (Model - View - ViewModel) + Repository
- **Local Persistence**: Jetpack Room Database (SQLite)
  - `Category`: `categories` table
  - `Subcategory`: `subcategories` table (Foreign Key -> `categories.id` with CASCADE delete)
  - `Expense`: `expenses` table (Foreign Keys -> `categories.id` & `subcategories.id`)
- **Reactive UI**: Android Jetpack `LiveData` & `Transformations.switchMap`
- **UI Toolkit**: Material Design 3 Components, ViewBinding, CoordinatorLayout, ConstraintLayout, RecyclerView, BottomNavigationView
- **Visualizations**: Custom Canvas-rendered `DonutChartView`

---

## 🚀 How to Open and Run

### In Android Studio:
1. Open Android Studio.
2. Select **File -> Open...**
3. Select and open the cloned `MonthlyExpenseTracker` repository directory.
4. Wait for Gradle Sync to complete.
5. Select an Android Emulator or connected physical device (API 26+) and click **Run (Shift + F10)**.

### From Command Line:
```powershell
cd MonthlyExpenseTracker
.\gradlew.bat assembleDebug
```
Or on macOS/Linux:
```bash
cd MonthlyExpenseTracker
./gradlew assembleDebug
```

To run unit tests:
```powershell
.\gradlew.bat test
```
Or on macOS/Linux:
```bash
./gradlew test
```
