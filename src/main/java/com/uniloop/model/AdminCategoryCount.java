package com.uniloop.model;

public class AdminCategoryCount {

    private final int categoryId;
    private final String categoryName;
    private final int listingCount;

    public AdminCategoryCount(int categoryId, String categoryName, int listingCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.listingCount = listingCount;
    }

    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public int getListingCount() { return listingCount; }
}
