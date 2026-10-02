package com.uniloop.model;

public class Request {

    private int requestId;
    private int itemId;
    private int buyerId;
    private String imagePath;

    private String itemName;
    private String buyerName;
    private String sellerName;

    private String requestStatus;
    private String requestDate;


    public Request() {
    }


    public Request(int requestId, int itemId, int buyerId,
                   String itemName, String buyerName,
                   String requestStatus, String requestDate) {

        this.requestId = requestId;
        this.itemId = itemId;
        this.buyerId = buyerId;
        this.itemName = itemName;
        this.buyerName = buyerName;
        this.requestStatus = requestStatus;
        this.requestDate = requestDate;
    }


    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }


    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }


    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }


    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }


    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }


    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }


    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getImagePath() {
    return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getSellerName() {
    return sellerName;
}

public void setSellerName(String sellerName) {
    this.sellerName = sellerName;
}
}