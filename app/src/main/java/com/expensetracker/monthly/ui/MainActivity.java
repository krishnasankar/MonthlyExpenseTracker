package com.expensetracker.monthly.ui;

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

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_OPEN_ADD_EXPENSE = "com.expensetracker.monthly.ACTION_OPEN_ADD_EXPENSE";
    public static final String EXTRA_OPEN_ADD_EXPENSE = "extra_open_add_expense";

    private ActivityMainBinding binding;

    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final ExpensesListFragment expensesListFragment = new ExpensesListFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.expensetracker.monthly.util.ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topToolbar);

        setupBottomNavigation();
        setupFab();

        if (savedInstanceState == null) {
            switchFragment(dashboardFragment, getString(R.string.nav_dashboard));
        }

        handleWidgetIntent(getIntent());
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleWidgetIntent(intent);
    }

    private void handleWidgetIntent(android.content.Intent intent) {
        if (intent == null) return;
        if (ACTION_OPEN_ADD_EXPENSE.equals(intent.getAction()) ||
                intent.getBooleanExtra(EXTRA_OPEN_ADD_EXPENSE, false)) {
            intent.removeExtra(EXTRA_OPEN_ADD_EXPENSE);
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
