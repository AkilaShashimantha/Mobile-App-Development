package com.example.elawalu;

import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        itemAdapter = new SellerItemAdapter(itemList);
        recyclerView.setAdapter(itemAdapter);

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("SellerDashboard", "User ID: " + userId); // Log the user ID
            loadAllItems(userId); // Load items for the current user
        } else {
            Log.e("SellerDashboard", "User not logged in");
        }
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
                itemList.clear(); // Clear the list before adding new items

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    SellerItem item = itemSnapshot.getValue(SellerItem.class);
                    if (item != null) {
                        itemList.add(item);
                        Log.d("SellerDashboard", "Item added: " + item.getVegetable()); // Log added items
                    }
                }

                itemAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Database error: " + databaseError.getMessage());
            }
        });
    }
}