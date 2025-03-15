package com.example.elawalu;

public class SellerItem {
    private String contactNumber;
    private String location;
    private String price;
    private String quantity;
    private String vegetable;
    private String userId; // Add this field

    // Default constructor required for Firebase
    public SellerItem() {
    }

    public SellerItem(String contactNumber, String location, String price, String quantity, String vegetable, String userId) {
        this.contactNumber = contactNumber;
        this.location = location;
        this.price = price;
        this.quantity = quantity;
        this.vegetable = vegetable;
        this.userId = userId; // Initialize userId
    }

    // Getters and setters
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getVegetable() {
        return vegetable;
    }

    public void setVegetable(String vegetable) {
        this.vegetable = vegetable;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}