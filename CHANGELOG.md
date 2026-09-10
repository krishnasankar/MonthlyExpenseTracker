# Changelog

All notable changes to the **Monthly Expense Tracker** application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.0.0] - 2026-09-10

### Added
- **Monthly Budget Pacing**: Set overall monthly spending limits with live tracking of remaining balance, daily burn rate, and over-budget status.
- **Safe Daily Spend**: Smart daily budget calculator computing safe spending thresholds based on remaining days in the active month.
- **Category-Level Budgets**: Allocate individual monthly spending caps per category with visual budget badges and progress bars.
- **Home Screen App Widget**: Interactive widget displaying current month's expenses and quick one-tap entry to record new expenses.
- **Category & Subcategory Management**: Full edit capabilities for categories (name, color palette) and subcategories in addition to deletion protections.
- **Smart Expense Entry**: Autofill suggestions from previous expense titles with automatic category and subcategory selection.
- **Swipe-to-Delete**: Seamless `ItemTouchHelper` swipe gesture to delete expense entries with immediate snackbar feedback.
- **Room Database Migration**: Added `MIGRATION_2_3` to seamlessly upgrade existing databases with the `budget_amount` column.
- **Unit Test Suite**: Added test coverage for `BudgetUtils` math edge cases and category/subcategory operations.

### Changed
- Streamlined Add Expense dialog with automatic keyboard focus and smooth IME action progression.
- Enhanced dashboard UI with collapsible/expandable budget overview and pacing cards.
- Bumped Android `versionCode` to `2` and `versionName` to `"2.0.0"`.

---

## [1.0.0] - 2026-09-09

### Added
- Native Android offline-first expense tracker built in Java with Jetpack Room and MVVM architecture.
- Predefined categories and subcategories covering everyday spending needs.
- Interactive Canvas-rendered Donut Chart with slice selection and dynamic breakdowns.
- Search, filter chips, and month navigation.
- Light, Dark, and System Default theme support with OLED contrast optimization.
- Multi-currency support defaulting to Indian Rupee (₹).
- Minimalist vector branding and adaptive launcher icons.
- Production release signing configuration.
