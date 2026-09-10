package com.expensetracker.monthly.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDropdownAdapter extends ArrayAdapter<CategoryDropdownAdapter.Item> {

    public static class Item {
        public final Category category;
        public final String title;
        public final boolean isAddAction;

        public Item(@NonNull Category category) {
            this.category = category;
            this.title = category.getName();
            this.isAddAction = false;
        }

        public Item(@NonNull String title, boolean isAddAction) {
            this.category = null;
            this.title = title;
            this.isAddAction = isAddAction;
        }

        @NonNull
        @Override
        public String toString() {
            return title;
        }
    }

    private final LayoutInflater inflater;
    private final int primaryColor;

    public CategoryDropdownAdapter(@NonNull Context context, @NonNull List<Category> categories) {
        super(context, R.layout.item_category_dropdown, buildItems(context, categories));
        this.inflater = LayoutInflater.from(context);
        this.primaryColor = ContextCompat.getColor(context, R.color.primary);
    }

    private static List<Item> buildItems(Context context, List<Category> categories) {
        List<Item> items = new ArrayList<>();
        if (categories != null) {
            for (Category cat : categories) {
                items.add(new Item(cat));
            }
        }
        items.add(new Item(context.getString(R.string.add_new_category_action), true));
        return items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView != null ? convertView : inflater.inflate(R.layout.item_category_dropdown, parent, false);

        View dotView = view.findViewById(R.id.view_category_dot);
        ImageView actionIcon = view.findViewById(R.id.iv_action_icon);
        TextView titleText = view.findViewById(R.id.tv_category_name);

        Item item = getItem(position);
        if (item != null) {
            if (item.isAddAction) {
                dotView.setVisibility(View.GONE);
                actionIcon.setVisibility(View.VISIBLE);
                actionIcon.setImageTintList(ColorStateList.valueOf(primaryColor));
                titleText.setText(item.title);
                titleText.setTextColor(primaryColor);
                titleText.setTypeface(null, Typeface.BOLD);
            } else {
                actionIcon.setVisibility(View.GONE);
                dotView.setVisibility(View.VISIBLE);
                int color = primaryColor;
                if (item.category != null && item.category.getColorHex() != null) {
                    try {
                        color = Color.parseColor(item.category.getColorHex());
                    } catch (Exception ignored) {}
                }
                dotView.setBackgroundTintList(ColorStateList.valueOf(color));
                titleText.setText(item.title);
                titleText.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
                titleText.setTypeface(null, Typeface.NORMAL);
            }
        }
        return view;
    }
}
