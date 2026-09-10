package com.expensetracker.monthly;

import static org.junit.Assert.assertEquals;

import com.expensetracker.monthly.ui.adapter.ExpenseAdapter;

import org.junit.Test;

public class ExpenseAdapterTest {

    @Test
    public void testFormatCategoryTextWithCategoryAndSubcategory() {
        CharSequence result = ExpenseAdapter.formatCategoryText(null, "Housing & Utilities", "Rent/Mortgage");
        assertEquals("Housing & Utilities › Rent/Mortgage", result.toString());
    }

    @Test
    public void testFormatCategoryTextWithSpacedSubcategory() {
        CharSequence result = ExpenseAdapter.formatCategoryText(null, "Housing & Utilities", "Rent / Mortgage");
        assertEquals("Housing & Utilities › Rent / Mortgage", result.toString());
    }

    @Test
    public void testFormatCategoryTextWithOnlyCategory() {
        CharSequence result = ExpenseAdapter.formatCategoryText(null, "Housing & Utilities", null);
        assertEquals("Housing & Utilities", result.toString());

        CharSequence emptySubResult = ExpenseAdapter.formatCategoryText(null, "Housing & Utilities", "   ");
        assertEquals("Housing & Utilities", emptySubResult.toString());
    }

    @Test
    public void testFormatCategoryTextWithNullCategory() {
        CharSequence result = ExpenseAdapter.formatCategoryText(null, null, "Rent/Mortgage");
        assertEquals("Uncategorized › Rent/Mortgage", result.toString());
    }

    @Test
    public void testFormatCategoryTextWithBothNull() {
        CharSequence result = ExpenseAdapter.formatCategoryText(null, null, null);
        assertEquals("Uncategorized", result.toString());
    }
}
