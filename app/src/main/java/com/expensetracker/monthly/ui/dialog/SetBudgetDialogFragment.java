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
import com.expensetracker.monthly.databinding.DialogSetBudgetBinding;
import com.expensetracker.monthly.ui.viewmodel.DashboardViewModel;
import com.expensetracker.monthly.ui.widget.MonthlyExpenseWidgetProvider;
import com.expensetracker.monthly.util.BudgetUtils;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SetBudgetDialogFragment extends DialogFragment {

    public static final String TAG = "SetBudgetDialog";

    private DialogSetBudgetBinding binding;
    private DashboardViewModel dashboardViewModel;

    public interface OnBudgetChangeListener {
        void onBudgetChanged();
    }

    private OnBudgetChangeListener listener;

    public static SetBudgetDialogFragment newInstance() {
        return new SetBudgetDialogFragment();
    }

    public void setOnBudgetChangeListener(OnBudgetChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSetBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        String currency = CurrencyUtils.getCurrencySymbol(requireContext());
        binding.tilBudgetAmount.setPrefixText(currency + " ");

        double currentBudget = BudgetUtils.getMonthlyBudget(requireContext());
        if (currentBudget > 0.0) {
            binding.tvBudgetDialogTitle.setText(R.string.edit_budget);
            // Format cleanly without trailing decimals if whole number
            if (currentBudget == Math.floor(currentBudget)) {
                binding.etBudgetAmount.setText(String.valueOf((long) currentBudget));
            } else {
                binding.etBudgetAmount.setText(String.valueOf(currentBudget));
            }
            binding.btnRemoveBudget.setVisibility(View.VISIBLE);
        } else {
            binding.tvBudgetDialogTitle.setText(R.string.set_monthly_budget);
            binding.btnRemoveBudget.setVisibility(View.GONE);
        }

        setupPresets(currency);

        binding.btnCancelBudget.setOnClickListener(v -> dismiss());
        binding.btnSaveBudget.setOnClickListener(v -> saveBudget());
        binding.btnRemoveBudget.setOnClickListener(v -> confirmRemoveBudget());
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
        binding.chipPreset1.setText(currency + " 15,000");
        binding.chipPreset1.setOnClickListener(v -> binding.etBudgetAmount.setText("15000"));

        binding.chipPreset2.setText(currency + " 30,000");
        binding.chipPreset2.setOnClickListener(v -> binding.etBudgetAmount.setText("30000"));

        binding.chipPreset3.setText(currency + " 50,000");
        binding.chipPreset3.setOnClickListener(v -> binding.etBudgetAmount.setText("50000"));

        binding.chipPreset4.setText(currency + " 100,000");
        binding.chipPreset4.setOnClickListener(v -> binding.etBudgetAmount.setText("100000"));
    }

    private void saveBudget() {
        String amountStr = binding.etBudgetAmount.getText() != null ? binding.etBudgetAmount.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            binding.tilBudgetAmount.setError(getString(R.string.error_invalid_amount));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0.0) {
                binding.tilBudgetAmount.setError(getString(R.string.error_invalid_amount));
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilBudgetAmount.setError(getString(R.string.error_invalid_amount));
            return;
        }

        binding.tilBudgetAmount.setError(null);
        dashboardViewModel.setMonthlyBudget(amount);
        MonthlyExpenseWidgetProvider.updateAllWidgets(requireContext().getApplicationContext());

        if (listener != null) {
            listener.onBudgetChanged();
        }

        Toast.makeText(requireContext(), R.string.budget_updated, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void confirmRemoveBudget() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.remove_budget)
                .setMessage(R.string.remove_budget_confirm)
                .setPositiveButton(R.string.delete, (d, which) -> {
                    dashboardViewModel.clearMonthlyBudget();
                    MonthlyExpenseWidgetProvider.updateAllWidgets(requireContext().getApplicationContext());
                    if (listener != null) {
                        listener.onBudgetChanged();
                    }
                    Toast.makeText(requireContext(), R.string.budget_removed, Toast.LENGTH_SHORT).show();
                    dismiss();
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
