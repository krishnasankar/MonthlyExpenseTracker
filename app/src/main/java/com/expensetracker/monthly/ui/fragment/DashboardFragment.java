package com.expensetracker.monthly.ui.fragment;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;
import com.expensetracker.monthly.databinding.FragmentDashboardBinding;
import com.expensetracker.monthly.ui.MainActivity;
import com.expensetracker.monthly.ui.adapter.CategorySummaryAdapter;
import com.expensetracker.monthly.ui.adapter.ExpenseAdapter;
import com.expensetracker.monthly.ui.dialog.AddEditExpenseDialogFragment;
import com.expensetracker.monthly.ui.dialog.SetBudgetDialogFragment;
import com.expensetracker.monthly.ui.dialog.SetCategoryBudgetDialogFragment;
import com.expensetracker.monthly.ui.viewmodel.DashboardViewModel;
import com.expensetracker.monthly.ui.viewmodel.ExpenseViewModel;
import com.expensetracker.monthly.ui.widget.MonthlyExpenseWidgetProvider;
import com.expensetracker.monthly.util.BudgetUtils;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.DateUtils;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private ExpenseViewModel expenseViewModel;

    private CategorySummaryAdapter categorySummaryAdapter;
    private ExpenseAdapter recentExpenseAdapter;

    private double currentTotalSpend = 0.0;
    private String currentCurrency = "$";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        setupMonthNavigation();
        setupBudgetCard();
        setupRecyclerViews();
        setupChart();
        setupObservers();

        binding.btnViewAllExpenses.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToExpensesTab();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        currentCurrency = CurrencyUtils.getCurrencySymbol(requireContext());
        if (recentExpenseAdapter != null) {
            recentExpenseAdapter.setCurrencySymbol(currentCurrency);
        }
        dashboardViewModel.refreshBudget();
        updateBudgetCard();
    }

    private void setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> dashboardViewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> dashboardViewModel.nextMonth());
        binding.layoutMonthPicker.setOnClickListener(v -> showMonthPicker());
    }

    private void showMonthPicker() {
        Calendar current = dashboardViewModel.getSelectedMonth().getValue();
        if (current == null) current = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> dashboardViewModel.setMonth(year, month),
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void setupBudgetCard() {
        binding.btnSetBudget.setOnClickListener(v -> showBudgetDialog());
        binding.btnEditBudget.setOnClickListener(v -> showBudgetDialog());
    }

    private void showBudgetDialog() {
        SetBudgetDialogFragment dialog = SetBudgetDialogFragment.newInstance();
        dialog.setOnBudgetChangeListener(() -> {
            dashboardViewModel.refreshBudget();
            updateBudgetCard();
        });
        dialog.show(getChildFragmentManager(), SetBudgetDialogFragment.TAG);
    }

    private void updateBudgetCard() {
        if (binding == null || !isAdded()) return;
        Double budgetObj = dashboardViewModel.getMonthlyBudget().getValue();
        double budget = budgetObj != null ? budgetObj : 0.0;
        currentCurrency = CurrencyUtils.getCurrencySymbol(requireContext());

        if (budget <= 0.0) {
            binding.layoutBudgetPrompt.setVisibility(View.VISIBLE);
            binding.layoutBudgetSet.setVisibility(View.GONE);
            return;
        }

        binding.layoutBudgetPrompt.setVisibility(View.GONE);
        binding.layoutBudgetSet.setVisibility(View.VISIBLE);

        int percent = BudgetUtils.calculatePercentage(budget, currentTotalSpend);
        binding.progressBudget.setProgress(Math.min(100, percent));
        binding.tvBudgetPercent.setText(percent + "% spent");

        int progressColor;
        if (percent >= 100) {
            progressColor = ContextCompat.getColor(requireContext(), R.color.budget_error);
        } else if (percent >= 80) {
            progressColor = ContextCompat.getColor(requireContext(), R.color.budget_warning);
        } else {
            progressColor = ContextCompat.getColor(requireContext(), R.color.primary);
        }
        binding.progressBudget.setIndicatorColor(progressColor);
        binding.tvBudgetPercent.setTextColor(progressColor);

        binding.tvBudgetSpentVal.setText(CurrencyUtils.formatAmount(currentTotalSpend, currentCurrency));
        binding.tvBudgetTotalVal.setText(CurrencyUtils.formatAmount(budget, currentCurrency));

        double remaining = BudgetUtils.calculateRemaining(budget, currentTotalSpend);
        if (remaining >= 0) {
            binding.tvBudgetRemainingLabel.setText(R.string.budget_remaining);
            binding.tvBudgetRemainingVal.setText(CurrencyUtils.formatAmount(remaining, currentCurrency));
            binding.tvBudgetRemainingVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.budget_safe));
        } else {
            binding.tvBudgetRemainingLabel.setText(R.string.budget_over);
            binding.tvBudgetRemainingVal.setText("+" + CurrencyUtils.formatAmount(Math.abs(remaining), currentCurrency));
            binding.tvBudgetRemainingVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.budget_error));
        }

        Calendar cal = dashboardViewModel.getSelectedMonth().getValue();
        int daysLeft = BudgetUtils.getDaysRemainingInMonth(cal);
        double safeDaily = BudgetUtils.calculateDailySafeSpend(remaining, cal);

        if (daysLeft <= 0) {
            binding.layoutSafeDailySpend.setVisibility(View.GONE);
        } else {
            binding.layoutSafeDailySpend.setVisibility(View.VISIBLE);
            if (remaining <= 0) {
                binding.tvSafeDailySpendVal.setText(CurrencyUtils.formatAmount(0.0, currentCurrency) + " / day");
                binding.tvSafeDailySpendVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.budget_error));
            } else {
                binding.tvSafeDailySpendVal.setText(CurrencyUtils.formatAmount(safeDaily, currentCurrency) + " / day");
                binding.tvSafeDailySpendVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            }
            if (daysLeft == 1) {
                binding.tvDaysRemainingBadge.setText(R.string.day_remaining_single);
            } else {
                binding.tvDaysRemainingBadge.setText(String.format(getString(R.string.days_remaining_format), daysLeft));
            }
        }
    }

    private void setupRecyclerViews() {
        currentCurrency = CurrencyUtils.getCurrencySymbol(requireContext());

        // Category breakdown adapter
        categorySummaryAdapter = new CategorySummaryAdapter();
        categorySummaryAdapter.setOnCategoryClickListener(item -> {
            SetCategoryBudgetDialogFragment dialog = SetCategoryBudgetDialogFragment.newInstance(
                    item.categoryId,
                    item.categoryName,
                    item.colorHex,
                    item.budgetAmount
            );
            dialog.show(getChildFragmentManager(), SetCategoryBudgetDialogFragment.TAG);
        });
        binding.rvCategoryBreakdown.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategoryBreakdown.setAdapter(categorySummaryAdapter);

        // Recent expenses adapter
        recentExpenseAdapter = new ExpenseAdapter(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(ExpenseWithDetails item) {
                AddEditExpenseDialogFragment.newInstance(item)
                        .show(getChildFragmentManager(), AddEditExpenseDialogFragment.TAG);
            }

            @Override
            public void onExpenseDelete(ExpenseWithDetails item) {
                confirmDeleteExpense(item);
            }
        });
        recentExpenseAdapter.setCurrencySymbol(currentCurrency);
        binding.rvRecentExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentExpenses.setAdapter(recentExpenseAdapter);
    }

    private void setupChart() {
        binding.chartDonut.setOnCategorySelectedListener(summary -> {
            // Optional callback when user taps a donut slice
        });
    }

    private void setupObservers() {
        dashboardViewModel.getSelectedMonth().observe(getViewLifecycleOwner(), calendar -> {
            if (calendar != null) {
                binding.tvSelectedMonth.setText(DateUtils.formatMonthYear(calendar));
                updateBudgetCard();
            }
        });

        dashboardViewModel.getTotalSpend().observe(getViewLifecycleOwner(), total -> {
            currentCurrency = CurrencyUtils.getCurrencySymbol(requireContext());
            currentTotalSpend = total != null ? total : 0.0;
            binding.tvTotalSpend.setText(CurrencyUtils.formatAmount(currentTotalSpend, currentCurrency));

            // Calculate daily average
            Calendar cal = dashboardViewModel.getSelectedMonth().getValue();
            int days = (cal != null) ? DateUtils.getDaysInMonth(cal) : 30;
            double dailyAvg = (days > 0) ? (currentTotalSpend / days) : 0.0;
            binding.tvDailyAverage.setText(CurrencyUtils.formatAmount(dailyAvg, currentCurrency));

            updateBudgetCard();
        });

        dashboardViewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            updateBudgetCard();
        });

        dashboardViewModel.getExpenseCount().observe(getViewLifecycleOwner(), count -> {
            int c = count != null ? count : 0;
            binding.tvTransactionCount.setText(String.valueOf(c));
        });

        dashboardViewModel.getCategorySpend().observe(getViewLifecycleOwner(), summaries -> {
            currentCurrency = CurrencyUtils.getCurrencySymbol(requireContext());
            binding.chartDonut.setData(summaries, currentCurrency);

            if (summaries != null && !summaries.isEmpty()) {
                binding.tvTopCategory.setText(summaries.get(0).categoryName);
                binding.rvCategoryBreakdown.setVisibility(View.VISIBLE);
                binding.tvEmptyCategories.setVisibility(View.GONE);
                categorySummaryAdapter.setData(summaries, currentCurrency, currentTotalSpend);
            } else {
                binding.tvTopCategory.setText("—");
                binding.rvCategoryBreakdown.setVisibility(View.GONE);
                binding.tvEmptyCategories.setVisibility(View.VISIBLE);
                categorySummaryAdapter.setData(null, currentCurrency, 0.0);
            }
        });

        dashboardViewModel.getRecentExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                binding.rvRecentExpenses.setVisibility(View.VISIBLE);
                binding.tvEmptyRecent.setVisibility(View.GONE);
                recentExpenseAdapter.submitList(expenses);
            } else {
                binding.rvRecentExpenses.setVisibility(View.GONE);
                binding.tvEmptyRecent.setVisibility(View.VISIBLE);
                recentExpenseAdapter.submitList(null);
            }
        });
    }

    private void confirmDeleteExpense(ExpenseWithDetails item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_expense)
                .setMessage(R.string.delete_expense_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    expenseViewModel.deleteExpense(item.expense, () -> {
                        if (isAdded()) {
                            MonthlyExpenseWidgetProvider.updateAllWidgets(requireContext().getApplicationContext());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
