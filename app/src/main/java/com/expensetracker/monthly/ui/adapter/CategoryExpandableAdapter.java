package com.expensetracker.monthly.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.data.model.CategoryWithSubcategories;
import com.expensetracker.monthly.databinding.ItemCategoryManageBinding;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class CategoryExpandableAdapter extends RecyclerView.Adapter<CategoryExpandableAdapter.CategoryViewHolder> {

    public interface OnCategoryManageListener {
        void onAddSubcategory(Category category);
        void onEditCategory(Category category);
        void onDeleteCategory(Category category);
        void onEditSubcategory(Category category, Subcategory subcategory);
        void onDeleteSubcategory(Subcategory subcategory);
        void onEditCategoryBudget(Category category);
    }

    private final List<CategoryWithSubcategories> items = new ArrayList<>();
    private final OnCategoryManageListener listener;

    public CategoryExpandableAdapter(OnCategoryManageListener listener) {
        this.listener = listener;
    }

    public void setData(List<CategoryWithSubcategories> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryManageBinding binding = ItemCategoryManageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryManageBinding binding;

        public CategoryViewHolder(@NonNull ItemCategoryManageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CategoryWithSubcategories item) {
            if (item == null || item.category == null) return;

            final Category category = item.category;
            final List<Subcategory> subcategories = item.subcategories;
            final Context context = binding.getRoot().getContext();

            binding.tvCategoryName.setText(category.getName());
            binding.tvCategoryName.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCategory(category);
                }
            });

            String currency = CurrencyUtils.getCurrencySymbol(context);
            if (category.getBudgetAmount() > 0.0) {
                binding.tvCategoryBudgetBadge.setText(String.format(context.getString(R.string.category_budget_badge), CurrencyUtils.formatAmount(category.getBudgetAmount(), currency)));
                binding.tvCategoryBudgetBadge.setTextColor(ContextCompat.getColor(context, R.color.primary));
            } else {
                binding.tvCategoryBudgetBadge.setText(R.string.category_no_budget_badge);
                binding.tvCategoryBudgetBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            binding.tvCategoryBudgetBadge.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCategoryBudget(category);
                }
            });

            int color;
            try {
                color = Color.parseColor(category.getColorHex());
            } catch (Exception e) {
                color = Color.parseColor("#1E88E5");
            }
            binding.viewCategoryColor.setBackgroundColor(color);

            binding.btnAddSubcategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddSubcategory(category);
                }
            });

            binding.btnEditCategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCategory(category);
                }
            });

            binding.btnDeleteCategory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCategory(category);
                }
            });

            // Populate Subcategories chips
            binding.chipGroupSubcategories.removeAllViews();
            if (subcategories != null && !subcategories.isEmpty()) {
                binding.tvNoSubcategories.setVisibility(View.GONE);
                binding.chipGroupSubcategories.setVisibility(View.VISIBLE);

                for (Subcategory sub : subcategories) {
                    Chip chip = new Chip(context);
                    chip.setText(sub.getName());
                    chip.setCloseIconVisible(true);
                    chip.setClickable(true);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F1F3F5")));
                    chip.setTextColor(Color.parseColor("#212121"));
                    chip.setTextSize(12);
                    chip.setChipIconResource(R.drawable.ic_edit);
                    chip.setChipIconVisible(true);
                    chip.setChipIconSize((int) (14 * context.getResources().getDisplayMetrics().density));
                    chip.setChipIconTint(ColorStateList.valueOf(Color.parseColor("#757575")));
                    androidx.appcompat.widget.TooltipCompat.setTooltipText(chip, context.getString(R.string.edit_subcategory));

                    chip.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onEditSubcategory(category, sub);
                        }
                    });

                    chip.setOnCloseIconClickListener(v -> {
                        if (listener != null) {
                            listener.onDeleteSubcategory(sub);
                        }
                    });

                    binding.chipGroupSubcategories.addView(chip);
                }
            } else {
                binding.tvNoSubcategories.setVisibility(View.VISIBLE);
                binding.chipGroupSubcategories.setVisibility(View.GONE);
            }
        }
    }
}
