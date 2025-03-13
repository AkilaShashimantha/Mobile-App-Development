package com.example.elawalu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;


//import com.example.elawalu.User_Details;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User_Details extends AppCompatActivity {

    private DatabaseReference userRef;


    private TextInputLayout profileFirstName, profileLastName, nictextLayout, profileBirthdayLayout, profilePhoneNumberLayout,
            profileEmailLayout, profileaddressLayout, profileCityLayout;

    private TextInputEditText fname,lname, phoneNumber, userEmail, birthday, nic , address,  city;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        getSupportActionBar().hide();


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "Guest");
        String profileImageUrl = sharedPreferences.getString("PROFILE_IMAGE", "");
        String uId = sharedPreferences.getString("USER_ID", "");
        String email = sharedPreferences.getString("EMAIL", "");
        String fName = sharedPreferences.getString("FIRST_NAME", "");
        String lName = sharedPreferences.getString("LAST_NAME", "");
        String gender = sharedPreferences.getString("GENDER", "");
        String phone = sharedPreferences.getString("PHONE", "");
        String role = sharedPreferences.getString("ROLE", "");
        String getnic = sharedPreferences.getString("NIC", "");
        String getbirthday = sharedPreferences.getString("BIRTHDAY", "");
        String getaddress = sharedPreferences.getString("ADDRESS", "");
        String getcity = sharedPreferences.getString("CITY","");


        ImageView profileImage = findViewById(R.id.profileImage);
         fname = findViewById(R.id.fName);
         lname = findViewById(R.id.lName);
         phoneNumber = findViewById(R.id.PhoneNumber);
         userEmail = findViewById(R.id.email);
         birthday = findViewById(R.id.profileBirthday);
             nic = findViewById(R.id.nic);
            address = findViewById(R.id.address);
            city = findViewById(R.id.city);

        profileFirstName = findViewById(R.id.profileFirstName);
        profileLastName = findViewById(R.id.profileLastName);
        nictextLayout = findViewById(R.id.nictextLayout);
        profileBirthdayLayout = findViewById(R.id.profileBirthdayLayout);
        profilePhoneNumberLayout = findViewById(R.id.profilePhoneNumberLayout);
        profileEmailLayout = findViewById(R.id.profileEmailLayout);
        profileaddressLayout = findViewById(R.id.profileaddressLayout);
        profileCityLayout = findViewById(R.id.profileCityLayout);


        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.account_circle_btn)  // Placeholder Image
                    .error(R.drawable.account_circle_btn)           // Error Image
                    .into(profileImage);
        }

        fname.setText(fName);
        lname.setText(lName);
        phoneNumber.setText(phone);
        userEmail.setText(email);
        nic.setText(getnic);
        birthday.setText(getbirthday);
        address.setText(getaddress);
        city.setText(getcity);


//Date Picker


birthday.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {

                String date = new SimpleDateFormat("dd-MM-yyy", Locale.getDefault()).format(new Date(selection));
                birthday.setText(MessageFormat.format("{0}",date));

            }
        });
        materialDatePicker.show(getSupportFragmentManager(),"tag");

    }
});

ImageView profileBackBtn = findViewById(R.id.profileBackBtn);

profileBackBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(User_Details.this, Home.class);
        startActivity(intent);
        finish();
    }
});

        userRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users").child(uId);

        Button confirm = findViewById(R.id.confirmBtn);
        confirm.setOnClickListener(v -> updateProfile());

        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a ContextThemeWrapper to apply the custom style
                ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(User_Details.this, R.style.CustomPopupMenu);
                PopupMenu popupMenu = new PopupMenu(contextThemeWrapper, v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

                // Force icons to show using reflection (if needed)
                try {
                    Field field = popupMenu.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.menu_sign_out) {
                            // Handle Sign Out
                            showSignOutDialog();
                            return true;
                        } else if (itemId == R.id.menu_delete_account) {
                            // Handle Delete Account
                            deleteAccount();
                            return true;
                        } else if (itemId == R.id.menu_payment_history) {
                            // Handle Payment History
                            showPaymentHistory();
                            return true;
                        } else if (itemId == R.id.menu_dashboard) {
                            // Handle Dashboard
                            openDashboard();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });

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
        // Step 1: Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Step 2: Clear the user session data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clears all stored user data
        editor.apply();

        // Step 3: Show a success message
        Toast.makeText(this, "Successfully signed out", Toast.LENGTH_SHORT).show();

        // Step 4: Navigate to the Login page
        Intent intent = new Intent(User_Details.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the back stack
        startActivity(intent);
        finish(); // Close the current activity
    }
    private void deleteAccount() {
        // Step 1: Show a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Step 2: Get the current user
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        // Step 3: Delete the user from Firebase Authentication
                        currentUser.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Step 4: Delete user data from Firebase Realtime Database (if applicable)
                                        deleteUserDataFromDatabase();

                                        // Step 5: Clear the user session data from SharedPreferences
                                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.clear(); // Clears all stored user data
                                        editor.apply();

                                        // Step 6: Show a success message
                                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

                                        // Step 7: Navigate to the Login page
                                        Intent intent = new Intent(User_Details.this, Login.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the back stack
                                        startActivity(intent);
                                        finish(); // Close the current activity
                                    } else {
                                        // Handle failure to delete account
                                        Toast.makeText(this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Handle case where the user is not logged in
                        Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showPaymentHistory() {
        // Handle Payment History
        Toast.makeText(this, "Payment History Clicked", Toast.LENGTH_SHORT).show();
    }


    private void openDashboard() {
        // Handle Dashboard
        Toast.makeText(this, "Dashboard Clicked", Toast.LENGTH_SHORT).show();

        // Navigate to the Seller_Dashboard activity
        Intent intent = new Intent(User_Details.this, Seller_Dashboard.class);
        startActivity(intent);
        finish(); // Close the current activity
    }


    private void deleteUserDataFromDatabase() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", "");

        if (!userId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId);

            userRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Data deleted successfully
                        Toast.makeText(this, "User data deleted from database", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to delete data
                        Toast.makeText(this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateProfile(){

        String fname = profileFirstName.getEditText().getText().toString().trim();
        String lname =  profileLastName.getEditText().getText().toString().trim();
        String nic = nictextLayout.getEditText().getText().toString().trim();
        String birthday =  profileBirthdayLayout.getEditText().getText().toString().trim();
        String phoneNumber = profilePhoneNumberLayout.getEditText().getText().toString().trim();
        String address = profileaddressLayout.getEditText().getText().toString().trim();
        String city = profileCityLayout.getEditText().getText().toString().trim();

        if(TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname)){
            Toast.makeText(this, "Please fill the first Name and Last Name!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidNIC(nic)) {
            nictextLayout.setError("Invalid NIC! It should be either 12 digits or 9 digits followed by V/v.");
            return;
        }
        if (!isValidPhoneNumber(phoneNumber)) {
            profilePhoneNumberLayout.setError("Invalid phone number! Start with 0 and have 9 digits after that.");
            return;
        }

        // Update user data in Firebase
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("firstName", fname);
        userUpdates.put("lastName", lname);
        userUpdates.put("nic", nic);
        userUpdates.put("birthday", birthday);
        userUpdates.put("phone", phoneNumber);
        userUpdates.put("address", address);
        userUpdates.put("city", city);

        userRef.updateChildren(userUpdates)
                .addOnSuccessListener(aVoid -> {

                            // ✅ 1. Save updated data to SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("FIRST_NAME", fname);
                            editor.putString("LAST_NAME", lname);
                            editor.putString("NIC", nic);
                            editor.putString("BIRTHDAY", birthday);
                            editor.putString("PHONE", phoneNumber);
                            editor.putString("ADDRESS", address);
                            editor.putString("CITY", city);
                            editor.apply();

                            Toast.makeText(getApplicationContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();

                            // ✅ 2. Restart activity to refresh data
                            Intent intent = new Intent(User_Details.this, User_Details.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                       )
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private boolean isValidNIC(String nic) {
        // Regex for Sri Lankan phone number validation
        return nic.matches("^(\\d{12}|\\d{9}[Vv])$");
    }

    private boolean isValidPhoneNumber(String phone) {
        // Regex for Sri Lankan phone number validation
        return phone.matches("^(0\\d{9}|\\+94\\d{9})$");
    }




}
