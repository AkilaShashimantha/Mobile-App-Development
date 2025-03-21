package com.example.elawalu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Seller_payment_History extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> allItems = new ArrayList<>(); // All items fetched from Firebase
    private int currentPage = 0; // Current page number
    private static final int ITEMS_PER_PAGE = 5;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seller_payment_history);

        getSupportActionBar().hide();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User is not logged in, redirect to login screen
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Back button
        ImageView sellerDashBackBtn = findViewById(R.id.sellerPaymentBackBtn);
        sellerDashBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Seller_payment_History.this, User_Details.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView); // Ensure this ID exists in your layout
        if (recyclerView == null) {
            Log.e("Seller_Payment_History", "RecyclerView not found");
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with an empty list and set showPaymentDateTime to true
        adapter = new ItemAdapter(new ArrayList<>(), true);
        recyclerView.setAdapter(adapter);

        adapter.setOnLoadMoreListener(() -> loadNextPage());

        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Get the current user's unique ID
        String currentUserId = currentUser.getUid();

        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allItems.clear(); // Clear the existing list

                if (snapshot.hasChild("SellingPayments")) {
                    DataSnapshot itemsSnapshot = snapshot.child("SellingPayments");

                    for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                        Item item = itemSnapshot.getValue(Item.class);
                        if (item != null && "1".equals(item.getActiveStatus())) { // Filter by activeStatus = "1"
                            String firstName = snapshot.child("firstName").getValue(String.class);
                            String lastName = snapshot.child("lastName").getValue(String.class);
                            String userName = firstName + " " + lastName;
                            item.setUserName(userName);
                            item.setPaymentDateTime(itemSnapshot.child("paymentDateTime").getValue(String.class));
                            allItems.add(item);
                        }
                    }
                }

                // Sort items by paymentDateTime in descending order (newest first)
                Collections.sort(allItems, (item1, item2) -> {
                    if (item1.getPaymentDateTime() == null || item2.getPaymentDateTime() == null) {
                        return 0; // Handle null values
                    }
                    return item2.getPaymentDateTime().compareTo(item1.getPaymentDateTime()); // Descending order
                });

                // Load the first page of data
                loadNextPage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Seller_payment_History.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNextPage() {
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allItems.size());

        if (start < allItems.size()) {
            List<Item> nextItems = allItems.subList(start, end);
            adapter.addItems(nextItems); // Add the next set of items to the adapter
            currentPage++; // Increment the page counter
        }

        // Update the "Load More" button visibility
        adapter.setShowLoadMoreButton(end < allItems.size());
    }
}