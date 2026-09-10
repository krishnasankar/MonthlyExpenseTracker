package com.expensetracker.monthly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.expensetracker.monthly.data.entity.Category;
import com.expensetracker.monthly.data.entity.Subcategory;

import org.junit.Test;

public class CategorySubcategoryTest {

    @Test
    public void testCategoryCreationAndModification() {
        Category category = new Category("Food", "#FF7043", "food");
        category.setId(10L);
        category.setBudgetAmount(5000.0);

        assertEquals(10L, category.getId());
        assertEquals("Food", category.getName());
        assertEquals("#FF7043", category.getColorHex());
        assertEquals("food", category.getIconName());
        assertEquals(5000.0, category.getBudgetAmount(), 0.001);

        // Edit Category properties
        category.setName("Food & Dining");
        category.setColorHex("#42A5F5");
        category.setBudgetAmount(7500.0);

        assertEquals("Food & Dining", category.getName());
        assertEquals("#42A5F5", category.getColorHex());
        assertEquals(7500.0, category.getBudgetAmount(), 0.001);
    }

    @Test
    public void testCategoryEquality() {
        Category cat1 = new Category("Entertainment", "#FFA726", "entertainment");
        cat1.setId(1L);

        Category cat2 = new Category("Entertainment", "#FFA726", "entertainment");
        cat2.setId(1L);

        assertEquals(cat1, cat2);
        assertEquals(cat1.hashCode(), cat2.hashCode());

        // Modifying name changes equality
        cat2.setName("Fun & Entertainment");
        assertNotEquals(cat1, cat2);
    }

    @Test
    public void testSubcategoryCreationAndModification() {
        Subcategory subcategory = new Subcategory(1L, "Groceries");
        subcategory.setId(25L);

        assertEquals(25L, subcategory.getId());
        assertEquals(1L, subcategory.getCategoryId());
        assertEquals("Groceries", subcategory.getName());

        // Edit Subcategory properties
        subcategory.setName("Supermarket & Groceries");
        assertEquals("Supermarket & Groceries", subcategory.getName());
        assertEquals("Supermarket & Groceries", subcategory.toString());
    }

    @Test
    public void testSubcategoryEquality() {
        Subcategory sub1 = new Subcategory(2L, "Fuel");
        sub1.setId(5L);

        Subcategory sub2 = new Subcategory(2L, "Fuel");
        sub2.setId(5L);

        assertEquals(sub1, sub2);
        assertEquals(sub1.hashCode(), sub2.hashCode());

        // Edit name changes equality
        sub2.setName("Gas / Fuel");
        assertNotEquals(sub1, sub2);
    }

    @Test
    public void testCaseInsensitiveDuplicateMatchLogic() {
        String existingName = "Groceries";
        String newNameUpper = "GROCERIES";
        String newNamePadded = "  groceries  ";

        assertTrue(existingName.equalsIgnoreCase(newNameUpper));
        assertTrue(existingName.equalsIgnoreCase(newNamePadded.trim()));
    }

    @Test
    public void testCategoryDropdownItemModel() {
        Category cat = new Category("Travel", "#8D6E63", "custom");
        cat.setId(42L);

        com.expensetracker.monthly.ui.adapter.CategoryDropdownAdapter.Item catItem =
                new com.expensetracker.monthly.ui.adapter.CategoryDropdownAdapter.Item(cat);
        assertEquals("Travel", catItem.title);
        assertEquals("Travel", catItem.toString());
        assertNotNull(catItem.category);
        assertEquals(42L, catItem.category.getId());
        assertEquals(false, catItem.isAddAction);

        com.expensetracker.monthly.ui.adapter.CategoryDropdownAdapter.Item actionItem =
                new com.expensetracker.monthly.ui.adapter.CategoryDropdownAdapter.Item("+ Add New Category…", true);
        assertEquals("+ Add New Category…", actionItem.title);
        assertEquals(true, actionItem.isAddAction);
        org.junit.Assert.assertNull(actionItem.category);
    }

    @Test
    public void testSubcategoryDropdownItemModel() {
        Subcategory sub = new Subcategory(10L, "Flight Tickets");
        sub.setId(99L);

        com.expensetracker.monthly.ui.adapter.SubcategoryDropdownAdapter.Item subItem =
                new com.expensetracker.monthly.ui.adapter.SubcategoryDropdownAdapter.Item(sub);
        assertEquals("Flight Tickets", subItem.title);
        assertEquals(false, subItem.isNone);
        assertEquals(false, subItem.isAddAction);
        assertNotNull(subItem.subcategory);

        com.expensetracker.monthly.ui.adapter.SubcategoryDropdownAdapter.Item noneItem =
                new com.expensetracker.monthly.ui.adapter.SubcategoryDropdownAdapter.Item("None", true, false);
        assertEquals("None", noneItem.title);
        assertEquals(true, noneItem.isNone);
        assertEquals(false, noneItem.isAddAction);

        com.expensetracker.monthly.ui.adapter.SubcategoryDropdownAdapter.Item actionItem =
                new com.expensetracker.monthly.ui.adapter.SubcategoryDropdownAdapter.Item("+ Add New Subcategory…", false, true);
        assertEquals("+ Add New Subcategory…", actionItem.title);
        assertEquals(false, actionItem.isNone);
        assertEquals(true, actionItem.isAddAction);
    }
}
