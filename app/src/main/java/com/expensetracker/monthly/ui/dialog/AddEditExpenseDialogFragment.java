package com.expensetracker.monthly.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.expensetracker.monthly.R;
import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Expense;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.data.model.ExpenseAutofillSuggestion;
import com.expensetracker.monthly.data.model.ExpenseWithDetails;
import com.expensetracker.monthly.databinding.DialogAddExpenseBinding;
import com.expensetracker.monthly.ui.viewmodel.CategoryViewModel;
import com.expensetracker.monthly.ui.viewmodel.ExpenseViewModel;
import com.expensetracker.monthly.util.CurrencyUtils;
import com.expensetracker.monthly.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddEditExpenseDialogFragment extends DialogFragment {

    public static final String TAG = "AddEditExpenseDialog";
    private static final String ARG_EXPENSE_ID = "arg_expense_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_AMOUNT = "arg_amount";
    private static final String ARG_DATE = "arg_date";
    private static final String ARG_CATEGORY_ID = "arg_cat_id";
    private static final String ARG_SUBCATEGORY_ID = "arg_subcat_id";
    private static final String ARG_NOTES = "arg_notes";

    private DialogAddExpenseBinding binding;
    private ExpenseViewModel expenseViewModel;
    private CategoryViewModel categoryViewModel;

    private final Calendar selectedDate = Calendar.getInstance();
    private final List<Category> categoriesList = new ArrayList<>();
    private final List<Subcategory> subcategoriesList = new ArrayList<>();
    private androidx.lifecycle.LiveData<List<Subcategory>> subcategoriesLiveData = null;

    private Category selectedCategory = null;
    private Subcategory selectedSubcategory = null;

    private boolean isEditMode = false;
    private long editExpenseId = -1;
    private long initialCategoryId = -1;
    private Long initialSubcategoryId = null;
    private Long pendingAutofillSubcategoryId = null;
    private final List<ExpenseAutofillSuggestion> autofillSuggestionsList = new ArrayList<>();

    public static AddEditExpenseDialogFragment newInstance(@Nullable ExpenseWithDetails expense) {
        AddEditExpenseDialogFragment fragment = new AddEditExpenseDialogFragment();
        if (expense != null && expense.expense != null) {
            Bundle args = new Bundle();
            args.putLong(ARG_EXPENSE_ID, expense.expense.getId());
            args.putString(ARG_TITLE, expense.expense.getTitle());
            args.putDouble(ARG_AMOUNT, expense.expense.getAmount());
            args.putLong(ARG_DATE, expense.expense.getDateMillis());
            args.putLong(ARG_CATEGORY_ID, expense.expense.getCategoryId());
            if (expense.expense.getSubcategoryId() != null) {
                args.putLong(ARG_SUBCATEGORY_ID, expense.expense.getSubcategoryId());
            }
            args.putString(ARG_NOTES, expense.expense.getNotes());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        if (getArguments() != null && getArguments().containsKey(ARG_EXPENSE_ID)) {
            isEditMode = true;
            editExpenseId = getArguments().getLong(ARG_EXPENSE_ID);
            initialCategoryId = getArguments().getLong(ARG_CATEGORY_ID);
            if (getArguments().containsKey(ARG_SUBCATEGORY_ID)) {
                initialSubcategoryId = getArguments().getLong(ARG_SUBCATEGORY_ID);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        String currencySymbol = CurrencyUtils.getCurrencySymbol(requireContext());
        binding.tilAmount.setPrefixText(currencySymbol + " ");

        if (isEditMode) {
            binding.tvDialogTitle.setText(R.string.edit_expense);
            binding.etTitle.setText(getArguments().getString(ARG_TITLE, ""));
            double amount = getArguments().getDouble(ARG_AMOUNT, 0.0);
            binding.etAmount.setText(String.valueOf(amount));
            long dateMillis = getArguments().getLong(ARG_DATE, System.currentTimeMillis());
            selectedDate.setTimeInMillis(dateMillis);
            binding.etNotes.setText(getArguments().getString(ARG_NOTES, ""));
        } else {
            binding.tvDialogTitle.setText(R.string.add_expense);
            selectedDate.setTimeInMillis(System.currentTimeMillis());
        }

        updateDateField();

        binding.etDate.setOnClickListener(v -> showDatePicker(false));
        binding.tilDate.setOnClickListener(v -> showDatePicker(false));

        binding.btnCancel.setOnClickListener(v -> handleBackPress());
        binding.btnSave.setOnClickListener(v -> saveExpense());

        observeCategories();
        setupTitleAutofill();
        setupInputActions();
        setupBackPressHandling();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isEditMode) {
            focusTitleFieldAndShowKeyboard();
        }
    }

    private void setupBackPressHandling() {
        binding.etTitle.setOnBackPressedListener(() -> {
            handleBackPress();
            return true;
        });

        binding.etAmount.setOnBackPressedListener(() -> {
            handleBackPress();
            return true;
        });

        binding.etNotes.setOnBackPressedListener(() -> {
            handleBackPress();
            return true;
        });

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    handleBackPress();
                    return true;
                }
                return false;
            });
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });
    }

    private void handleBackPress() {
        hideKeyboard();
        dismiss();
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
        // Title input actions
        binding.etTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.etTitle.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            boolean isProceedAction = actionId == EditorInfo.IME_ACTION_NEXT
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_GO;

            if (isProceedAction || isEnterKey) {
                moveToAmountField();
                return true;
            }
            return false;
        });

        binding.etTitle.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                moveToAmountField();
                return true;
            }
            return false;
        });

        // Amount input actions
        binding.etAmount.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.etAmount.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            boolean isProceedAction = actionId == EditorInfo.IME_ACTION_NEXT
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_GO;

            if (isProceedAction || isEnterKey) {
                proceedFromAmount();
                return true;
            }
            return false;
        });

        binding.etAmount.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                proceedFromAmount();
                return true;
            }
            return false;
        });

        // Notes input actions
        binding.etNotes.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.etNotes.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                saveExpense();
                return true;
            }
            return false;
        });
    }

    private void moveToAmountField() {
        if (binding == null) return;
        binding.etTitle.dismissDropDown();
        binding.etAmount.requestFocus();
        if (binding.etAmount.getText() != null) {
            binding.etAmount.setSelection(binding.etAmount.getText().length());
        }
        showKeyboardForView(binding.etAmount);
    }

    private void proceedFromAmount() {
        if (binding == null) return;
        hideKeyboard();
        binding.etAmount.clearFocus();
        showDatePicker(true);
    }

    private void proceedToCategory() {
        if (binding == null || !isAdded()) return;
        binding.actCategory.requestFocus();
        binding.actCategory.postDelayed(() -> {
            if (binding != null && isAdded()) {
                binding.actCategory.showDropDown();
            }
        }, 200);
    }

    private void proceedToSubcategory() {
        if (binding == null || !isAdded()) return;
        binding.actSubcategory.requestFocus();
        binding.actSubcategory.postDelayed(() -> {
            if (binding != null && isAdded()) {
                binding.actSubcategory.showDropDown();
            }
        }, 200);
    }

    private void proceedToNotes() {
        if (binding == null || !isAdded()) return;
        binding.etNotes.requestFocus();
        if (binding.etNotes.getText() != null) {
            binding.etNotes.setSelection(binding.etNotes.getText().length());
        }
        binding.etNotes.postDelayed(() -> {
            if (binding != null && isAdded()) {
                showKeyboardForView(binding.etNotes);
            }
        }, 200);
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
            view.requestFocus();
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

    private void updateDateField() {
        binding.etDate.setText(DateUtils.formatDate(selectedDate.getTimeInMillis()));
    }

    private void showDatePicker(boolean continueFlow) {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                    if (continueFlow) {
                        proceedToCategory();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void observeCategories() {
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            if (categories != null && !categories.isEmpty()) {
                categoriesList.addAll(categories);

                List<String> names = new ArrayList<>();
                for (Category cat : categories) {
                    names.add(cat.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_dropdown_menu,
                        names
                );
                binding.actCategory.setAdapter(adapter);
                binding.actCategory.setDropDownBackgroundResource(R.drawable.bg_popup_menu);

                // If editing, find initial category
                if (selectedCategory == null && initialCategoryId > 0) {
                    for (Category cat : categories) {
                        if (cat.getId() == initialCategoryId) {
                            selectedCategory = cat;
                            binding.actCategory.setText(cat.getName(), false);
                            loadSubcategories(cat.getId(), false);
                            break;
                        }
                    }
                } else if (selectedCategory == null && !categories.isEmpty()) {
                    // Default select first category
                    selectedCategory = categories.get(0);
                    binding.actCategory.setText(selectedCategory.getName(), false);
                    loadSubcategories(selectedCategory.getId(), false);
                }

                binding.actCategory.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCategory = categoriesList.get(position);
                    selectedSubcategory = null;
                    pendingAutofillSubcategoryId = null;
                    binding.actSubcategory.setText("", false);
                    loadSubcategories(selectedCategory.getId(), true);
                });
            }
        });
    }

    private void loadSubcategories(long categoryId, boolean continueFlow) {
        if (subcategoriesLiveData != null) {
            subcategoriesLiveData.removeObservers(getViewLifecycleOwner());
        }
        subcategoriesLiveData = categoryViewModel.getSubcategoriesForCategory(categoryId);
        subcategoriesLiveData.observe(getViewLifecycleOwner(), subcategories -> {
            subcategoriesList.clear();
            List<String> subNames = new ArrayList<>();
            subNames.add(getString(R.string.expense_subcategory_none));

            boolean hasSubcategories = false;
            if (subcategories != null && !subcategories.isEmpty()) {
                hasSubcategories = true;
                subcategoriesList.addAll(subcategories);
                for (Subcategory s : subcategories) {
                    subNames.add(s.getName());
                }
            }

            ArrayAdapter<String> subAdapter = new ArrayAdapter<>(
                    requireContext(),
                    R.layout.item_dropdown_menu,
                    subNames
            );
            binding.actSubcategory.setAdapter(subAdapter);
            binding.actSubcategory.setDropDownBackgroundResource(R.drawable.bg_popup_menu);

            if (initialSubcategoryId != null && initialSubcategoryId > 0 && selectedSubcategory == null) {
                for (Subcategory s : subcategoriesList) {
                    if (s.getId() == initialSubcategoryId) {
                        selectedSubcategory = s;
                        binding.actSubcategory.setText(s.getName(), false);
                        break;
                    }
                }
                initialSubcategoryId = null; // Clear so subsequent category switches don't re-select it
            } else if (pendingAutofillSubcategoryId != null) {
                selectedSubcategory = null;
                if (pendingAutofillSubcategoryId > 0) {
                    for (Subcategory s : subcategoriesList) {
                        if (s.getId() == (long) pendingAutofillSubcategoryId) {
                            selectedSubcategory = s;
                            binding.actSubcategory.setText(s.getName(), false);
                            break;
                        }
                    }
                }
                if (selectedSubcategory == null) {
                    binding.actSubcategory.setText(getString(R.string.expense_subcategory_none), false);
                }
                pendingAutofillSubcategoryId = null; // Clear so subsequent manual category switches don't re-select it
            } else if (selectedSubcategory != null) {
                binding.actSubcategory.setText(selectedSubcategory.getName(), false);
            } else {
                binding.actSubcategory.setText(getString(R.string.expense_subcategory_none), false);
            }

            binding.actSubcategory.setOnItemClickListener((parent, view, position, id) -> {
                if (position == 0) {
                    selectedSubcategory = null;
                } else {
                    selectedSubcategory = subcategoriesList.get(position - 1);
                }
                proceedToNotes();
            });

            if (continueFlow) {
                if (hasSubcategories) {
                    proceedToSubcategory();
                } else {
                    proceedToNotes();
                }
            }
        });
    }

    private void setupTitleAutofill() {
        expenseViewModel.getExpenseAutofillSuggestions().observe(getViewLifecycleOwner(), suggestions -> {
            autofillSuggestionsList.clear();
            if (suggestions != null && !suggestions.isEmpty()) {
                autofillSuggestionsList.addAll(suggestions);
                ArrayAdapter<ExpenseAutofillSuggestion> adapter = new ArrayAdapter<>(
                        requireContext(),
                        R.layout.item_dropdown_menu,
                        new ArrayList<>(autofillSuggestionsList)
                );
                binding.etTitle.setAdapter(adapter);
                binding.etTitle.setDropDownBackgroundResource(R.drawable.bg_popup_menu);
                binding.etTitle.setThreshold(1);
            }
        });

        binding.etTitle.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof ExpenseAutofillSuggestion) {
                applyAutofillSuggestion((ExpenseAutofillSuggestion) item);
            }
            binding.etTitle.post(() -> {
                if (binding != null && binding.etTitle.getText() != null) {
                    binding.etTitle.setSelection(binding.etTitle.getText().length());
                }
            });
        });
    }

    private void applyAutofillSuggestion(ExpenseAutofillSuggestion suggestion) {
        long targetCatId = suggestion.getCategoryId();
        for (Category cat : categoriesList) {
            if (cat.getId() == targetCatId) {
                selectedCategory = cat;
                binding.actCategory.setText(cat.getName(), false);
                binding.tilCategory.setError(null);

                selectedSubcategory = null;
                pendingAutofillSubcategoryId = suggestion.getSubcategoryId();
                loadSubcategories(cat.getId(), false);
                break;
            }
        }
    }

    private void saveExpense() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String amountStr = binding.etAmount.getText() != null ? binding.etAmount.getText().toString().trim() : "";
        String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";

        if (title.isEmpty()) {
            binding.tilTitle.setError(getString(R.string.error_required_fields));
            return;
        } else {
            binding.tilTitle.setError(null);
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                binding.tilAmount.setError(getString(R.string.error_invalid_amount));
                return;
            }
            binding.tilAmount.setError(null);
        } catch (NumberFormatException e) {
            binding.tilAmount.setError(getString(R.string.error_invalid_amount));
            return;
        }

        if (selectedCategory == null) {
            binding.tilCategory.setError(getString(R.string.error_required_fields));
            return;
        } else {
            binding.tilCategory.setError(null);
        }

        Long subcategoryId = selectedSubcategory != null ? selectedSubcategory.getId() : null;

        if (isEditMode) {
            Expense updated = new Expense(title, amount, selectedDate.getTimeInMillis(), selectedCategory.getId(), subcategoryId, notes);
            updated.setId(editExpenseId);
            expenseViewModel.updateExpense(updated, () -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Expense updated", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            });
        } else {
            Expense newExpense = new Expense(title, amount, selectedDate.getTimeInMillis(), selectedCategory.getId(), subcategoryId, notes);
            expenseViewModel.insertExpense(newExpense, () -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Expense added", Toast.LENGTH_SHORT).show();
                        dismiss();
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
