package com.uniloop.model;

import java.math.BigDecimal;

public class AdminListing {

    private final int itemId;
    private final String itemName;
    private final String sellerName;
    private final String categoryName;
    private final String listingType;
    private final BigDecimal price;
    private final String condition;
    private final String datePosted;
    private final String itemStatus;

    public AdminListing(int itemId, String itemName, String sellerName, String categoryName,
                        String listingType, BigDecimal price, String condition,
                        String datePosted, String itemStatus) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.sellerName = sellerName;
        this.categoryName = categoryName;
        this.listingType = listingType;
        this.price = price;
        this.condition = condition;
        this.datePosted = datePosted;
        this.itemStatus = itemStatus;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getSellerName() { return sellerName; }
    public String getCategoryName() { return categoryName; }
    public String getListingType() { return listingType; }
    public BigDecimal getPrice() { return price; }
    public String getCondition() { return condition; }
    public String getDatePosted() { return datePosted; }
    public String getItemStatus() { return itemStatus; }
}
