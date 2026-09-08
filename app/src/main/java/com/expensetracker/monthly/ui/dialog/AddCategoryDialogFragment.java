package com.expensetracker.monthly.ui.dialog;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

public class AddCategoryDialogFragment extends DialogFragment {

    public static final String TAG = "AddCategoryDialog";

    private DialogAddCategoryBinding binding;
    private CategoryViewModel categoryViewModel;

    private static final String[] PALETTE_COLORS = {
        "#FF7043", "#42A5F5", "#AB47BC", "#FFA726", "#26A69A",
        "#EC407A", "#7E57C2", "#5C6BC0", "#29B6F6", "#66BB6A",
        "#8D6E63", "#78909C"
    };

    private String selectedColorHex = PALETTE_COLORS[0];

    public static AddCategoryDialogFragment newInstance() {
        return new AddCategoryDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
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

        setupColorPalette();

        binding.btnCancelCategory.setOnClickListener(v -> dismiss());
        binding.btnSaveCategory.setOnClickListener(v -> saveCategory());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void setupColorPalette() {
        int padding = (int) (6 * getResources().getDisplayMetrics().density);
        int size = (int) (40 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < PALETTE_COLORS.length; i++) {
            String colorHex = PALETTE_COLORS[i];
            int color = Color.parseColor(colorHex);

            RadioButton rb = new RadioButton(requireContext());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(size, size);
            params.setMargins(padding, padding, padding, padding);
            rb.setLayoutParams(params);
            rb.setButtonTintList(ColorStateList.valueOf(color));
            rb.setId(View.generateViewId());
            if (i == 0) {
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

        Category category = new Category(name, selectedColorHex, "custom");
        categoryViewModel.insertCategory(category, categoryId -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Category \"" + name + "\" added", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
