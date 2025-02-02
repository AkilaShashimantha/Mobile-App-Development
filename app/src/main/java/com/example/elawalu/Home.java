package com.example.elawalu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        getSupportActionBar().hide();

ImageButton profile = findViewById(R.id.profileBtn);
profile.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(Home.this,User_Details.class);
        startActivity(intent);
    }
});

Button addproductBtn = findViewById(R.id.addProductBtn);
addproductBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Home.this,Items.class);
        startActivity(intent);
    }
});


//Card view clicked function

        CardView cardViewBtn = findViewById(R.id.cardViewBtn);
        cardViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, View_Product.class);
                startActivity(intent);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_home) {
                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), Search.class));
                finish();
                return true;
            } else if (id == R.id.bottom_Cart) {
                startActivity(new Intent(getApplicationContext(), View_Cart.class));
                finish();
                return true;
            }

            return false;
        });
    }
}
