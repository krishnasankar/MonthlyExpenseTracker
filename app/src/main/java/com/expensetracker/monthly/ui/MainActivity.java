package com.expensetracker.monthly.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.databinding.ActivityMainBinding;
import com.expensetracker.monthly.ui.dialog.AddEditExpenseDialogFragment;
import com.expensetracker.monthly.ui.fragment.DashboardFragment;
import com.expensetracker.monthly.ui.fragment.ExpensesListFragment;
import com.expensetracker.monthly.ui.fragment.SettingsFragment;
import com.expensetracker.monthly.util.BiometricUtils;
import com.expensetracker.monthly.util.RecurringExpenseManager;
import com.expensetracker.monthly.util.ThemeUtils;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_OPEN_ADD_EXPENSE = "com.expensetracker.monthly.ACTION_OPEN_ADD_EXPENSE";
    public static final String EXTRA_OPEN_ADD_EXPENSE = "EXTRA_OPEN_ADD_EXPENSE";
    public static final String EXTRA_NAVIGATE_TAB = "EXTRA_NAVIGATE_TAB";

    private ActivityMainBinding binding;
    private boolean isAuthenticated = false;

    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final ExpensesListFragment expensesListFragment = new ExpensesListFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topToolbar);

        setupBottomNavigation();
        setupFab();

        binding.btnUnlock.setOnClickListener(v -> promptBiometricUnlock());

        if (savedInstanceState == null) {
            switchFragment(dashboardFragment, getString(R.string.nav_dashboard));
        }

        if (BiometricUtils.isAppLockEnabled(this)) {
            binding.layoutLockOverlay.setVisibility(View.VISIBLE);
            promptBiometricUnlock();
        } else {
            isAuthenticated = true;
            binding.layoutLockOverlay.setVisibility(View.GONE);
            handleWidgetIntent(getIntent());
            RecurringExpenseManager.checkAndProcessRecurringExpenses(getApplicationContext());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BiometricUtils.isAppLockEnabled(this)) {
            if (!isAuthenticated) {
                binding.layoutLockOverlay.setVisibility(View.VISIBLE);
                promptBiometricUnlock();
            } else {
                binding.layoutLockOverlay.setVisibility(View.GONE);
                RecurringExpenseManager.checkAndProcessRecurringExpenses(getApplicationContext());
            }
        } else {
            isAuthenticated = true;
            binding.layoutLockOverlay.setVisibility(View.GONE);
            RecurringExpenseManager.checkAndProcessRecurringExpenses(getApplicationContext());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (BiometricUtils.isAppLockEnabled(this)) {
            isAuthenticated = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (isAuthenticated) {
            handleWidgetIntent(intent);
        }
    }

    private void promptBiometricUnlock() {
        if (!BiometricUtils.isAppLockEnabled(this)) {
            isAuthenticated = true;
            binding.layoutLockOverlay.setVisibility(View.GONE);
            return;
        }

        BiometricUtils.showBiometricPrompt(this, new BiometricUtils.BiometricAuthListener() {
            @Override
            public void onSuccess() {
                isAuthenticated = true;
                binding.layoutLockOverlay.setVisibility(View.GONE);
                RecurringExpenseManager.checkAndProcessRecurringExpenses(getApplicationContext());
                handleWidgetIntent(getIntent());
            }

            @Override
            public void onError(String message) {
                isAuthenticated = false;
                binding.layoutLockOverlay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleWidgetIntent(Intent intent) {
        if (intent == null) return;

        if (intent.hasExtra(EXTRA_NAVIGATE_TAB)) {
            String tab = intent.getStringExtra(EXTRA_NAVIGATE_TAB);
            intent.removeExtra(EXTRA_NAVIGATE_TAB);
            if ("expenses".equalsIgnoreCase(tab)) {
                navigateToExpensesTab();
            } else if ("dashboard".equalsIgnoreCase(tab)) {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
            }
        }

        boolean isOpenAdd = ACTION_OPEN_ADD_EXPENSE.equals(intent.getAction()) ||
                intent.getBooleanExtra(EXTRA_OPEN_ADD_EXPENSE, false) ||
                "true".equalsIgnoreCase(intent.getStringExtra(EXTRA_OPEN_ADD_EXPENSE)) ||
                intent.getBooleanExtra("extra_open_add_expense", false);

        if (isOpenAdd) {
            intent.removeExtra(EXTRA_OPEN_ADD_EXPENSE);
            intent.removeExtra("extra_open_add_expense");
            intent.setAction(null);
            AddEditExpenseDialogFragment.newInstance(null)
                    .show(getSupportFragmentManager(), AddEditExpenseDialogFragment.TAG);
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                switchFragment(dashboardFragment, getString(R.string.nav_dashboard));
                binding.fabAddExpense.show();
                return true;
            } else if (itemId == R.id.nav_expenses) {
                switchFragment(expensesListFragment, getString(R.string.nav_expenses));
                binding.fabAddExpense.show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                switchFragment(settingsFragment, getString(R.string.nav_settings));
                binding.fabAddExpense.hide();
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(v -> {
            AddEditExpenseDialogFragment.newInstance(null)
                    .show(getSupportFragmentManager(), AddEditExpenseDialogFragment.TAG);
        });
    }

    private void switchFragment(Fragment fragment, String title) {
        if (binding.topToolbar != null) {
            binding.topToolbar.setTitle(title);
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public void navigateToExpensesTab() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_expenses);
    }
}
