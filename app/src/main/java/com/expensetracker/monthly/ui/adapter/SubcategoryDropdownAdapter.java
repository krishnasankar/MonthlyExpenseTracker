package com.expensetracker.monthly.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.expensetracker.monthly.data.entity.Subcategory;

import java.util.ArrayList;
import java.util.List;

public class SubcategoryDropdownAdapter extends ArrayAdapter<SubcategoryDropdownAdapter.Item> {

    public static class Item {
        public final Subcategory subcategory;
        public final String title;
        public final boolean isNone;
        public final boolean isAddAction;

        public Item(@NonNull Subcategory subcategory) {
            this.subcategory = subcategory;
            this.title = subcategory.getName();
            this.isNone = false;
            this.isAddAction = false;
        }

        public Item(@NonNull String title, boolean isNone, boolean isAddAction) {
            this.subcategory = null;
            this.title = title;
            this.isNone = isNone;
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
    private final int textSecondaryColor;

    public SubcategoryDropdownAdapter(@NonNull Context context, @NonNull List<Subcategory> subcategories) {
        super(context, R.layout.item_category_dropdown, buildItems(context, subcategories));
        this.inflater = LayoutInflater.from(context);
        this.primaryColor = ContextCompat.getColor(context, R.color.primary);
        this.textSecondaryColor = ContextCompat.getColor(context, R.color.text_secondary);
    }

    private static List<Item> buildItems(Context context, List<Subcategory> subcategories) {
        List<Item> items = new ArrayList<>();
        items.add(new Item(context.getString(R.string.expense_subcategory_none), true, false));
        if (subcategories != null) {
            for (Subcategory sub : subcategories) {
                items.add(new Item(sub));
            }
        }
        items.add(new Item(context.getString(R.string.add_new_subcategory_action), false, true));
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
            } else if (item.isNone) {
                dotView.setVisibility(View.GONE);
                actionIcon.setVisibility(View.GONE);
                titleText.setText(item.title);
                titleText.setTextColor(textSecondaryColor);
                titleText.setTypeface(null, Typeface.ITALIC);
            } else {
                actionIcon.setVisibility(View.GONE);
                dotView.setVisibility(View.VISIBLE);
                dotView.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
                titleText.setText(item.title);
                titleText.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
                titleText.setTypeface(null, Typeface.NORMAL);
            }
        }
        return view;
    }
}
