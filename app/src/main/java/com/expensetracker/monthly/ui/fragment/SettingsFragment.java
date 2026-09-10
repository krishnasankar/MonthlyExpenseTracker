package com.expensetracker.monthly.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import com.expensetracker.monthly.ui.dialog.SetBudgetDialogFragment;
import com.expensetracker.monthly.ui.dialog.SetCategoryBudgetDialogFragment;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;
import com.expensetracker.monthly.util.BudgetUtils;
import com.expensetracker.monthly.util.CurrencyUtils;
import androidx.core.content.ContextCompat;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private CategoryViewModel categoryViewModel;
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

        setupCurrencyPicker();
        setupThemePicker();
        setupBudgetSetting();
        setupCategoryManagement();
        setupRecurringSetting();
        setupSecuritySetting();
        setupExportSetting();
        setupObservers();
    }

    private void setupRecurringSetting() {
        binding.cardRecurringBills.setOnClickListener(v -> {
            com.expensetracker.monthly.ui.dialog.ManageRecurringDialogFragment.newInstance()
                    .show(getChildFragmentManager(), com.expensetracker.monthly.ui.dialog.ManageRecurringDialogFragment.TAG);
        });
    }

    private void setupSecuritySetting() {
        binding.switchAppLock.setChecked(com.expensetracker.monthly.util.BiometricUtils.isAppLockEnabled(requireContext()));
        binding.switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!com.expensetracker.monthly.util.BiometricUtils.isBiometricOrDeviceCredentialAvailable(requireContext())) {
                    binding.switchAppLock.setChecked(false);
                    Toast.makeText(requireContext(), R.string.biometric_not_supported, Toast.LENGTH_LONG).show();
                    return;
                }
                com.expensetracker.monthly.util.BiometricUtils.showBiometricPrompt(requireActivity(), new com.expensetracker.monthly.util.BiometricUtils.BiometricAuthListener() {
                    @Override
                    public void onSuccess() {
                        com.expensetracker.monthly.util.BiometricUtils.setAppLockEnabled(requireContext(), true);
                        Toast.makeText(requireContext(), R.string.app_lock_enabled, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        binding.switchAppLock.setChecked(false);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                com.expensetracker.monthly.util.BiometricUtils.setAppLockEnabled(requireContext(), false);
                Toast.makeText(requireContext(), R.string.app_lock_disabled, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupExportSetting() {
        binding.cardExportStatements.setOnClickListener(v -> {
            com.expensetracker.monthly.ui.dialog.ExportStatementDialogFragment.newInstance(System.currentTimeMillis())
                    .show(getChildFragmentManager(), com.expensetracker.monthly.ui.dialog.ExportStatementDialogFragment.TAG);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBudgetDisplay();
        binding.switchAppLock.setChecked(com.expensetracker.monthly.util.BiometricUtils.isAppLockEnabled(requireContext()));
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
                updateBudgetDisplay();
                Toast.makeText(requireContext(), "Currency set to " + symbol, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBudgetSetting() {
        binding.layoutSettingsBudget.setOnClickListener(v -> showBudgetDialog());
        updateBudgetDisplay();
    }

    private void updateBudgetDisplay() {
        if (binding == null || !isAdded()) return;
        if (BudgetUtils.hasMonthlyBudget(requireContext())) {
            double budget = BudgetUtils.getMonthlyBudget(requireContext());
            binding.tvSettingsBudgetVal.setText(CurrencyUtils.formatAmount(budget, requireContext()));
            binding.tvSettingsBudgetVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        } else {
            binding.tvSettingsBudgetVal.setText(R.string.set_budget);
            binding.tvSettingsBudgetVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));
        }
    }

    private void showBudgetDialog() {
        SetBudgetDialogFragment dialog = SetBudgetDialogFragment.newInstance();
        dialog.setOnBudgetChangeListener(this::updateBudgetDisplay);
        dialog.show(getChildFragmentManager(), SetBudgetDialogFragment.TAG);
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
            public void onEditCategory(Category category) {
                AddCategoryDialogFragment.newEditInstance(category)
                        .show(getChildFragmentManager(), AddCategoryDialogFragment.TAG);
            }

            @Override
            public void onDeleteCategory(Category category) {
                confirmDeleteCategory(category);
            }

            @Override
            public void onEditSubcategory(Category category, Subcategory subcategory) {
                AddSubcategoryDialogFragment.newEditInstance(
                        subcategory.getId(),
                        category.getId(),
                        subcategory.getName(),
                        category.getName()
                ).show(getChildFragmentManager(), AddSubcategoryDialogFragment.TAG);
            }

            @Override
            public void onDeleteSubcategory(Subcategory subcategory) {
                confirmDeleteSubcategory(subcategory);
            }

            @Override
            public void onEditCategoryBudget(Category category) {
                SetCategoryBudgetDialogFragment.newInstance(
                        category.getId(),
                        category.getName(),
                        category.getColorHex(),
                        category.getBudgetAmount()
                ).show(getChildFragmentManager(), SetCategoryBudgetDialogFragment.TAG);
            }
        });

        binding.rvManageCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvManageCategories.setAdapter(categoryAdapter);
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
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Category")
                .setMessage(message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    categoryViewModel.deleteCategory(category.getId(), (deleted, activeExpenseCount) -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (deleted) {
                                    Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setTitle(R.string.cannot_delete_category_title)
                                            .setMessage(getString(R.string.cannot_delete_category_message, category.getName(), activeExpenseCount))
                                            .setPositiveButton(android.R.string.ok, null)
                                            .show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteSubcategory(Subcategory subcategory) {
        String message = getString(R.string.delete_subcategory_confirm, subcategory.getName());
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Subcategory")
                .setMessage(message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    categoryViewModel.deleteSubcategory(subcategory.getId(), (deleted, activeExpenseCount) -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (deleted) {
                                    Toast.makeText(requireContext(), "Subcategory deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setTitle(R.string.cannot_delete_subcategory_title)
                                            .setMessage(getString(R.string.cannot_delete_subcategory_message, subcategory.getName(), activeExpenseCount))
                                            .setPositiveButton(android.R.string.ok, null)
                                            .show();
                                }
                            });
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
