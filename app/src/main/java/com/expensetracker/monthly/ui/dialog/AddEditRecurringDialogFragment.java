package com.expensetracker.monthly.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.RecurringExpense;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.data.model.RecurringExpenseWithDetails;
import com.expensetracker.monthly.databinding.DialogAddEditRecurringBinding;
import com.expensetracker.monthly.databinding.DialogQuickAddCategoryBinding;
import com.expensetracker.monthly.databinding.DialogQuickAddSubcategoryBinding;
import com.expensetracker.monthly.ui.adapter.CategoryDropdownAdapter;
import com.expensetracker.monthly.ui.adapter.SubcategoryDropdownAdapter;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;
import com.expensetracker.monthly.ui.viewmodel.RecurringExpenseViewModel;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.RecurringExpenseManager;

import java.util.ArrayList;
import java.util.List;

public class AddEditRecurringDialogFragment extends DialogFragment {

    public static final String TAG = "AddEditRecurringDialog";

    private static final String ARG_ID = "arg_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_AMOUNT = "arg_amount";
    private static final String ARG_CAT_ID = "arg_cat_id";
    private static final String ARG_SUBCAT_ID = "arg_subcat_id";
    private static final String ARG_FREQUENCY = "arg_frequency";
    private static final String ARG_DAY = "arg_day";
    private static final String ARG_NOTES = "arg_notes";
    private static final String ARG_ACTIVE = "arg_active";
    private static final String ARG_LAST_LOGGED = "arg_last_logged";

    private static final String[] PALETTE_COLORS = {
            "#FF7043", "#42A5F5", "#AB47BC", "#FFA726", "#26A69A",
            "#EC407A", "#7E57C2", "#5C6BC0", "#29B6F6", "#66BB6A",
            "#8D6E63", "#78909C"
    };

    private DialogAddEditRecurringBinding binding;
    private RecurringExpenseViewModel recurringViewModel;
    private CategoryViewModel categoryViewModel;

    private boolean isEditMode = false;
    private long editId = -1;
    private long initialCategoryId = -1;
    private Long initialSubcategoryId = null;
    private boolean currentActive = true;
    private long lastLoggedMillis = 0;

    private final List<Category> categoriesList = new ArrayList<>();
    private final List<Subcategory> subcategoriesList = new ArrayList<>();
    private LiveData<List<Subcategory>> subcategoriesLiveData = null;

    private Category selectedCategory = null;
    private Subcategory selectedSubcategory = null;

