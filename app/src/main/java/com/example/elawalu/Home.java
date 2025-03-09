package com.example.elawalu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.elawalu.User_Details;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import com.example.elawalu.ItemAdapter; // Replace with your actual package name
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;

    private BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    private Button signOutBtn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_btn);

        // Get passed data from Login Activity
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        String firstName = sharedPreferences.getString("FIRST_NAME","");
        String lastName = sharedPreferences.getString("LAST_NAME","");
        String profileImageUrl = sharedPreferences.getString("PROFILE_IMAGE", "");
        String uId = sharedPreferences.getString("USER_ID", "");
        String email = sharedPreferences.getString("EMAIL", "");
        String role = sharedPreferences.getString("ROLE", "");


        // Reference Navigation Drawer Header
        NavigationView navigationView = findViewById(R.id.nav_btn);
        View headerView = navigationView.getHeaderView(0);

        // Find Header Views
        TextView userNameTextView = headerView.findViewById(R.id.userNameTextView);
        ImageView profileImageView = headerView.findViewById(R.id.profileImageView);

        userNameTextView.setText(userName);
        String displayName = firstName +" "+ lastName ;
        if(userName.isEmpty()){
            userNameTextView.setText(displayName);
        }

        // Load Profile Image using Glide (Recommended)
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.account_circle_btn)  // Placeholder Image
                    .error(R.drawable.account_circle_btn)           // Error Image
                    .into(profileImageView);
        }

        View mainLayout = findViewById(R.id.main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(view -> showSignOutDialog());


        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            mainLayout.getWindowVisibleDisplayFrame(r);
            int screenHeight = mainLayout.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                // Keyboard is closed
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_home) {
                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), Search.class));

                return true;
            } else if (id == R.id.bottom_Cart) {
                startActivity(new Intent(getApplicationContext(), View_Cart.class));

                return true;
            }

            return false;
        });


        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_btn);

        // Check if Views Exist
        if (drawerLayout == null || navigationView == null) {
            Toast.makeText(this, "Error: Drawer or NavigationView is missing in XML!", Toast.LENGTH_LONG).show();
            return; // Prevents app crash
        }

        // Set Up Drawer Toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Prevent Crash: Check if ActionBar Exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        navigationView.setCheckedItem(R.id.home);
        // Handle Navigation Clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Call showToast() function to display messages
                if (id == R.id.home) {
                    showToast("Home Selected");


                } else if (id == R.id.profile) {
                    showToast("Profile Selected");
                    startActivity(new Intent(Home.this, User_Details.class));

                } else if (id == R.id.addProduct) {
                    showToast("Add product Selected");

                    startActivity(new Intent(Home.this, Items.class));
                }
                else if (id == R.id.seller) {
                    showToast("Vegetable Sellers Selected");

                    startActivity(new Intent(Home.this, Vegetable_Sellers.class));
                }

                else if (id == R.id.paymentHistory) {
                    showToast("Payment History Selected");

                    startActivity(new Intent(Home.this, Payment_History.class));
                }
                else {
                    return false; // If no valid item is selected
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true; // Return true when an item is handled
            }
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with an empty list
        adapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Fetch data from Firebase and update the adapter
        fetchDataAndUpdateAdapter();

    }

    private void fetchDataAndUpdateAdapter() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Item> itemList = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String userName = firstName + " " + lastName;

                    if (userSnapshot.hasChild("Items")) {
                        DataSnapshot itemsSnapshot = userSnapshot.child("Items");

                        for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                            Item item = itemSnapshot.getValue(Item.class);
                            if (item != null) {
                                item.setUserName(userName);
                                itemList.add(item);
                            }
                        }
                    }
                }

                // Update the adapter with the new data
                adapter.itemList = itemList;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to show Toast messages (Avoids repetition)
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

    private void showSignOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> signOut())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clears all stored user data
        editor.apply();

        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent intent = new Intent(Home.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

}
