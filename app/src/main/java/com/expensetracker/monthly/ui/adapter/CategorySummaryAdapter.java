package com.expensetracker.monthly.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.databinding.ItemCategorySummaryBinding;
import com.expensetracker.monthly.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.SummaryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(CategorySpendSummary item);
    }

    private final List<CategorySpendSummary> items = new ArrayList<>();
    private String currencySymbol = "₹";
    private double totalSpend = 0.0;
    private OnCategoryClickListener listener;

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<CategorySpendSummary> newItems, String currencySymbol, double totalSpend) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        this.currencySymbol = currencySymbol != null ? currencySymbol : "$";
        this.totalSpend = totalSpend;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategorySummaryBinding binding = ItemCategorySummaryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SummaryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SummaryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategorySummaryBinding binding;

        public SummaryViewHolder(@NonNull ItemCategorySummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CategorySpendSummary item) {
            if (item == null) return;

            final Context context = binding.getRoot().getContext();

            binding.tvCategoryName.setText(item.categoryName);

            int catColor;
            try {
                catColor = Color.parseColor(item.colorHex);
            } catch (Exception e) {
                catColor = ContextCompat.getColor(context, R.color.primary);
            }
            binding.viewCategoryColorDot.setBackgroundColor(catColor);

            if (item.budgetAmount > 0.0) {
                // Category has an active budget
                String spentFormatted = CurrencyUtils.formatAmount(item.totalAmount, currencySymbol);
                String budgetFormatted = CurrencyUtils.formatAmount(item.budgetAmount, currencySymbol);
                binding.tvCategoryAmount.setText(spentFormatted + " / " + budgetFormatted);

                double percentage = (item.totalAmount / item.budgetAmount) * 100.0;
                int roundedPct = (int) Math.round(percentage);
                binding.tvCategoryPercentage.setText(roundedPct + "%");

                int progress = Math.min(100, Math.max(0, roundedPct));
                binding.progressCategoryShare.setProgress(progress);

                if (percentage >= 100.0) {
                    int errColor = ContextCompat.getColor(context, R.color.budget_error);
                    binding.tvCategoryPercentage.setTextColor(errColor);
                    binding.progressCategoryShare.setIndicatorColor(errColor);
                } else if (percentage >= 80.0) {
                    int warnColor = ContextCompat.getColor(context, R.color.budget_warning);
                    binding.tvCategoryPercentage.setTextColor(warnColor);
                    binding.progressCategoryShare.setIndicatorColor(warnColor);
                } else {
                    binding.tvCategoryPercentage.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                    binding.progressCategoryShare.setIndicatorColor(catColor);
                }
            } else {
                // No category budget set - display share of total monthly spend
                binding.tvCategoryAmount.setText(CurrencyUtils.formatAmount(item.totalAmount, currencySymbol));

                float percentage = (totalSpend > 0) ? (float) ((item.totalAmount / totalSpend) * 100f) : 0f;
                binding.tvCategoryPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percentage));
                binding.tvCategoryPercentage.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));

                int progress = Math.min(100, Math.round(percentage));
                binding.progressCategoryShare.setProgress(progress);
                binding.progressCategoryShare.setIndicatorColor(catColor);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(item);
                }
            });
        }
    }
}
