package com.expensetracker.monthly.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.database.AppDatabase;
import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;
import com.expensetracker.monthly.data.repository.ExpenseRepository;
import com.expensetracker.monthly.databinding.DialogExportStatementBinding;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.DateUtils;
import com.expensetracker.monthly.util.ExportUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportStatementDialogFragment extends DialogFragment {

    public static final String TAG = "ExportStatementDialog";
    private static final String ARG_MONTH_MILLIS = "arg_month_millis";

    private DialogExportStatementBinding binding;
    private ExpenseRepository repository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Calendar targetMonth = Calendar.getInstance();

    public static ExportStatementDialogFragment newInstance(long monthMillis) {
        ExportStatementDialogFragment fragment = new ExportStatementDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_MONTH_MILLIS, monthMillis);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        if (getArguments() != null && getArguments().containsKey(ARG_MONTH_MILLIS)) {
            targetMonth.setTimeInMillis(getArguments().getLong(ARG_MONTH_MILLIS));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogExportStatementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ExpenseRepository(requireActivity().getApplication());

        String monthName = DateUtils.formatMonthYear(targetMonth);
        binding.rbRangeMonth.setText(getString(R.string.range_current_month) + " (" + monthName + ")");

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnExportShare.setOnClickListener(v -> performExport());
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

    private void performExport() {
        binding.btnExportShare.setEnabled(false);
        binding.progressExport.setVisibility(View.VISIBLE);

        boolean isPdf = binding.rbFormatPdf.isChecked();
        boolean isMonthOnly = binding.rbRangeMonth.isChecked();
        String currencySymbol = CurrencyUtils.getCurrencySymbol(requireContext());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long startMillis;
                long endMillis;
                String periodLabel;

                if (isMonthOnly) {
                    startMillis = DateUtils.getStartOfMonthMillis(targetMonth);
                    endMillis = DateUtils.getEndOfMonthMillis(targetMonth);
                    periodLabel = DateUtils.formatMonthYear(targetMonth);
                } else {
                    startMillis = 0L;
                    endMillis = Long.MAX_VALUE;
                    periodLabel = "All Recorded Expenses";
                }

                List<ExpenseWithDetails> expenses;
                List<CategorySpendSummary> categorySummaries;

                if (isMonthOnly) {
                    expenses = repository.getExpensesForDateRangeSync(startMillis, endMillis);
                    categorySummaries = repository.getMonthlyCategorySpendSync(startMillis, endMillis);
                } else {
                    expenses = repository.getAllExpensesSync();
                    categorySummaries = repository.getMonthlyCategorySpendSync(0L, Long.MAX_VALUE);
                }

                if (expenses == null || expenses.isEmpty()) {
                    mainHandler.post(() -> {
                        if (isAdded()) {
                            binding.progressExport.setVisibility(View.GONE);
                            binding.btnExportShare.setEnabled(true);
                            Toast.makeText(requireContext(), R.string.export_empty, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                double totalSpend = 0.0;
                for (ExpenseWithDetails e : expenses) {
                    if (e.expense != null) {
                        totalSpend += e.expense.getAmount();
                    }
                }

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File exportedFile;
                String mimeType;

                if (isPdf) {
                    String filename = "Expense_Statement_" + timestamp + ".pdf";
                    exportedFile = ExportUtils.generatePdfStatement(
                            requireContext(),
                            periodLabel,
                            expenses,
                            categorySummaries,
                            totalSpend,
                            currencySymbol,
                            filename
                    );
                    mimeType = "application/pdf";
                } else {
                    String filename = "Expense_Export_" + timestamp + ".csv";
                    exportedFile = ExportUtils.generateCsvFile(requireContext(), expenses, filename);
                    mimeType = "text/csv";
                }

                mainHandler.post(() -> {
                    if (isAdded()) {
                        binding.progressExport.setVisibility(View.GONE);
                        binding.btnExportShare.setEnabled(true);
                        Toast.makeText(requireContext(), R.string.export_success, Toast.LENGTH_SHORT).show();
                        ExportUtils.shareExportedFile(requireContext(), exportedFile, mimeType, getString(R.string.btn_export_share));
                        dismiss();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (isAdded()) {
                        binding.progressExport.setVisibility(View.GONE);
                        binding.btnExportShare.setEnabled(true);
                        Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
