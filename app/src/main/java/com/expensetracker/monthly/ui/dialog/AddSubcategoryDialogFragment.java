package com.expensetracker.monthly.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.databinding.DialogAddSubcategoryBinding;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;

public class AddSubcategoryDialogFragment extends DialogFragment {

    public static final String TAG = "AddSubcategoryDialog";
    private static final String ARG_CATEGORY_ID = "arg_cat_id";
    private static final String ARG_CATEGORY_NAME = "arg_cat_name";
    private static final String ARG_SUBCATEGORY_ID = "arg_subcat_id";
    private static final String ARG_SUBCATEGORY_NAME = "arg_subcat_name";

    private DialogAddSubcategoryBinding binding;
    private CategoryViewModel categoryViewModel;
    private long categoryId;
    private String categoryName;

    private boolean isEditMode = false;
    private long subcategoryId = -1;
    private String initialSubcategoryName;

    public static AddSubcategoryDialogFragment newInstance(long categoryId, String categoryName) {
        AddSubcategoryDialogFragment fragment = new AddSubcategoryDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddSubcategoryDialogFragment newEditInstance(long subcategoryId, long categoryId, String subcategoryName, String categoryName) {
        AddSubcategoryDialogFragment fragment = new AddSubcategoryDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SUBCATEGORY_ID, subcategoryId);
        args.putLong(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_SUBCATEGORY_NAME, subcategoryName);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        if (getArguments() != null) {
            categoryId = getArguments().getLong(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            if (getArguments().containsKey(ARG_SUBCATEGORY_ID)) {
                isEditMode = true;
                subcategoryId = getArguments().getLong(ARG_SUBCATEGORY_ID);
                initialSubcategoryName = getArguments().getString(ARG_SUBCATEGORY_NAME);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddSubcategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        binding.tvParentCategoryName.setText("Under: " + categoryName);

        if (isEditMode) {
            binding.tvSubcategoryDialogTitle.setText(R.string.edit_subcategory);
            if (initialSubcategoryName != null) {
                binding.etSubcatName.setText(initialSubcategoryName);
            }
        } else {
            binding.tvSubcategoryDialogTitle.setText(R.string.add_subcategory);
        }

        binding.btnCancelSubcat.setOnClickListener(v -> dismiss());
        binding.btnSaveSubcat.setOnClickListener(v -> saveSubcategory());
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

    private void saveSubcategory() {
        String name = binding.etSubcatName.getText() != null ? binding.etSubcatName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.tilSubcatName.setError(getString(R.string.error_required_fields));
            return;
        }
        binding.tilSubcatName.setError(null);

        if (isEditMode) {
            Subcategory subcategory = new Subcategory(categoryId, name);
            subcategory.setId(subcategoryId);

            categoryViewModel.updateSubcategory(subcategory, (success, errorMessage) -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(requireContext(), getString(R.string.subcategory_updated, name), Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            binding.tilSubcatName.setError(errorMessage != null ? errorMessage : "Failed to update subcategory");
                        }
                    });
                }
            });
        } else {
            Subcategory subcategory = new Subcategory(categoryId, name);
            categoryViewModel.insertSubcategory(subcategory, (success, errorMessage) -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(requireContext(), getString(R.string.subcategory_added, name), Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            binding.tilSubcatName.setError(errorMessage != null ? errorMessage : "Failed to add subcategory");
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
