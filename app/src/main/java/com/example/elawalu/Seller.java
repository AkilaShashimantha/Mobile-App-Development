package com.example.elawalu;

import java.util.ArrayList;
import java.util.List;

public class Seller {
    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private String profileImageUrl;
    private List<String> vegetables = new ArrayList<>();

    public Seller() {}

    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public List<String> getVegetables() { return vegetables; }
    public void setVegetables(List<String> vegetables) { this.vegetables = vegetables; }

    public void addVegetable(String vegetable) {
        if (!vegetables.contains(vegetable)) {
            vegetables.add(vegetable);
        }
    }
}