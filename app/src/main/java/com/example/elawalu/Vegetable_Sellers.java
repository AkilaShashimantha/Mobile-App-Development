package com.example.elawalu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vegetable_Sellers extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SellerAdapter sellerAdapter;
    private List<Seller> sellerList;
    private DatabaseReference usersRef, itemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vegetable_sellers);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sellerList = new ArrayList<>();
        sellerAdapter = new SellerAdapter(this, sellerList);
        recyclerView.setAdapter(sellerAdapter);


        FirebaseDatabase database = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("Users");
        itemsRef = database.getReference("Items");

        getSupportActionBar().hide();

        ImageView profileBackBtn = findViewById(R.id.sellerDetailsBackBtn);
        profileBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Vegetable_Sellers.this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        loadSellersWithVegetables();
    }

    private void loadSellersWithVegetables() {
        // Since Items are inside Users, we'll query Users directly
        usersRef.orderByChild("role").equalTo("Seller").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                sellerList.clear();

                for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                    String phone = userSnapshot.child("phone").getValue(String.class);
                    if (phone != null) {
                        Seller seller = new Seller();
                        seller.setFirstName(userSnapshot.child("firstName").getValue(String.class));
                        seller.setLastName(userSnapshot.child("lastName").getValue(String.class));
                        seller.setPhone(phone);
                        seller.setCity(userSnapshot.child("city").getValue(String.class));
                        seller.setProfileImageUrl(userSnapshot.child("profileImageUrl").getValue(String.class));

                        // Get items for this seller (from the Items node inside Users)
                        DataSnapshot itemsSnapshot = userSnapshot.child("Items");
                        List<String> vegetables = new ArrayList<>();

                        for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                            String activeStatus = itemSnapshot.child("activeStatus").getValue(String.class);
                            String vegetable = itemSnapshot.child("vegetable").getValue(String.class);

                            if ("1".equals(activeStatus) && vegetable != null) {
                                vegetables.add(vegetable);
                            }
                        }

                        seller.setVegetables(vegetables);
                        sellerList.add(seller);
                    }
                }

                sellerAdapter.notifyDataSetChanged();

                if (sellerList.isEmpty()) {
                    Toast.makeText(Vegetable_Sellers.this, "No sellers found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load sellers", error.toException());
                Toast.makeText(Vegetable_Sellers.this, "Failed to load sellers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}