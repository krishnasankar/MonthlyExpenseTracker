package com.expensetracker.monthly.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.databinding.FragmentSettingsBinding;
import com.expensetracker.monthly.ui.adapter.CategoryExpandableAdapter;
import com.expensetracker.monthly.ui.dialog.AddCategoryDialogFragment;
import com.expensetracker.monthly.ui.dialog.AddSubcategoryDialogFragment;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;
import com.expensetracker.monthly.ui.viewmodel.DashboardViewModel;
import com.expensetracker.monthly.util.CurrencyUtils;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private CategoryViewModel categoryViewModel;
    private DashboardViewModel dashboardViewModel;
    private CategoryExpandableAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        setupCurrencyPicker();
        setupThemePicker();
        setupCategoryManagement();
        setupDemoDataButton();
        setupObservers();
    }

    private void setupCurrencyPicker() {
        String currentCurrency = CurrencyUtils.getCurrencySymbol(requireContext());
        switch (currentCurrency) {
            case "$":
                binding.toggleGroupCurrency.check(R.id.btn_curr_usd);
                break;
            case "€":
                binding.toggleGroupCurrency.check(R.id.btn_curr_eur);
                break;
            case "£":
                binding.toggleGroupCurrency.check(R.id.btn_curr_gbp);
                break;
            case "¥":
                binding.toggleGroupCurrency.check(R.id.btn_curr_jpy);
                break;
            default:
                binding.toggleGroupCurrency.check(R.id.btn_curr_inr);
                break;
        }

        binding.toggleGroupCurrency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String symbol = "₹";
                if (checkedId == R.id.btn_curr_usd) {
                    symbol = "$";
                } else if (checkedId == R.id.btn_curr_eur) {
                    symbol = "€";
                } else if (checkedId == R.id.btn_curr_gbp) {
                    symbol = "£";
                } else if (checkedId == R.id.btn_curr_jpy) {
                    symbol = "¥";
                }
                CurrencyUtils.setCurrencySymbol(requireContext(), symbol);
                Toast.makeText(requireContext(), "Currency set to " + symbol, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupThemePicker() {
        int currentTheme = com.expensetracker.monthly.util.ThemeUtils.getThemeMode(requireContext());
        if (currentTheme == com.expensetracker.monthly.util.ThemeUtils.THEME_LIGHT) {
            binding.toggleGroupTheme.check(R.id.btn_theme_light);
        } else if (currentTheme == com.expensetracker.monthly.util.ThemeUtils.THEME_DARK) {
            binding.toggleGroupTheme.check(R.id.btn_theme_dark);
        } else {
            binding.toggleGroupTheme.check(R.id.btn_theme_system);
        }

        binding.toggleGroupTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int mode = com.expensetracker.monthly.util.ThemeUtils.THEME_SYSTEM;
                String modeName = getString(R.string.theme_system);
                if (checkedId == R.id.btn_theme_light) {
                    mode = com.expensetracker.monthly.util.ThemeUtils.THEME_LIGHT;
                    modeName = getString(R.string.theme_light);
                } else if (checkedId == R.id.btn_theme_dark) {
                    mode = com.expensetracker.monthly.util.ThemeUtils.THEME_DARK;
                    modeName = getString(R.string.theme_dark);
                }
                com.expensetracker.monthly.util.ThemeUtils.setThemeMode(requireContext(), mode);
                Toast.makeText(requireContext(), "Theme set to " + modeName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryManagement() {
        binding.btnAddCategory.setOnClickListener(v -> {
            AddCategoryDialogFragment.newInstance()
                    .show(getChildFragmentManager(), AddCategoryDialogFragment.TAG);
        });

        categoryAdapter = new CategoryExpandableAdapter(new CategoryExpandableAdapter.OnCategoryManageListener() {
            @Override
            public void onAddSubcategory(Category category) {
                AddSubcategoryDialogFragment.newInstance(category.getId(), category.getName())
                        .show(getChildFragmentManager(), AddSubcategoryDialogFragment.TAG);
            }

            @Override
            public void onDeleteCategory(Category category) {
                confirmDeleteCategory(category);
            }

            @Override
            public void onDeleteSubcategory(Subcategory subcategory) {
                confirmDeleteSubcategory(subcategory);
            }
        });

        binding.rvManageCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvManageCategories.setAdapter(categoryAdapter);
    }

    private void setupDemoDataButton() {
        binding.btnSeedDemoData.setOnClickListener(v -> {
            binding.btnSeedDemoData.setEnabled(false);
            dashboardViewModel.seedDemoData(() -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.btnSeedDemoData.setEnabled(true);
                        Toast.makeText(requireContext(), R.string.sample_data_added, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void setupObservers() {
        categoryViewModel.getCategoriesWithSubcategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                binding.rvManageCategories.setVisibility(View.VISIBLE);
                binding.tvEmptyCategoriesManage.setVisibility(View.GONE);
                categoryAdapter.setData(categories);
            } else {
                binding.rvManageCategories.setVisibility(View.GONE);
                binding.tvEmptyCategoriesManage.setVisibility(View.VISIBLE);
                categoryAdapter.setData(null);
            }
        });
    }

    private void confirmDeleteCategory(Category category) {
        String message = getString(R.string.delete_category_confirm, category.getName());
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Category")
                .setMessage(message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    categoryViewModel.deleteCategory(category.getId(), () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteSubcategory(Subcategory subcategory) {
        String message = getString(R.string.delete_subcategory_confirm, subcategory.getName());
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Subcategory")
                .setMessage(message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    categoryViewModel.deleteSubcategory(subcategory.getId(), () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Subcategory deleted", Toast.LENGTH_SHORT).show()
                            );
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
