package com.uniloop.dto;

public class ItemResponse {

    private final int itemId;
    private final int sellerId;
    private final int categoryId;
    private final String itemName;
    private final String description;
    private final double price;
    private final String imagePath;
    private final String condition;
    private final String listingType;
    private final String itemStatus;
    private final String datePosted;
    private final String categoryName;
    private final String sellerName;

    public ItemResponse(int itemId, int sellerId, int categoryId, String itemName,
                        String description, double price, String imagePath,
                        String condition, String listingType, String itemStatus,
                        String datePosted, String categoryName, String sellerName) {
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.itemName = itemName;
        this.description = description;
        this.price = price;
        this.imagePath = imagePath;
        this.condition = condition;
        this.listingType = listingType;
        this.itemStatus = itemStatus;
        this.datePosted = datePosted;
        this.categoryName = categoryName;
        this.sellerName = sellerName;
    }

    public int getItemId() {
        return itemId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getCondition() {
        return condition;
    }

    public String getListingType() {
        return listingType;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getSellerName() {
        return sellerName;
    }
}
