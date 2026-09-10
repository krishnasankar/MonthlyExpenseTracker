package com.expensetracker.monthly.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.model.RecurringExpenseWithDetails;
import com.expensetracker.monthly.databinding.ItemRecurringExpenseBinding;
import com.expensetracker.monthly.util.CurrencyUtils;

public class RecurringExpenseAdapter extends ListAdapter<RecurringExpenseWithDetails, RecurringExpenseAdapter.ViewHolder> {

    private String currencySymbol = "₹";

    public interface OnRecurringActionListener {
        void onRecurringClick(RecurringExpenseWithDetails item);
        void onToggleActive(RecurringExpenseWithDetails item, boolean isActive);
    }

    private final OnRecurringActionListener listener;

    public RecurringExpenseAdapter(OnRecurringActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<RecurringExpenseWithDetails> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RecurringExpenseWithDetails>() {
                @Override
                public boolean areItemsTheSame(@NonNull RecurringExpenseWithDetails oldItem, @NonNull RecurringExpenseWithDetails newItem) {
                    return oldItem.recurringExpense.getId() == newItem.recurringExpense.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull RecurringExpenseWithDetails oldItem, @NonNull RecurringExpenseWithDetails newItem) {
                    return oldItem.recurringExpense.getAmount() == newItem.recurringExpense.getAmount()
                            && oldItem.recurringExpense.getDayOfMonth() == newItem.recurringExpense.getDayOfMonth()
                            && oldItem.recurringExpense.isActive() == newItem.recurringExpense.isActive()
                            && oldItem.recurringExpense.getTitle().equals(newItem.recurringExpense.getTitle())
                            && oldItem.recurringExpense.getFrequency().equals(newItem.recurringExpense.getFrequency());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecurringExpenseBinding binding = ItemRecurringExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecurringExpenseBinding binding;

        ViewHolder(ItemRecurringExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RecurringExpenseWithDetails item) {
            binding.tvTitle.setText(item.recurringExpense.getTitle());
            binding.tvAmount.setText(CurrencyUtils.formatAmount(item.recurringExpense.getAmount(), currencySymbol));

            // Category & Subcategory info
            StringBuilder catBuilder = new StringBuilder();
            if (item.category != null) {
                catBuilder.append(item.category.getName());
                try {
                    int color = Color.parseColor(item.category.getColorHex());
                    binding.viewCategoryIndicator.setBackgroundColor(color);
                } catch (Exception e) {
                    binding.viewCategoryIndicator.setBackgroundColor(Color.parseColor("#1E88E5"));
                }
            } else {
                catBuilder.append("Uncategorized");
                binding.viewCategoryIndicator.setBackgroundColor(Color.GRAY);
            }

            if (item.subcategory != null) {
                catBuilder.append(" • ").append(item.subcategory.getName());
            }
            binding.tvCategoryInfo.setText(catBuilder.toString());

            // Schedule info
            String freqDisplay = com.expensetracker.monthly.data.entity.RecurringExpense.getFrequencyDisplayName(item.recurringExpense.getFrequency());
            String scheduleText = freqDisplay + " • " +
                    binding.getRoot().getContext().getString(R.string.due_day_format, item.recurringExpense.getDayOfMonth());
            binding.tvDueSchedule.setText(scheduleText);

            // Active Switch
            binding.switchActive.setOnCheckedChangeListener(null);
            binding.switchActive.setChecked(item.recurringExpense.isActive());

            float alpha = item.recurringExpense.isActive() ? 1.0f : 0.55f;
            binding.tvTitle.setAlpha(alpha);
            binding.tvCategoryInfo.setAlpha(alpha);
            binding.tvDueSchedule.setAlpha(alpha);
            binding.tvAmount.setAlpha(alpha);

            binding.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleActive(item, isChecked);
                }
            });

            binding.cardRecurring.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecurringClick(item);
                }
            });
        }
    }
}
