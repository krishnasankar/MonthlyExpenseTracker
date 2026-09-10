package com.expensetracker.monthly.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.R;
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

    public static CharSequence formatCategoryText(Context context, String categoryName, String subcategoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            categoryName = "Uncategorized";
        } else {
            categoryName = categoryName.trim();
        }

        if (subcategoryName == null || subcategoryName.trim().isEmpty()) {
            return categoryName;
        }

        String subName = subcategoryName.trim();
        String separator = " › ";
        String fullText = categoryName + separator + subName;

        if (context == null) {
            return fullText;
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder(fullText);

        int categoryColor = ContextCompat.getColor(context, R.color.text_secondary);
        int separatorColor = ContextCompat.getColor(context, R.color.text_tertiary);
        int subcategoryColor = ContextCompat.getColor(context, R.color.secondary);

        int catEnd = categoryName.length();
        int sepEnd = catEnd + separator.length();
        int subEnd = fullText.length();

        ssb.setSpan(new ForegroundColorSpan(categoryColor), 0, catEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(separatorColor), catEnd, sepEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(subcategoryColor), sepEnd, subEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), sepEnd, subEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ssb;
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
                try {
                    int color = Color.parseColor(item.category.getColorHex());
                    binding.viewCategoryIndicator.setBackgroundColor(color);
                } catch (Exception e) {
                    binding.viewCategoryIndicator.setBackgroundColor(Color.parseColor("#1E88E5"));
                }
            } else {
                binding.viewCategoryIndicator.setBackgroundColor(Color.GRAY);
            }

            String categoryName = item.category != null ? item.category.getName() : null;
            String subcategoryName = item.subcategory != null ? item.subcategory.getName() : null;

            binding.tvExpenseCategory.setText(formatCategoryText(
                    binding.getRoot().getContext(),
                    categoryName,
                    subcategoryName
            ));

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
