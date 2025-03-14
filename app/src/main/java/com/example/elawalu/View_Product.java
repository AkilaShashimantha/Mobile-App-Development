package com.example.elawalu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class View_Product extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        getSupportActionBar().hide();

        // Initialize views
        ImageView imageVegetable = findViewById(R.id.imageView5);
        TextView textVegetable = findViewById(R.id.textView19);
        TextView textQuantity = findViewById(R.id.vegetableQuantity);
        TextView textLocation = findViewById(R.id.vegetableLocation);
        TextView textPrice = findViewById(R.id.vegetablePrice);
        TextView textSellerName = findViewById(R.id.vegetableSeller);
        TextView textContactNumber = findViewById(R.id.vegetableContact);

        // Retrieve data from the intent
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String vegetable = intent.getStringExtra("vegetable");
        String quantity = intent.getStringExtra("quantity");
        String location = intent.getStringExtra("location");
        String contactNumber = intent.getStringExtra("contactNumber");
        String price = intent.getStringExtra("price");

        // Display the item's details
        textVegetable.setText(vegetable);
        textQuantity.setText(quantity);
        textLocation.setText(location);
        textPrice.setText("Rs. " + price + " per kg");
        textSellerName.setText(userName);
        textContactNumber.setText(contactNumber);

        // Set vegetable image dynamically
        int vegetableImageResId = getVegetableImageResource(vegetable);
        imageVegetable.setImageResource(vegetableImageResId);

        ImageView viewProductBackbtn = findViewById(R.id.viewProductBackbtn);
        viewProductBackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(View_Product.this ,Home.class);
                // Clear the back stack and start a new task
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    // Helper method to get the vegetable image resource
    private int getVegetableImageResource(String vegetableName) {
        switch (vegetableName.toLowerCase()) {
            case "tomato":
                return R.drawable.thakkali;
            case "carrots":
                return R.drawable.carrot;
            case "cabbage":
                return R.drawable.gova;
            case "pumpkin":
                return R.drawable.pumking;
            case "brinjols":
                return R.drawable.brinjol;
            case "ladies fingers":
                return R.drawable.ladies_fingers;
            case "onions":
                return R.drawable.b_onion;
            case "potato":
                return R.drawable.potato;
            case "beetroots":
                return R.drawable.beetroot;
            case "leeks":
                return R.drawable.leeks;
            // Add more cases for other vegetables
            default:
                return R.drawable.elavaluokkoma; // A default image if no match is foun
        }
    }
}