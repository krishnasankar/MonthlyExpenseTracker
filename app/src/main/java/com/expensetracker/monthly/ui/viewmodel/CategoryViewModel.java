package com.expensetracker.monthly.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Subcategory;
import com.expensetracker.monthly.data.model.CategoryWithSubcategories;
import com.expensetracker.monthly.data.repository.ExpenseRepository;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final LiveData<List<Category>> allCategories;
    private final LiveData<List<CategoryWithSubcategories>> categoriesWithSubcategories;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allCategories = repository.getAllCategories();
        categoriesWithSubcategories = repository.getCategoriesWithSubcategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<CategoryWithSubcategories>> getCategoriesWithSubcategories() {
        return categoriesWithSubcategories;
    }

    public LiveData<List<Subcategory>> getSubcategoriesForCategory(long categoryId) {
        return repository.getSubcategoriesForCategory(categoryId);
    }

    public void insertCategory(Category category, ExpenseRepository.OnCategoryInsertedListener listener) {
        repository.insertCategory(category, listener);
    }

    public void deleteCategory(long categoryId, Runnable onComplete) {
        repository.deleteCategory(categoryId, onComplete);
    }

    public void insertSubcategory(Subcategory subcategory, Runnable onComplete) {
        repository.insertSubcategory(subcategory, onComplete);
    }

    public void deleteSubcategory(long subcategoryId, Runnable onComplete) {
        repository.deleteSubcategory(subcategoryId, onComplete);
    }
}
