package com.expensetracker.monthly.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;
import com.expensetracker.monthly.databinding.FragmentExpensesListBinding;
import com.expensetracker.monthly.ui.adapter.ExpenseAdapter;
import com.expensetracker.monthly.ui.dialog.AddEditExpenseDialogFragment;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;
import com.expensetracker.monthly.ui.viewmodel.ExpenseViewModel;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.DateUtils;
import com.google.android.material.chip.Chip;

import java.util.Calendar;
import java.util.List;

public class ExpensesListFragment extends Fragment {

    private FragmentExpensesListBinding binding;
    private ExpenseViewModel expenseViewModel;
    private CategoryViewModel categoryViewModel;
    private ExpenseAdapter expenseAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExpensesListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        setupMonthNavigation();
        setupSearch();
        setupRecyclerView();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (expenseAdapter != null) {
            expenseAdapter.setCurrencySymbol(CurrencyUtils.getCurrencySymbol(requireContext()));
        }
    }

    private void setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> expenseViewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> expenseViewModel.nextMonth());
        binding.layoutMonthPicker.setOnClickListener(v -> showMonthPicker());
    }

    private void showMonthPicker() {
        Calendar current = expenseViewModel.getSelectedMonth().getValue();
        if (current == null) current = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> expenseViewModel.setMonth(year, month),
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                expenseViewModel.setSearchQuery(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        String currency = CurrencyUtils.getCurrencySymbol(requireContext());
        expenseAdapter = new ExpenseAdapter(new ExpenseAdapter.OnExpenseClickListener() {
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
        expenseAdapter.setCurrencySymbol(currency);

        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpenses.setAdapter(expenseAdapter);
    }

    private void setupObservers() {
        expenseViewModel.getSelectedMonth().observe(getViewLifecycleOwner(), calendar -> {
            if (calendar != null) {
                binding.tvSelectedMonth.setText(DateUtils.formatMonthYear(calendar));
            }
        });

        expenseViewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                binding.rvExpenses.setVisibility(View.VISIBLE);
                binding.layoutEmptyExpenses.setVisibility(View.GONE);
                expenseAdapter.submitList(expenses);
            } else {
                binding.rvExpenses.setVisibility(View.GONE);
                binding.layoutEmptyExpenses.setVisibility(View.VISIBLE);
                expenseAdapter.submitList(null);
            }
        });

        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), this::populateCategoryChips);
    }

    private void populateCategoryChips(List<Category> categories) {
        binding.chipGroupCategories.removeAllViews();

        // "All Categories" chip
        Chip allChip = new Chip(requireContext());
        allChip.setText(R.string.all_categories);
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setId(View.generateViewId());
        allChip.setOnClickListener(v -> expenseViewModel.setCategoryFilter(-1L));
        binding.chipGroupCategories.addView(allChip);

        if (categories != null) {
            for (Category cat : categories) {
                Chip chip = new Chip(requireContext());
                chip.setText(cat.getName());
                chip.setCheckable(true);
                chip.setId(View.generateViewId());
                chip.setOnClickListener(v -> expenseViewModel.setCategoryFilter(cat.getId()));
                binding.chipGroupCategories.addView(chip);
            }
        }
    }

    private void confirmDeleteExpense(ExpenseWithDetails item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_expense)
                .setMessage(R.string.delete_expense_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    expenseViewModel.deleteExpense(item.expense, null);
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
