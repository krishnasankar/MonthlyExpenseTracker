package com.expensetracker.monthly.ui.dialog;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.databinding.DialogAddCategoryBinding;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;
import com.expensetracker.monthly.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddCategoryDialogFragment extends DialogFragment {

    public static final String TAG = "AddCategoryDialog";
    private static final String ARG_CATEGORY_ID = "arg_cat_id";
    private static final String ARG_CATEGORY_NAME = "arg_cat_name";
    private static final String ARG_CATEGORY_COLOR = "arg_cat_color";
    private static final String ARG_CATEGORY_BUDGET = "arg_cat_budget";
    private static final String ARG_CATEGORY_ICON = "arg_cat_icon";

    private DialogAddCategoryBinding binding;
    private CategoryViewModel categoryViewModel;

    private static final String[] PALETTE_COLORS = {
        "#FF7043", "#42A5F5", "#AB47BC", "#FFA726", "#26A69A",
        "#EC407A", "#7E57C2", "#5C6BC0", "#29B6F6", "#66BB6A",
        "#8D6E63", "#78909C"
    };

    private String selectedColorHex = PALETTE_COLORS[0];
    private boolean isEditMode = false;
    private long categoryId = -1;
    private String initialName;
    private double initialBudget = 0.0;
    private String initialIcon = "custom";

    public static AddCategoryDialogFragment newInstance() {
        return new AddCategoryDialogFragment();
    }

    public static AddCategoryDialogFragment newEditInstance(Category category) {
        AddCategoryDialogFragment fragment = new AddCategoryDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, category.getId());
        args.putString(ARG_CATEGORY_NAME, category.getName());
        args.putString(ARG_CATEGORY_COLOR, category.getColorHex());
        args.putDouble(ARG_CATEGORY_BUDGET, category.getBudgetAmount());
        args.putString(ARG_CATEGORY_ICON, category.getIconName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        if (getArguments() != null && getArguments().containsKey(ARG_CATEGORY_ID)) {
            isEditMode = true;
            categoryId = getArguments().getLong(ARG_CATEGORY_ID);
            initialName = getArguments().getString(ARG_CATEGORY_NAME);
            selectedColorHex = getArguments().getString(ARG_CATEGORY_COLOR, PALETTE_COLORS[0]);
            initialBudget = getArguments().getDouble(ARG_CATEGORY_BUDGET, 0.0);
            initialIcon = getArguments().getString(ARG_CATEGORY_ICON, "custom");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        String currency = CurrencyUtils.getCurrencySymbol(requireContext());
        binding.tilCatBudget.setPrefixText(currency + " ");

        if (isEditMode) {
            binding.tvCategoryDialogTitle.setText(R.string.edit_category);
            if (initialName != null) {
                binding.etCatName.setText(initialName);
            }
            if (initialBudget > 0.0) {
                if (initialBudget == Math.floor(initialBudget)) {
                    binding.etCatBudget.setText(String.valueOf((long) initialBudget));
                } else {
                    binding.etCatBudget.setText(String.valueOf(initialBudget));
                }
            }
        } else {
            binding.tvCategoryDialogTitle.setText(R.string.add_category);
        }

        setupColorPalette();

        binding.btnCancelCategory.setOnClickListener(v -> dismiss());
        binding.btnSaveCategory.setOnClickListener(v -> saveCategory());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void setupColorPalette() {
        int padding = (int) (6 * getResources().getDisplayMetrics().density);
        int size = (int) (40 * getResources().getDisplayMetrics().density);

        List<String> palette = new ArrayList<>(Arrays.asList(PALETTE_COLORS));
        if (selectedColorHex != null && !palette.contains(selectedColorHex)) {
            palette.add(0, selectedColorHex);
        }

        for (int i = 0; i < palette.size(); i++) {
            String colorHex = palette.get(i);
            int color;
            try {
                color = Color.parseColor(colorHex);
            } catch (Exception e) {
                color = Color.parseColor("#1E88E5");
            }

            RadioButton rb = new RadioButton(requireContext());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(size, size);
            params.setMargins(padding, padding, padding, padding);
            rb.setLayoutParams(params);
            rb.setButtonTintList(ColorStateList.valueOf(color));
            rb.setId(View.generateViewId());

            if (colorHex.equalsIgnoreCase(selectedColorHex)) {
                rb.setChecked(true);
            }

            rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedColorHex = colorHex;
                }
            });

            binding.rgColors.addView(rb);
        }
    }

    private void saveCategory() {
        String name = binding.etCatName.getText() != null ? binding.etCatName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.tilCatName.setError(getString(R.string.error_required_fields));
            return;
        }
        binding.tilCatName.setError(null);

        String budgetStr = binding.etCatBudget.getText() != null ? binding.etCatBudget.getText().toString().trim() : "";
        double budgetAmount = 0.0;
        if (!budgetStr.isEmpty()) {
            try {
                budgetAmount = Double.parseDouble(budgetStr);
                if (budgetAmount < 0.0) {
                    binding.tilCatBudget.setError(getString(R.string.error_invalid_amount));
                    return;
                }
            } catch (NumberFormatException e) {
                binding.tilCatBudget.setError(getString(R.string.error_invalid_amount));
                return;
            }
        }
        binding.tilCatBudget.setError(null);

        if (isEditMode) {
            Category category = new Category(name, selectedColorHex, initialIcon != null ? initialIcon : "custom");
            category.setId(categoryId);
            category.setBudgetAmount(budgetAmount);

            categoryViewModel.updateCategory(category, (success, errorMessage) -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(requireContext(), getString(R.string.category_updated, name), Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            binding.tilCatName.setError(errorMessage != null ? errorMessage : "Failed to update category");
                        }
                    });
                }
            });
        } else {
            Category category = new Category(name, selectedColorHex, "custom");
            category.setBudgetAmount(budgetAmount);

            categoryViewModel.insertCategory(category, (success, errorMessage) -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(requireContext(), getString(R.string.category_added, name), Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            binding.tilCatName.setError(errorMessage != null ? errorMessage : "Failed to add category");
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
