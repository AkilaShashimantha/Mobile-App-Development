package com.example.elawalu;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private EditText searchEditText;
    private ImageView searchButton;
    private RecyclerView searchRecyclerView;
    private ItemAdapter searchAdapter;
    private List<Item> allItems = new ArrayList<>();
    private LinearLayout noResultsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        getSupportActionBar().hide();

        // Initialize views
        View mainLayout = findViewById(R.id.main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        searchEditText = findViewById(R.id.searchText);
        searchButton = findViewById(R.id.searchBtn);
        searchRecyclerView = findViewById(R.id.recycleView);
        noResultsLayout = findViewById(R.id.noResultsLayout);

        // Set up RecyclerView
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new ItemAdapter(new ArrayList<>(), false);
        searchRecyclerView.setAdapter(searchAdapter);

        // Set item click listener
        searchAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(Search.this, View_Product.class);
            intent.putExtra("productId", item.getItemId());
            intent.putExtra("vegetable", item.getVegetable());
            intent.putExtra("quantity", item.getQuantity());
            intent.putExtra("location", item.getLocation());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("userName", item.getUserName());
            intent.putExtra("contactNumber", item.getContactNumber());
            intent.putExtra("sellerId", item.getSellerId());
            startActivity(intent);
        });

        // Handle keyboard visibility
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            mainLayout.getWindowVisibleDisplayFrame(r);
            int screenHeight = mainLayout.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        // Set up bottom navigation
        setupBottomNavigation();

        // Initialize the toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // Fetch all items from Firebase
        fetchAllItems();

        // Set up search functionality
        setupSearch();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_search);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), Home.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                return true;
            } else if (id == R.id.bottom_search) {
                return true;
            } else if (id == R.id.bottom_Cart) {
                startActivity(new Intent(getApplicationContext(), View_Cart.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            }
            return false;
        });
    }

    private void fetchAllItems() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allItems.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String userName = firstName + " " + lastName;

                    if (userSnapshot.hasChild("Items")) {
                        DataSnapshot itemsSnapshot = userSnapshot.child("Items");

                        for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                            Item item = itemSnapshot.getValue(Item.class);
                            if (item != null && "1".equals(item.getActiveStatus())) {
                                item.setUserName(userName);
                                item.setItemId(itemSnapshot.getKey());
                                item.setSellerId(userSnapshot.getKey());
                                allItems.add(item);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Search.this, "Failed to fetch items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        // Search when button is clicked
        searchButton.setOnClickListener(v -> performSearch());

        // Search as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        String searchQuery = searchEditText.getText().toString().trim().toLowerCase();

        if (searchQuery.isEmpty()) {
            searchAdapter.addItems(new ArrayList<>());
            noResultsLayout.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.VISIBLE);
            return;
        }

        List<Item> filteredItems = new ArrayList<>();
        for (Item item : allItems) {
            if (item.getVegetable().toLowerCase().contains(searchQuery)) {
                filteredItems.add(item);
            }
        }

        searchAdapter.addItems(filteredItems);

        if (filteredItems.isEmpty()) {
            noResultsLayout.setVisibility(View.VISIBLE);
            searchRecyclerView.setVisibility(View.GONE);
        } else {
            noResultsLayout.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        fetchAllItems();
    }
}