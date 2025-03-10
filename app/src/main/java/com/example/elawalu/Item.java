package com.example.elawalu; // Replace with your package name

import java.io.Serializable;

public class Item implements Serializable {
    private String vegetable;
    private String quantity;
    private String location;
    private String contactNumber;
    private String price;
    private String userName;

    // Default constructor required for Firebase
    public Item() {}

    public Item(String vegetable, String quantity, String location, String contactNumber, String price, String userName) {
        this.vegetable = vegetable;
        this.quantity = quantity;
        this.location = location;
        this.contactNumber = contactNumber;
        this.price = price;
        this.userName = userName;
    }

    // Getters and Setters
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}