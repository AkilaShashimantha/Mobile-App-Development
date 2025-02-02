package com.example.elawalu;

import android.content.Intent;
import android.os.Bundle;

import com.example.elawalu.User_Details;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class User_Details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        getSupportActionBar().hide();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Deselect All Items
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
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
