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


import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class User_Details extends AppCompatActivity {


    private BottomNavigationView bottomNavigationView;



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

        ImageView profileImage = findViewById(R.id.profileImage);
        TextView fname = findViewById(R.id.fName);
        TextView lname = findViewById(R.id.lName);
        TextView phoneNumber = findViewById(R.id.PhoneNumber);
        TextView userEmail = findViewById(R.id.email);
        TextView birthday = findViewById(R.id.profileBirthday);
        TextView nic = findViewById(R.id.nic);
        TextView address = findViewById(R.id.address);
        TextView city = findViewById(R.id.city);



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

        View mainLayout = findViewById(R.id.main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Detect Keyboard Open/Close and Hide/Show Bottom Navigation
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
                startActivity(new Intent(getApplicationContext(), Home.class));
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), Search.class));

                return true;
            } else if (id == R.id.bottom_Cart) {
                startActivity(new Intent(getApplicationContext(), View_Cart.class));

                return true;
            }

            return false;
        });

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




    }
}
