package com.uniloop.model;

public class Item {

    private int itemId;
    private int sellerId;
    private int categoryId;
    private String itemName;
    private String description;
    private double price;
    private String imagePath;
    private String condition;
    private String listingType;
    private String itemStatus;
    private String datePosted;

    public Item() {
    }

    public Item(int itemId, int sellerId, int categoryId,
                String itemName, String description, double price,
                String imagePath, String condition,
                String listingType, String itemStatus,
                String datePosted) {

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
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getListingType() {
        return listingType;
    }

    public void setListingType(String listingType) {
        this.listingType = listingType;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }
}