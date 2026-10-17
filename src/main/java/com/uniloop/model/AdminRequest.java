package com.uniloop.model;

public class AdminRequest {

    private final int requestId;
    private final int itemId;
    private final String itemName;
    private final String buyerName;
    private final String sellerName;
    private final String requestStatus;
    private final String requestDate;

    public AdminRequest(int requestId, int itemId, String itemName, String buyerName,
                        String sellerName, String requestStatus, String requestDate) {
        this.requestId = requestId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.buyerName = buyerName;
        this.sellerName = sellerName;
        this.requestStatus = requestStatus;
        this.requestDate = requestDate;
    }

    public int getRequestId() { return requestId; }
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getBuyerName() { return buyerName; }
    public String getSellerName() { return sellerName; }
    public String getRequestStatus() { return requestStatus; }
    public String getRequestDate() { return requestDate; }
}
