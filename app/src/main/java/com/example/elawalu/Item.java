package com.example.elawalu;

import java.io.Serializable;

public class Item implements Serializable {
    private String itemId;
    private String vegetable;
    private String quantity;
    private String location;
    private String contactNumber;
    private String price;
    private String activeStatus;
    private String userName;
    private String paymentDateTime;
    private String sellerId;

    public Item() {}

    public Item(String itemId, String vegetable, String quantity, String location,
                String contactNumber, String price, String activeStatus,
                String userName, String paymentDateTime, String sellerId) {
        this.itemId = itemId;
        this.vegetable = vegetable;
        this.quantity = quantity;
        this.location = location;
        this.contactNumber = contactNumber;
        this.price = price;
        this.activeStatus = activeStatus;
        this.userName = userName;
        this.paymentDateTime = paymentDateTime;
        this.sellerId = sellerId;
    }

    // Getters and Setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getVegetable() { return vegetable; }
    public void setVegetable(String vegetable) { this.vegetable = vegetable; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPaymentDateTime() { return paymentDateTime; }
    public void setPaymentDateTime(String paymentDateTime) { this.paymentDateTime = paymentDateTime; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
}