    public static AddEditRecurringDialogFragment newInstance(@Nullable RecurringExpenseWithDetails item) {
        AddEditRecurringDialogFragment fragment = new AddEditRecurringDialogFragment();
        if (item != null && item.recurringExpense != null) {
            Bundle args = new Bundle();
            args.putLong(ARG_ID, item.recurringExpense.getId());
            args.putString(ARG_TITLE, item.recurringExpense.getTitle());
            args.putDouble(ARG_AMOUNT, item.recurringExpense.getAmount());
            args.putLong(ARG_CAT_ID, item.recurringExpense.getCategoryId());
            if (item.recurringExpense.getSubcategoryId() != null) {
                args.putLong(ARG_SUBCAT_ID, item.recurringExpense.getSubcategoryId());
            }
            args.putString(ARG_FREQUENCY, item.recurringExpense.getFrequency());
            args.putInt(ARG_DAY, item.recurringExpense.getDayOfMonth());
            args.putString(ARG_NOTES, item.recurringExpense.getNotes());
            args.putBoolean(ARG_ACTIVE, item.recurringExpense.isActive());
            args.putLong(ARG_LAST_LOGGED, item.recurringExpense.getLastLoggedMillis());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        if (getArguments() != null && getArguments().containsKey(ARG_ID)) {
            isEditMode = true;
            editId = getArguments().getLong(ARG_ID);
            initialCategoryId = getArguments().getLong(ARG_CAT_ID);
            if (getArguments().containsKey(ARG_SUBCAT_ID)) {
                initialSubcategoryId = getArguments().getLong(ARG_SUBCAT_ID);
            }
            currentActive = getArguments().getBoolean(ARG_ACTIVE, true);
            lastLoggedMillis = getArguments().getLong(ARG_LAST_LOGGED, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddEditRecurringBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recurringViewModel = new ViewModelProvider(requireActivity()).get(RecurringExpenseViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        String currencySymbol = CurrencyUtils.getCurrencySymbol(requireContext());
        binding.tilAmount.setPrefixText(currencySymbol + " ");

        setupFrequencyDropdown();

        if (isEditMode) {
            binding.tvDialogTitle.setText(R.string.edit_recurring_bill);
            binding.btnSave.setText(R.string.save);
            binding.etTitle.setText(getArguments().getString(ARG_TITLE, ""));
            double amount = getArguments().getDouble(ARG_AMOUNT, 0.0);
            binding.etAmount.setText(amount == Math.floor(amount) ? String.valueOf((long) amount) : String.valueOf(amount));
            String rawFreq = getArguments().getString(ARG_FREQUENCY, RecurringExpense.FREQUENCY_MONTHLY);
            binding.actvFrequency.setText(RecurringExpense.getFrequencyDisplayName(rawFreq), false);
            binding.etDueDay.setText(String.valueOf(getArguments().getInt(ARG_DAY, 1)));
            binding.etNotes.setText(getArguments().getString(ARG_NOTES, ""));

            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.btnDelete.setOnClickListener(v -> confirmDelete());
        } else {
            binding.tvDialogTitle.setText(R.string.add_recurring_bill);
            binding.btnSave.setText(R.string.save_bill_btn);
            binding.actvFrequency.setText(RecurringExpense.getFrequencyDisplayName(RecurringExpense.FREQUENCY_MONTHLY), false);
            binding.etDueDay.setText("1");
            binding.btnDelete.setVisibility(View.GONE);
        }

        binding.btnClose.setOnClickListener(v -> handleBackPress());
        binding.btnSave.setOnClickListener(v -> saveRecurring());

        binding.btnAddCategory.setOnClickListener(v -> showQuickAddCategoryDialog());
        binding.btnAddSubcategory.setOnClickListener(v -> {
            if (selectedCategory == null) {
                binding.tilCategory.setError(getString(R.string.select_category_first));
            } else {
                showQuickAddSubcategoryDialog();
            }
        });

        setupCategoryObservers();
        setupInputActions();
        setupFocusAutoScroll();
        setupWindowInsetsHandling();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.94);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isEditMode) {
            focusTitleFieldAndShowKeyboard();
        }
    }

    private void setupWindowInsetsHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int paddingBottom = Math.max(0, imeHeight - systemBars);
            v.setPadding(0, 0, 0, paddingBottom);
            return insets;
        });
    }

    private void setupFocusAutoScroll() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus && binding != null) {
                binding.scrollView.postDelayed(() -> {
                    if (binding != null) {
                        int top = v.getTop();
                        View parent = (View) v.getParent();
                        while (parent != null && parent != binding.scrollView) {
                            top += parent.getTop();
                            if (parent.getParent() instanceof View) {
                                parent = (View) parent.getParent();
                            } else {
                                break;
                            }
                        }
                        binding.scrollView.smoothScrollTo(0, Math.max(0, top - 60));
                    }
                }, 150);
            }
        };

        binding.etTitle.setOnFocusChangeListener(focusListener);
        binding.etAmount.setOnFocusChangeListener(focusListener);
        binding.etDueDay.setOnFocusChangeListener(focusListener);
        binding.etNotes.setOnFocusChangeListener(focusListener);
    }

    private void handleBackPress() {
        hideKeyboard();
        if (binding != null) {
            binding.getRoot().clearFocus();
        }
        dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        hideKeyboard();
        super.onDismiss(dialog);
    }

    private void hideKeyboard() {
        if (binding == null) return;
        View focus = null;
        Dialog dialog = getDialog();
        if (dialog != null) {
            focus = dialog.getCurrentFocus();
        }
        if (focus == null && getView() != null) {
            focus = getView().findFocus();
        }
        if (focus == null && binding != null) {
            focus = binding.getRoot();
        }

        if (focus != null && isAdded()) {
            if (dialog != null && dialog.getWindow() != null) {
                WindowInsetsControllerCompat insetsController =
                        WindowCompat.getInsetsController(dialog.getWindow(), focus);
                if (insetsController != null) {
                    insetsController.hide(WindowInsetsCompat.Type.ime());
                }
            }
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        }
    }

    private void setupInputActions() {
        binding.etTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.etTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterKeyDown(event)) {
                binding.etAmount.requestFocus();
                showKeyboardForView(binding.etAmount);
                return true;
            }
            return false;
        });

        binding.etAmount.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.etAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterKeyDown(event)) {
                binding.etDueDay.requestFocus();
                showKeyboardForView(binding.etDueDay);
                return true;
            }
            return false;
        });

        binding.etDueDay.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.etDueDay.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || isEnterKeyDown(event)) {
                binding.etNotes.requestFocus();
                showKeyboardForView(binding.etNotes);
                return true;
            }
            return false;
        });

        binding.etNotes.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.etNotes.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                saveRecurring();
                return true;
            }
            return false;
        });
    }

    private boolean isEnterKeyDown(KeyEvent event) {
        return event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN;
    }

    private void focusTitleFieldAndShowKeyboard() {
        if (binding == null) return;
        binding.etTitle.requestFocus();
        if (binding.etTitle.getText() != null) {
            binding.etTitle.setSelection(binding.etTitle.getText().length());
        }
        binding.etTitle.postDelayed(() -> {
            if (isAdded() && binding != null) {
                showKeyboardForView(binding.etTitle);
            }
        }, 100);
    }

    private void showKeyboardForView(View view) {
        if (view == null || !isAdded()) return;
        view.requestFocus();
        view.post(() -> {
            if (!isAdded() || view == null) return;
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                WindowInsetsControllerCompat insetsController =
                        WindowCompat.getInsetsController(dialog.getWindow(), view);
                if (insetsController != null) {
                    insetsController.show(WindowInsetsCompat.Type.ime());
                }
            }
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                imm.showSoftInput(view, 0);
            }
        });
    }

    private void setupFrequencyDropdown() {
        String[] frequencies = new String[]{
                RecurringExpense.getFrequencyDisplayName(RecurringExpense.FREQUENCY_MONTHLY),
                RecurringExpense.getFrequencyDisplayName(RecurringExpense.FREQUENCY_WEEKLY),
                RecurringExpense.getFrequencyDisplayName(RecurringExpense.FREQUENCY_YEARLY)
        };
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_dropdown_menu, frequencies);
        binding.actvFrequency.setAdapter(freqAdapter);
        binding.actvFrequency.setDropDownBackgroundResource(R.drawable.bg_popup_menu);
    }

    private void setupCategoryObservers() {
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            if (categories != null && !categories.isEmpty()) {
                categoriesList.addAll(categories);

                CategoryDropdownAdapter adapter = new CategoryDropdownAdapter(requireContext(), categoriesList);
                binding.actvCategory.setAdapter(adapter);
                binding.actvCategory.setDropDownBackgroundResource(R.drawable.bg_popup_menu);

                if (selectedCategory == null && initialCategoryId > 0) {
                    for (Category c : categoriesList) {
                        if (c.getId() == initialCategoryId) {
                            selectedCategory = c;
                            binding.actvCategory.setText(c.getName(), false);
                            loadSubcategories(c.getId());
                            break;
                        }
                    }
                } else if (selectedCategory == null && !categoriesList.isEmpty()) {
                    selectedCategory = categoriesList.get(0);
                    binding.actvCategory.setText(selectedCategory.getName(), false);
                    loadSubcategories(selectedCategory.getId());
                } else if (selectedCategory != null) {
                    for (Category c : categoriesList) {
                        if (c.getId() == selectedCategory.getId()) {
                            selectedCategory = c;
                            binding.actvCategory.setText(c.getName(), false);
                            break;
                        }
                    }
                }

                binding.actvCategory.setOnItemClickListener((parent, v, position, id) -> {
                    CategoryDropdownAdapter.Item item = (CategoryDropdownAdapter.Item) parent.getItemAtPosition(position);
                    if (item != null) {
                        if (item.isAddAction) {
                            if (selectedCategory != null) {
                                binding.actvCategory.setText(selectedCategory.getName(), false);
                            } else {
                                binding.actvCategory.setText("", false);
                            }
                            showQuickAddCategoryDialog();
                        } else if (item.category != null) {
                            selectedCategory = item.category;
                            selectedSubcategory = null;
                            binding.actvCategory.setText(selectedCategory.getName(), false);
                            binding.tilCategory.setError(null);
                            binding.actvSubcategory.setText("", false);
                            loadSubcategories(selectedCategory.getId());
                        }
                    }
                });
            }
        });
    }

    private void loadSubcategories(long categoryId) {
        if (subcategoriesLiveData != null) {
            subcategoriesLiveData.removeObservers(getViewLifecycleOwner());
        }
        subcategoriesLiveData = categoryViewModel.getSubcategoriesForCategory(categoryId);
        subcategoriesLiveData.observe(getViewLifecycleOwner(), subcategories -> {
            subcategoriesList.clear();
            if (subcategories != null) {
                subcategoriesList.addAll(subcategories);
            }

            SubcategoryDropdownAdapter subAdapter = new SubcategoryDropdownAdapter(requireContext(), subcategoriesList);
            binding.actvSubcategory.setAdapter(subAdapter);
            binding.actvSubcategory.setDropDownBackgroundResource(R.drawable.bg_popup_menu);

            if (initialSubcategoryId != null && initialSubcategoryId > 0 && selectedSubcategory == null) {
                for (Subcategory s : subcategoriesList) {
                    if (s.getId() == initialSubcategoryId) {
                        selectedSubcategory = s;
                        binding.actvSubcategory.setText(s.getName(), false);
                        break;
                    }
                }
                initialSubcategoryId = null;
            } else if (selectedSubcategory != null) {
                boolean exists = false;
                for (Subcategory s : subcategoriesList) {
                    if (s.getId() == selectedSubcategory.getId()) {
                        selectedSubcategory = s;
                        binding.actvSubcategory.setText(s.getName(), false);
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    selectedSubcategory = null;
                    binding.actvSubcategory.setText(getString(R.string.expense_subcategory_none), false);
                }
            } else {
                binding.actvSubcategory.setText(getString(R.string.expense_subcategory_none), false);
            }

            binding.actvSubcategory.setOnItemClickListener((parent, v, position, id) -> {
                SubcategoryDropdownAdapter.Item item = (SubcategoryDropdownAdapter.Item) parent.getItemAtPosition(position);
                if (item != null) {
                    if (item.isAddAction) {
                        if (selectedSubcategory != null) {
                            binding.actvSubcategory.setText(selectedSubcategory.getName(), false);
                        } else {
                            binding.actvSubcategory.setText(getString(R.string.expense_subcategory_none), false);
                        }
                        showQuickAddSubcategoryDialog();
                    } else if (item.isNone) {
                        selectedSubcategory = null;
                        binding.actvSubcategory.setText(getString(R.string.expense_subcategory_none), false);
                    } else if (item.subcategory != null) {
                        selectedSubcategory = item.subcategory;
                        binding.actvSubcategory.setText(selectedSubcategory.getName(), false);
                    }
                }
            });
        });
    }

    private void showQuickAddCategoryDialog() {
        Dialog quickDialog = new Dialog(requireContext());
        DialogQuickAddCategoryBinding quickBinding = DialogQuickAddCategoryBinding.inflate(getLayoutInflater());
        quickDialog.setContentView(quickBinding.getRoot());

        if (quickDialog.getWindow() != null) {
            quickDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            quickDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            quickDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        int padding = (int) (4 * getResources().getDisplayMetrics().density);
        int size = (int) (38 * getResources().getDisplayMetrics().density);
        final String[] selectedColorHex = {PALETTE_COLORS[0]};

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
                    selectedColorHex[0] = colorHex;
                }
            });

            quickBinding.rgColors.addView(rb);
        }

        quickBinding.btnCancel.setOnClickListener(v -> quickDialog.dismiss());

        Runnable createAction = () -> {
            String name = quickBinding.etName.getText() != null ? quickBinding.etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                quickBinding.tilName.setError(getString(R.string.error_field_required));
                return;
            }

            for (Category existing : categoriesList) {
                if (existing.getName().equalsIgnoreCase(name)) {
                    selectedCategory = existing;
                    binding.actvCategory.setText(existing.getName(), false);
                    binding.tilCategory.setError(null);
                    loadSubcategories(existing.getId());
                    quickDialog.dismiss();
                    Toast.makeText(requireContext(), getString(R.string.category_updated, existing.getName()), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Category newCategory = new Category(name, selectedColorHex[0], "custom");
            categoryViewModel.createCategory(newCategory, (createdCategory, success, error) -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success && createdCategory != null) {
                            selectedCategory = createdCategory;
                            binding.actvCategory.setText(createdCategory.getName(), false);
                            binding.tilCategory.setError(null);
                            loadSubcategories(createdCategory.getId());
                            quickDialog.dismiss();
                            Toast.makeText(requireContext(), getString(R.string.category_added, createdCategory.getName()), Toast.LENGTH_SHORT).show();
                        } else {
                            quickBinding.tilName.setError(error != null ? error : "Failed to create category");
                        }
                    });
                }
            });
        };

        quickBinding.btnCreate.setOnClickListener(v -> createAction.run());
        quickBinding.etName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createAction.run();
                return true;
            }
            return false;
        });

        quickDialog.show();
        quickBinding.etName.requestFocus();
        quickBinding.etName.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(quickBinding.etName, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 150);
    }

    private void showQuickAddSubcategoryDialog() {
        if (selectedCategory == null) {
            binding.tilCategory.setError(getString(R.string.select_category_first));
            return;
        }

        Dialog quickDialog = new Dialog(requireContext());
        DialogQuickAddSubcategoryBinding quickBinding = DialogQuickAddSubcategoryBinding.inflate(getLayoutInflater());
        quickDialog.setContentView(quickBinding.getRoot());

        if (quickDialog.getWindow() != null) {
            quickDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            quickDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            quickDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        quickBinding.tvParentCategory.setText("Under: " + selectedCategory.getName());
        quickBinding.btnCancel.setOnClickListener(v -> quickDialog.dismiss());

        Runnable createAction = () -> {
            String name = quickBinding.etName.getText() != null ? quickBinding.etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                quickBinding.tilName.setError(getString(R.string.error_field_required));
                return;
            }

            for (Subcategory existing : subcategoriesList) {
                if (existing.getName().equalsIgnoreCase(name)) {
                    selectedSubcategory = existing;
                    binding.actvSubcategory.setText(existing.getName(), false);
                    binding.tilSubcategory.setError(null);
                    quickDialog.dismiss();
                    Toast.makeText(requireContext(), getString(R.string.subcategory_updated, existing.getName()), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Subcategory newSubcategory = new Subcategory(selectedCategory.getId(), name);
            categoryViewModel.createSubcategory(newSubcategory, (createdSubcategory, success, error) -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (success && createdSubcategory != null) {
                            selectedSubcategory = createdSubcategory;
                            binding.actvSubcategory.setText(createdSubcategory.getName(), false);
                            binding.tilSubcategory.setError(null);
                            quickDialog.dismiss();
                            Toast.makeText(requireContext(), getString(R.string.subcategory_added, createdSubcategory.getName()), Toast.LENGTH_SHORT).show();
                        } else {
                            quickBinding.tilName.setError(error != null ? error : "Failed to create subcategory");
                        }
                    });
                }
            });
        };

        quickBinding.btnCreate.setOnClickListener(v -> createAction.run());
        quickBinding.etName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createAction.run();
                return true;
            }
            return false;
        });

        quickDialog.show();
        quickBinding.etName.requestFocus();
        quickBinding.etName.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(quickBinding.etName, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 150);
    }

    private void saveRecurring() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            binding.tilTitle.setError(getString(R.string.error_field_required));
            binding.scrollView.smoothScrollTo(0, binding.tilTitle.getTop());
            return;
        }
        binding.tilTitle.setError(null);

        String amountStr = binding.etAmount.getText() != null ? binding.etAmount.getText().toString().trim() : "";
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                binding.tilAmount.setError(getString(R.string.error_invalid_amount));
                binding.scrollView.smoothScrollTo(0, binding.tilAmount.getTop());
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilAmount.setError(getString(R.string.error_invalid_amount));
            binding.scrollView.smoothScrollTo(0, binding.tilAmount.getTop());
            return;
        }
        binding.tilAmount.setError(null);

        if (selectedCategory == null) {
            binding.tilCategory.setError(getString(R.string.error_select_category));
            binding.scrollView.smoothScrollTo(0, binding.tilCategory.getTop());
            return;
        }
        binding.tilCategory.setError(null);

        String freqDisplay = binding.actvFrequency.getText().toString().trim();
        String freq = RecurringExpense.parseFrequencyFromDisplay(freqDisplay);

        String dayStr = binding.etDueDay.getText() != null ? binding.etDueDay.getText().toString().trim() : "";
        int dueDay = 1;
        try {
            dueDay = Integer.parseInt(dayStr);
            if (dueDay < 1 || dueDay > 31) {
                binding.tilDueDay.setError("Day must be between 1 and 31");
                binding.scrollView.smoothScrollTo(0, binding.tilDueDay.getTop());
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilDueDay.setError("Enter a valid day (1–31)");
            binding.scrollView.smoothScrollTo(0, binding.tilDueDay.getTop());
            return;
        }
        binding.tilDueDay.setError(null);

        String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";
        Long subcategoryId = selectedSubcategory != null ? selectedSubcategory.getId() : null;

        if (isEditMode) {
            RecurringExpense recurring = new RecurringExpense(
                    title, amount, selectedCategory.getId(), subcategoryId,
                    freq, dueDay, lastLoggedMillis, currentActive, notes);
            recurring.setId(editId);

            recurringViewModel.updateRecurringExpense(recurring, () -> {
                if (isAdded()) {
                    RecurringExpenseManager.checkAndProcessRecurringExpenses(requireContext().getApplicationContext());
                }
            });
        } else {
            RecurringExpense recurring = new RecurringExpense(
                    title, amount, selectedCategory.getId(), subcategoryId,
                    freq, dueDay, 0L, true, notes);

            recurringViewModel.insertRecurringExpense(recurring, () -> {
                if (isAdded()) {
                    RecurringExpenseManager.checkAndProcessRecurringExpenses(requireContext().getApplicationContext());
                }
            });
        }

        Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void confirmDelete() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_recurring_bill)
                .setMessage(getString(R.string.delete_recurring_confirm, binding.etTitle.getText().toString()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    RecurringExpense item = new RecurringExpense(
                            "", 0.0, 0, null, "MONTHLY", 1, 0L, false, null);
                    item.setId(editId);
                    recurringViewModel.deleteRecurringExpense(item, null);
                    Toast.makeText(requireContext(), R.string.deleted_successfully, Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
