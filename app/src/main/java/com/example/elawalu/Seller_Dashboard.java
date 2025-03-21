package com.example.elawalu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
import java.util.List;

public class Seller_Dashboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SellerItemAdapter itemAdapter;
    private List<SellerItem> itemList;
    private List<SellerItem> allItems; // Store all items for filtering
    private RadioGroup radioGroup;
    private RadioButton activeProduct, deactiveProduct;
    private TextView noDataTextView; // TextView to show "No data to display"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        getSupportActionBar().hide();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        allItems = new ArrayList<>(); // Initialize the list to store all items

        // Initialize RadioGroup and RadioButtons
        radioGroup = findViewById(R.id.radioGroup);
        activeProduct = findViewById(R.id.activeProduct);
        deactiveProduct = findViewById(R.id.deactiveProduct);

        // Initialize the "No data to display" TextView
        noDataTextView = findViewById(R.id.noDataTextView);

        // Set up RadioGroup listener
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.activeProduct) {
                    filterItems("1"); // Show active items
                } else if (checkedId == R.id.deactiveProduct) {
                    filterItems("0"); // Show deactive items
                }
            }
        });

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            itemAdapter = new SellerItemAdapter(itemList, userId);
            recyclerView.setAdapter(itemAdapter);
            Log.d("SellerDashboard", "User ID: " + userId); // Log the user ID
            loadAllItems(userId); // Load items for the current user
        } else {
            Log.e("SellerDashboard", "User not logged in");
        }

        // Back button
        ImageView sellerDashBackBtn = findViewById(R.id.sellerDashBackBtn);
        sellerDashBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Seller_Dashboard.this, User_Details.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadAllItems(String userId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference usersRef = database.getReference("Users");

        // Reference the current user's node
        DatabaseReference currentUserRef = usersRef.child(userId);

        // Access the "Items" node under the current user
        DatabaseReference itemsRef = currentUserRef.child("Items");

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("SellerDashboard", "DataSnapshot count: " + dataSnapshot.getChildrenCount()); // Log the number of items
                allItems.clear(); // Clear the list before adding new items

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    SellerItem item = itemSnapshot.getValue(SellerItem.class);
                    if (item != null) {
                        item.setItemId(itemSnapshot.getKey()); // Set the itemId from the snapshot key
                        allItems.add(item);
                        Log.d("SellerDashboard", "Item added: " + item.getVegetable() + ", ID: " + item.getItemId()); // Log added items
                    }
                }

                // By default, show active products
                filterItems("1");
                activeProduct.setChecked(true); // Set the "Active product" radio button as checked
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void filterItems(String activeStatus) {
        itemList.clear(); // Clear the current list

        // Filter items based on activeStatus
        for (SellerItem item : allItems) {
            if (item.getActiveStatus().equals(activeStatus)) {
                itemList.add(item);
            }
        }

        // Show "No data to display" if the list is empty
        if (itemList.isEmpty()) {
            noDataTextView.setVisibility(View.VISIBLE); // Show the TextView
            recyclerView.setVisibility(View.GONE); // Hide the RecyclerView
        } else {
            noDataTextView.setVisibility(View.GONE); // Hide the TextView
            recyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView
        }

        itemAdapter.notifyDataSetChanged(); // Notify adapter of data changes
    }
}