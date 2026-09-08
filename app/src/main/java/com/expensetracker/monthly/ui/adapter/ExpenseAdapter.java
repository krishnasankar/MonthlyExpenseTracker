package com.expensetracker.monthly.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.data.model.ExpenseWithDetails;
import com.expensetracker.monthly.databinding.ItemExpenseBinding;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.DateUtils;

import java.util.Objects;

public class ExpenseAdapter extends ListAdapter<ExpenseWithDetails, ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseWithDetails expenseWithDetails);
        void onExpenseDelete(ExpenseWithDetails expenseWithDetails);
    }

    private final OnExpenseClickListener listener;
    private String currencySymbol = "₹";

    public ExpenseAdapter(OnExpenseClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol != null ? currencySymbol : "$";
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<ExpenseWithDetails> DIFF_CALLBACK = new DiffUtil.ItemCallback<ExpenseWithDetails>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExpenseWithDetails oldItem, @NonNull ExpenseWithDetails newItem) {
            return oldItem.expense.getId() == newItem.expense.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExpenseWithDetails oldItem, @NonNull ExpenseWithDetails newItem) {
            return Objects.equals(oldItem.expense, newItem.expense) &&
                    Objects.equals(oldItem.category, newItem.category) &&
                    Objects.equals(oldItem.subcategory, newItem.subcategory);
        }
    };

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;

        public ExpenseViewHolder(@NonNull ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ExpenseWithDetails item) {
            if (item == null || item.expense == null) return;

            binding.tvExpenseTitle.setText(item.expense.getTitle());
            binding.tvExpenseAmount.setText("-" + CurrencyUtils.formatAmount(item.expense.getAmount(), currencySymbol));
            binding.tvExpenseDate.setText(DateUtils.formatDate(item.expense.getDateMillis()) + " • " + DateUtils.formatTime(item.expense.getDateMillis()));

            if (item.category != null) {
                binding.tvExpenseCategory.setText(item.category.getName());
                try {
                    int color = Color.parseColor(item.category.getColorHex());
                    binding.viewCategoryIndicator.setBackgroundColor(color);
                } catch (Exception e) {
                    binding.viewCategoryIndicator.setBackgroundColor(Color.parseColor("#1E88E5"));
                }
            } else {
                binding.tvExpenseCategory.setText("Uncategorized");
                binding.viewCategoryIndicator.setBackgroundColor(Color.GRAY);
            }

            if (item.subcategory != null && item.subcategory.getName() != null && !item.subcategory.getName().isEmpty()) {
                binding.tvSubcategorySeparator.setVisibility(View.VISIBLE);
                binding.tvExpenseSubcategory.setVisibility(View.VISIBLE);
                binding.tvExpenseSubcategory.setText(item.subcategory.getName());
            } else {
                binding.tvSubcategorySeparator.setVisibility(View.GONE);
                binding.tvExpenseSubcategory.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExpenseClick(item);
                }
            });

            binding.btnDeleteExpense.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExpenseDelete(item);
                }
            });
        }
    }
}
