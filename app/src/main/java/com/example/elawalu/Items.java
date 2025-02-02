package com.example.elawalu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ViewFlipper;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class Items extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_items);



        Spinner vegetableSpinner = findViewById(R.id.vegetableSpinner);

        // Load vegetables from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.vegetable_list, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vegetableSpinner.setAdapter(adapter);

        Spinner locationSpinner = findViewById(R.id.locationspinner);

        // Load Locations from strings.xml
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.location_list, android.R.layout.simple_spinner_item);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter2);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().hide();

        ViewFlipper viewFlipper = findViewById(R.id.viewFlipper);
        viewFlipper.setFlipInterval(3000);
        viewFlipper.startFlipping();

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