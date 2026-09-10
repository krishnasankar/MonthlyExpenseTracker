package com.expensetracker.monthly.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.expensetracker.monthly.data.model.RecurringExpenseWithDetails;
import com.expensetracker.monthly.databinding.DialogManageRecurringBinding;
import com.expensetracker.monthly.ui.adapter.RecurringExpenseAdapter;
import com.expensetracker.monthly.ui.viewmodel.RecurringExpenseViewModel;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.RecurringExpenseManager;

public class ManageRecurringDialogFragment extends DialogFragment {

    public static final String TAG = "ManageRecurringDialog";

    private DialogManageRecurringBinding binding;
    private RecurringExpenseViewModel recurringViewModel;
    private RecurringExpenseAdapter adapter;

    public static ManageRecurringDialogFragment newInstance() {
        return new ManageRecurringDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogManageRecurringBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recurringViewModel = new ViewModelProvider(requireActivity()).get(RecurringExpenseViewModel.class);

        String currencySymbol = CurrencyUtils.getCurrencySymbol(requireContext());

        adapter = new RecurringExpenseAdapter(new RecurringExpenseAdapter.OnRecurringActionListener() {
            @Override
            public void onRecurringClick(RecurringExpenseWithDetails item) {
                AddEditRecurringDialogFragment.newInstance(item)
                        .show(getChildFragmentManager(), AddEditRecurringDialogFragment.TAG);
            }

            @Override
            public void onToggleActive(RecurringExpenseWithDetails item, boolean isActive) {
                recurringViewModel.setRecurringExpenseActive(item.recurringExpense.getId(), isActive, () -> {
                    if (isActive && isAdded()) {
                        RecurringExpenseManager.checkAndProcessRecurringExpenses(requireContext().getApplicationContext());
                    }
                });
            }
        });
        adapter.setCurrencySymbol(currencySymbol);

        binding.rvRecurring.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecurring.setAdapter(adapter);

        binding.btnAddRecurring.setOnClickListener(v -> {
            AddEditRecurringDialogFragment.newInstance(null)
                    .show(getChildFragmentManager(), AddEditRecurringDialogFragment.TAG);
        });

        binding.btnClose.setOnClickListener(v -> dismiss());

        recurringViewModel.getAllRecurringExpenses().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                binding.rvRecurring.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
                adapter.submitList(items);
            } else {
                binding.rvRecurring.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                adapter.submitList(null);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
