package com.example.elawalu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        getSupportActionBar().hide();






        // Find buttons in XML
        Button backButton = findViewById(R.id.backButton);
        Button addbutton = findViewById(R.id.addbutton);

        // Set Back Button Click Listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open only one activity (UserDetails)
                Intent intent = new Intent(Home.this, User_Details.class);
                startActivity(intent);
                // If you want to open Items instead, replace UserDetails with Items
            }
        });

        // Set Add Button Click Listener (Navigate to AddItemActivity)
        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Items.class);
                startActivity(intent);
            }
        });

        // Handle System Bar Insets

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
