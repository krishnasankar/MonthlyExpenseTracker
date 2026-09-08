package com.expensetracker.monthly.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.data.model.CategorySpendSummary;
import com.expensetracker.monthly.databinding.ItemCategorySummaryBinding;
import com.expensetracker.monthly.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.SummaryViewHolder> {

    private final List<CategorySpendSummary> items = new ArrayList<>();
    private String currencySymbol = "$";
    private double totalSpend = 0.0;

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

            binding.tvCategoryName.setText(item.categoryName);
            binding.tvCategoryAmount.setText(CurrencyUtils.formatAmount(item.totalAmount, currencySymbol));

            float percentage = (totalSpend > 0) ? (float) ((item.totalAmount / totalSpend) * 100f) : 0f;
            binding.tvCategoryPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percentage));

            int progress = Math.min(100, Math.round(percentage));
            binding.progressCategoryShare.setProgress(progress);

            int color;
            try {
                color = Color.parseColor(item.colorHex);
            } catch (Exception e) {
                color = Color.parseColor("#1E88E5");
            }

            binding.viewCategoryColorDot.setBackgroundColor(color);
            binding.progressCategoryShare.setIndicatorColor(color);
        }
    }
}
