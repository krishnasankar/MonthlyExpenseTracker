package com.expensetracker.monthly.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.databinding.DialogSetCategoryBudgetBinding;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SetCategoryBudgetDialogFragment extends DialogFragment {

    public static final String TAG = "SetCategoryBudgetDialog";

    private static final String ARG_CATEGORY_ID = "arg_category_id";
    private static final String ARG_CATEGORY_NAME = "arg_category_name";
    private static final String ARG_CATEGORY_COLOR = "arg_category_color";
    private static final String ARG_CURRENT_BUDGET = "arg_current_budget";

    private DialogSetCategoryBudgetBinding binding;
    private CategoryViewModel categoryViewModel;

    private long categoryId;
    private String categoryName;
    private String categoryColor;
    private double currentBudget;

    public interface OnCategoryBudgetChangeListener {
        void onBudgetChanged();
    }

    private OnCategoryBudgetChangeListener listener;

    public static SetCategoryBudgetDialogFragment newInstance(
            long categoryId,
            String categoryName,
            String colorHex,
            double currentBudget
    ) {
        SetCategoryBudgetDialogFragment fragment = new SetCategoryBudgetDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        args.putString(ARG_CATEGORY_COLOR, colorHex);
        args.putDouble(ARG_CURRENT_BUDGET, currentBudget);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnCategoryBudgetChangeListener(OnCategoryBudgetChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        if (getArguments() != null) {
            categoryId = getArguments().getLong(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            categoryColor = getArguments().getString(ARG_CATEGORY_COLOR);
            currentBudget = getArguments().getDouble(ARG_CURRENT_BUDGET, 0.0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSetCategoryBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        String currency = CurrencyUtils.getCurrencySymbol(requireContext());
        binding.tilCategoryBudgetAmount.setPrefixText(currency + " ");

        if (categoryName != null) {
            binding.tvCategoryDialogName.setText(categoryName);
            binding.tvCategoryDialogSubtitle.setText(String.format(getString(R.string.category_budget_subtitle), categoryName));
        }

        if (categoryColor != null) {
            try {
                binding.viewCategoryColorDot.setBackgroundColor(Color.parseColor(categoryColor));
            } catch (Exception ignored) {
            }
        }

        if (currentBudget > 0.0) {
            if (currentBudget == Math.floor(currentBudget)) {
                binding.etCategoryBudgetAmount.setText(String.valueOf((long) currentBudget));
            } else {
                binding.etCategoryBudgetAmount.setText(String.valueOf(currentBudget));
            }
            binding.btnRemoveCategoryBudget.setVisibility(View.VISIBLE);
        } else {
            binding.btnRemoveCategoryBudget.setVisibility(View.GONE);
        }

        setupPresets(currency);

        binding.btnCancelCategoryBudget.setOnClickListener(v -> dismiss());
        binding.btnSaveCategoryBudget.setOnClickListener(v -> saveCategoryBudget());
        binding.btnRemoveCategoryBudget.setOnClickListener(v -> confirmRemoveBudget());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void setupPresets(String currency) {
        binding.chipCatPreset1.setText(currency + " 2,000");
        binding.chipCatPreset1.setOnClickListener(v -> binding.etCategoryBudgetAmount.setText("2000"));

        binding.chipCatPreset2.setText(currency + " 5,000");
        binding.chipCatPreset2.setOnClickListener(v -> binding.etCategoryBudgetAmount.setText("5000"));

        binding.chipCatPreset3.setText(currency + " 10,000");
        binding.chipCatPreset3.setOnClickListener(v -> binding.etCategoryBudgetAmount.setText("10000"));

        binding.chipCatPreset4.setText(currency + " 20,000");
        binding.chipCatPreset4.setOnClickListener(v -> binding.etCategoryBudgetAmount.setText("20000"));
    }

    private void saveCategoryBudget() {
        String amountStr = binding.etCategoryBudgetAmount.getText() != null ? binding.etCategoryBudgetAmount.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            binding.tilCategoryBudgetAmount.setError(getString(R.string.error_invalid_amount));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0.0) {
                binding.tilCategoryBudgetAmount.setError(getString(R.string.error_invalid_amount));
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilCategoryBudgetAmount.setError(getString(R.string.error_invalid_amount));
            return;
        }

        binding.tilCategoryBudgetAmount.setError(null);
        categoryViewModel.updateCategoryBudget(categoryId, amount, () -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onBudgetChanged();
                    }
                    String msg = String.format(getString(R.string.category_budget_updated), categoryName);
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
        });
    }

    private void confirmRemoveBudget() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.remove_budget)
                .setMessage(String.format(getString(R.string.remove_category_budget_confirm), categoryName))
                .setPositiveButton(R.string.delete, (d, which) -> {
                    categoryViewModel.updateCategoryBudget(categoryId, 0.0, () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                if (listener != null) {
                                    listener.onBudgetChanged();
                                }
                                String msg = String.format(getString(R.string.category_budget_removed), categoryName);
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                                dismiss();
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
