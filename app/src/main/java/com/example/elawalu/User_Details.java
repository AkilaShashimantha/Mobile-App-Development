package com.example.elawalu;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private TextInputEditText fname, lname, phoneNumber, userEmail, birthday, nic, address, city;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageView menuButton;
    private boolean isMenuOpen = false; // Track the state of the menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        // Initialize views
        initializeViews();

        // Load user data from SharedPreferences
        loadUserData();

        // Set up date picker for birthday
        setupDatePicker();

        // Set up back button
        setupBackButton();

        // Set up menu button with animation
        setupMenuButton();

        // Set up confirm button for profile update
        setupConfirmButton();
    }

    private void initializeViews() {
        profileFirstName = findViewById(R.id.profileFirstName);
        profileLastName = findViewById(R.id.profileLastName);
        nictextLayout = findViewById(R.id.nictextLayout);
        profileBirthdayLayout = findViewById(R.id.profileBirthdayLayout);
        profilePhoneNumberLayout = findViewById(R.id.profilePhoneNumberLayout);
        profileEmailLayout = findViewById(R.id.profileEmailLayout);
        profileaddressLayout = findViewById(R.id.profileaddressLayout);
        profileCityLayout = findViewById(R.id.profileCityLayout);

        fname = findViewById(R.id.fName);
        lname = findViewById(R.id.lName);
        phoneNumber = findViewById(R.id.PhoneNumber);
        userEmail = findViewById(R.id.email);
        birthday = findViewById(R.id.profileBirthday);
        nic = findViewById(R.id.nic);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);

        menuButton = findViewById(R.id.menuButton);
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "Guest");
        String profileImageUrl = sharedPreferences.getString("PROFILE_IMAGE", "");
        String uId = sharedPreferences.getString("USER_ID", "");
        String email = sharedPreferences.getString("EMAIL", "");
        String fName = sharedPreferences.getString("FIRST_NAME", "");
        String lName = sharedPreferences.getString("LAST_NAME", "");
        String phone = sharedPreferences.getString("PHONE", "");
        String getnic = sharedPreferences.getString("NIC", "");
        String getbirthday = sharedPreferences.getString("BIRTHDAY", "");
        String getaddress = sharedPreferences.getString("ADDRESS", "");
        String getcity = sharedPreferences.getString("CITY", "");

        // Load profile image
        ImageView profileImage = findViewById(R.id.profileImage);
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.account_circle_btn)
                    .error(R.drawable.account_circle_btn)
                    .into(profileImage);
        }

        // Set user data to views
        fname.setText(fName);
        lname.setText(lName);
        phoneNumber.setText(phone);
        userEmail.setText(email);
        nic.setText(getnic);
        birthday.setText(getbirthday);
        address.setText(getaddress);
        city.setText(getcity);

        // Initialize Firebase reference
        userRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users").child(uId);
    }

    private void setupDatePicker() {
        birthday.setOnClickListener(v -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date(selection));
                birthday.setText(MessageFormat.format("{0}", date));
            });
            materialDatePicker.show(getSupportFragmentManager(), "tag");
        });
    }

    private void setupBackButton() {
        ImageView profileBackBtn = findViewById(R.id.profileBackBtn);
        profileBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(User_Details.this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupMenuButton() {
        menuButton.setOnClickListener(v -> {
            if (isMenuOpen) {
                // Close the menu and animate back to the menu icon
                animateMenuButton(R.drawable.menu);
                isMenuOpen = false;
            } else {
                // Open the menu and animate to the down arrow icon
                animateMenuButton(R.drawable.down_arrow);
                isMenuOpen = true;
            }

            // Show the popup menu
            showPopupMenu(v);
        });
    }

    private void animateMenuButton(int targetDrawable) {
        // Create a rotation animation (rotate 180 degrees)
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(menuButton, "rotation", isMenuOpen ? 180 : 0, isMenuOpen ? 0 : 180);
        rotationAnimator.setDuration(300); // Animation duration in milliseconds
        rotationAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth easing

        // Create an alpha animation (fade in/out)
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(menuButton, "alpha", isMenuOpen ? 1f : 0.5f, isMenuOpen ? 0.5f : 1f);
        alphaAnimator.setDuration(300);

        // Start the animations
        rotationAnimator.start();
        alphaAnimator.start();

        // Change the image after the animation completes
        rotationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                menuButton.setImageResource(targetDrawable);
            }
        });
    }

    private void showPopupMenu(View v) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(User_Details.this, R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(contextThemeWrapper, v);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

        // Force icons to show using reflection
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

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_sign_out) {
                showSignOutDialog();
                return true;
            } else if (itemId == R.id.menu_delete_account) {
                deleteAccount();
                return true;
            } else if (itemId == R.id.menu_payment_history) {
                showPaymentHistory();
                return true;
            } else if (itemId == R.id.menu_dashboard) {
                openDashboard();
                return true;
            } else {
                return false;
            }
        });

        // Reset the icon when the popup menu is dismissed
        popupMenu.setOnDismissListener(menu -> {
            animateMenuButton(R.drawable.menu);
            isMenuOpen = false;
        });

        popupMenu.show();
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
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
            FirebaseAuth.getInstance().signOut();

            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Successfully signed out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(User_Details.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        currentUser.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        deleteUserDataFromDatabase();

                                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.clear();
                                        editor.apply();

                                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(User_Details.this, Login.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showPaymentHistory() {
        Intent intent = new Intent(User_Details.this, Seller_payment_History.class);
        startActivity(intent);
        finish();
    }

    private void openDashboard() {
        Intent intent = new Intent(User_Details.this, Seller_Dashboard.class);
        startActivity(intent);
        finish();
    }

    private void deleteUserDataFromDatabase() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", "");

        if (!userId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId);

            userRef.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "User data deleted from database", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void setupConfirmButton() {
        Button confirm = findViewById(R.id.confirmBtn);
        confirm.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        String fname = profileFirstName.getEditText().getText().toString().trim();
        String lname = profileLastName.getEditText().getText().toString().trim();
        String nic = nictextLayout.getEditText().getText().toString().trim();
        String birthday = profileBirthdayLayout.getEditText().getText().toString().trim();
        String phoneNumber = profilePhoneNumberLayout.getEditText().getText().toString().trim();
        String address = profileaddressLayout.getEditText().getText().toString().trim();
        String city = profileCityLayout.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname)) {
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

                    Intent intent = new Intent(User_Details.this, User_Details.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean isValidNIC(String nic) {
        return nic.matches("^(\\d{12}|\\d{9}[Vv])$");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^(0\\d{9}|\\+94\\d{9})$");
    }
}