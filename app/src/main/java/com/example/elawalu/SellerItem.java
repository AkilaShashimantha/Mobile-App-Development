package com.example.elawalu;

public class SellerItem {
    private String itemId; // Add this field
    private String vegetable;
    private String quantity;
    private String location;
    private String contactNumber;
    private String price;
    private String activeStatus;

    // Default constructor required for Firebase
    public SellerItem() {}

    // Constructor with all fields
    public SellerItem(String itemId, String vegetable, String quantity, String location, String contactNumber, String price, String activeStatus) {
        this.itemId = itemId;
        this.vegetable = vegetable;
        this.quantity = quantity;
        this.location = location;
        this.contactNumber = contactNumber;
        this.price = price;
        this.activeStatus = activeStatus;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getVegetable() {
        return vegetable;
    }

    public void setVegetable(String vegetable) {
        this.vegetable = vegetable;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }
}