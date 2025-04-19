package com.example.elawalu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class View_Product extends AppCompatActivity {

    private static final String TAG = "View_Product";
    private Button addCartBtn;
    private ImageView viewProductBackbtn, imageVegetable;
    private TextView textVegetable, textQuantity, textLocation, textPrice, textSellerName, textContactNumber;

    private String productId, productName, price, location, sellerName, sellerContact, sellerId, quantity;
    private DatabaseReference cartRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);
        getSupportActionBar().hide();

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        cartRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Carts").child(currentUserId);

        // Initialize views
        initializeViews();

        // Retrieve and validate intent data
        if (!getIntentData()) {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set product details
        setProductDetails();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        imageVegetable = findViewById(R.id.imageView5);
        textVegetable = findViewById(R.id.textView19);
        textQuantity = findViewById(R.id.vegetableQuantity);
        textLocation = findViewById(R.id.vegetableLocation);
        textPrice = findViewById(R.id.vegetablePrice);
        textSellerName = findViewById(R.id.vegetableSeller);
        textContactNumber = findViewById(R.id.vegetableContact);
        addCartBtn = findViewById(R.id.addCartBtn);
        viewProductBackbtn = findViewById(R.id.viewProductBackbtn);
    }

    private boolean getIntentData() {
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        productName = intent.getStringExtra("vegetable");
        quantity = intent.getStringExtra("quantity");
        location = intent.getStringExtra("location");
        price = intent.getStringExtra("price");
        sellerName = intent.getStringExtra("userName");
        sellerContact = intent.getStringExtra("contactNumber");
        sellerId = intent.getStringExtra("sellerId");

        // Debug logging
        Log.d(TAG, "Received intent data:");
        Log.d(TAG, "productId: " + productId);
        Log.d(TAG, "productName: " + productName);
        Log.d(TAG, "price: " + price);
        Log.d(TAG, "quantity: " + quantity);
        Log.d(TAG, "location: " + location);
        Log.d(TAG, "sellerName: " + sellerName);
        Log.d(TAG, "sellerContact: " + sellerContact);
        Log.d(TAG, "sellerId: " + sellerId);

        // Validate required fields
        if (productId == null) {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (productName == null) {
            Toast.makeText(this, "Product name is missing", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (price == null) {
            Toast.makeText(this, "Price is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void setProductDetails() {
        textVegetable.setText(productName);
        textQuantity.setText(quantity != null ? quantity : "1");
        textLocation.setText(location != null ? location : "N/A");
        textPrice.setText(price);
        textSellerName.setText(sellerName != null ? sellerName : "N/A");
        textContactNumber.setText(sellerContact != null ? sellerContact : "N/A");

        // Set vegetable image
        imageVegetable.setImageResource(getVegetableImageResource(productName));
    }

    private void setupClickListeners() {
        viewProductBackbtn.setOnClickListener(v -> finish());

        addCartBtn.setOnClickListener(v -> {
            try {
                addToCart();
            } catch (Exception e) {
                Log.e(TAG, "Error adding to cart", e);
                Toast.makeText(View_Product.this, "Error adding to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart() {
        // Parse price safely
        double parsedPrice;
        try {
            parsedPrice = Double.parseDouble(price.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create cart item object
        HashMap<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", productId);
        cartItem.put("productName", productName);
        cartItem.put("price", parsedPrice);
        cartItem.put("quantity", 1);
        if (location != null) cartItem.put("location", location);
        if (sellerName != null) cartItem.put("sellerName", sellerName);
        if (sellerContact != null) cartItem.put("sellerContact", sellerContact);
        if (sellerId != null) cartItem.put("sellerId", sellerId);
        cartItem.put("timestamp", System.currentTimeMillis());

        // Add to user's cart in Firebase
        cartRef.child(productId).setValue(cartItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(View_Product.this, "Added to cart successfully", Toast.LENGTH_SHORT).show();
                        updateUserSessionWithCart();
                        Intent intent = new Intent(View_Product.this, Home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(View_Product.this, "Failed to add to cart: " +
                                        (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserSessionWithCart() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("hasCartItems", true);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error updating user session", e);
        }
    }

    private int getVegetableImageResource(String vegetableName) {
        if (vegetableName == null) return R.drawable.elavaluokkoma;

        switch (vegetableName.toLowerCase()) {
            case "tomato": return R.drawable.thakkali;
            case "carrots": return R.drawable.carrot;
            case "cabbage": return R.drawable.gova;
            case "pumpkin": return R.drawable.pumking;
            case "brinjols": return R.drawable.brinjol;
            case "ladies fingers": return R.drawable.ladies_fingers;
            case "onions": return R.drawable.b_onion;
            case "potato": return R.drawable.potato;
            case "beetroots": return R.drawable.beetroot;
            case "leeks": return R.drawable.leeks;
            default: return R.drawable.elavaluokkoma;
        }
    }
}