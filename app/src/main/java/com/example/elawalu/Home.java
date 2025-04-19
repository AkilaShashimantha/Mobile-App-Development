package com.example.elawalu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> allItems = new ArrayList<>();
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 5;

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private boolean showLoadMoreButton;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_btn);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        recyclerView = findViewById(R.id.recyclerView);

        // Get passed data from Login Activity
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        String firstName = sharedPreferences.getString("FIRST_NAME", "");
        String lastName = sharedPreferences.getString("LAST_NAME", "");
        String profileImageUrl = sharedPreferences.getString("PROFILE_IMAGE", "");
        String uId = sharedPreferences.getString("USER_ID", "");
        String email = sharedPreferences.getString("EMAIL", "");
        String role = sharedPreferences.getString("ROLE", "");

        // Reference Navigation Drawer Header
        View headerView = navigationView.getHeaderView(0);

        // Find Header Views
        TextView userNameTextView = headerView.findViewById(R.id.userNameTextView);
        ImageView profileImageView = headerView.findViewById(R.id.profileImageView);

        userNameTextView.setText(userName);
        String displayName = firstName + " " + lastName;
        if (userName.isEmpty()) {
            userNameTextView.setText(displayName);
        }

        // Load Profile Image
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.account_circle_btn)
                    .error(R.drawable.account_circle_btn)
                    .into(profileImageView);
        }

        // Handle keyboard visibility
        View mainLayout = findViewById(R.id.main);
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

        // Set up Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_home) {
                return true;
            } else if (id == R.id.bottom_search) {
                Intent searchIntent = new Intent(getApplicationContext(), Search.class);
                searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(searchIntent);
                return true;
            } else if (id == R.id.bottom_Cart) {
                Intent cartIntent = new Intent(getApplicationContext(), View_Cart.class);
                cartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(cartIntent);
                return true;
            }

            return false;
        });

        // Set up Drawer Toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle Navigation Drawer Clicks
        navigationView.setCheckedItem(R.id.home);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                showToast("Home Selected");
            } else if (id == R.id.profile) {
                showToast("Profile Selected");
                startActivity(new Intent(Home.this, User_Details.class));
            } else if (id == R.id.addProduct) {
                showToast("Add product Selected");
                startActivity(new Intent(Home.this, Items.class));
            } else if (id == R.id.activeProduct) {
                showToast("Vegetable Sellers Selected");
                startActivity(new Intent(Home.this, Vegetable_Sellers.class));
            } else if (id == R.id.paymentHistory) {
                showToast("Payment History Selected");
                startActivity(new Intent(Home.this, Payment_History.class));
            } else {
                return false;
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(new ArrayList<>(), false);
        recyclerView.setAdapter(adapter);

        // Set "Load More" listener
        adapter.setOnLoadMoreListener(this::loadNextPage);

        // Set item click listener - UPDATED TO USE ITEM OBJECT
        adapter.setOnItemClickListener(item -> {
            if (item != null) {
                Intent intent = new Intent(Home.this, View_Product.class);
                intent.putExtra("productId", item.getItemId());
                intent.putExtra("userName", item.getUserName());
                intent.putExtra("vegetable", item.getVegetable());
                intent.putExtra("quantity", item.getQuantity());
                intent.putExtra("location", item.getLocation());
                intent.putExtra("contactNumber", item.getContactNumber());
                intent.putExtra("price", item.getPrice());
                intent.putExtra("sellerId", item.getSellerId());
                startActivity(intent);
            } else {
                Toast.makeText(Home.this, "Invalid item data", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch initial data
        fetchDataFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
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

                // Sort items by paymentDateTime
                Collections.sort(allItems, (item1, item2) -> {
                    if (item1.getPaymentDateTime() == null || item2.getPaymentDateTime() == null) {
                        return 0;
                    }
                    return item2.getPaymentDateTime().compareTo(item1.getPaymentDateTime());
                });

                // Reset pagination and load first page
                currentPage = 0;
                adapter.addItems(new ArrayList<>()); // Clear existing items
                loadNextPage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNextPage() {
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allItems.size());

        if (start < allItems.size()) {
            List<Item> nextItems = allItems.subList(start, end);
            adapter.addItems(nextItems);
            currentPage++;
        }

        adapter.setShowLoadMoreButton(end < allItems.size());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}