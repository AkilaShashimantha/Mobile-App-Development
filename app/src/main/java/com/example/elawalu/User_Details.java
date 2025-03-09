package com.example.elawalu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;


//import com.example.elawalu.User_Details;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;


import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
