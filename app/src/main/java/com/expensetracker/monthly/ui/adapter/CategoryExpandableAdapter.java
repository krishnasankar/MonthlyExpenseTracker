package com.expensetracker.monthly.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.data.model.CategoryWithSubcategories;
import com.expensetracker.monthly.databinding.ItemCategoryManageBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class CategoryExpandableAdapter extends RecyclerView.Adapter<CategoryExpandableAdapter.CategoryViewHolder> {

    public interface OnCategoryManageListener {
        void onAddSubcategory(Category category);
        void onDeleteCategory(Category category);
        void onDeleteSubcategory(Subcategory subcategory);
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
                    chip.setClickable(false);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F1F3F5")));
                    chip.setTextColor(Color.parseColor("#212121"));
                    chip.setTextSize(12);

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
