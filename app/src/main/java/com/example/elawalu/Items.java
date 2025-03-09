package com.example.elawalu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Items extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_items);


        getSupportActionBar().hide();


        // Vegetable Flipper
        ViewFlipper viewFlipper = findViewById(R.id.viewFlipper);
        viewFlipper.setFlipInterval(3000);
        viewFlipper.startFlipping();

        //Vegetable Spinner
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


        // Quantity
        TextInputEditText itemQuantity = findViewById(R.id.itemQuantity);

        itemQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null && editable.length() > 0) {
                    if (!editable.toString().endsWith("kg")) {
                        itemQuantity.removeTextChangedListener(this);
                        itemQuantity.setText(editable.toString() + " kg");
                        itemQuantity.setSelection(itemQuantity.getText().length() - 3);  // Position cursor before "kg"
                        itemQuantity.addTextChangedListener(this);
                    }
                }
            }
        });


        ImageButton itemBackBtn = findViewById(R.id.itemBackBtn);
        itemBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Items.this, Home.class);
                startActivity(intent);
                finish();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", "");

        MaterialButton readySaleButton = findViewById(R.id.readySale);
        readySaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Spinner vegetableSpinner = findViewById(R.id.vegetableSpinner);
                String selectedVegetable = vegetableSpinner.getSelectedItem().toString();

                TextInputEditText itemQuantity = findViewById(R.id.itemQuantity);
                TextInputLayout itemQuantityLayout = findViewById(R.id.itemQuantityLayout);
                String quantity = itemQuantityLayout.getEditText().getText().toString().trim();

                Spinner locationSpinner = findViewById(R.id.locationspinner);
                String selectedLocation = locationSpinner.getSelectedItem().toString();

                TextInputEditText itemContactNumber = findViewById(R.id.itemContactNumber);
                TextInputLayout itemContactNumberLayout = findViewById(R.id.itemContactNumberlayout);
                String contactNumber = itemContactNumberLayout.getEditText().getText().toString().trim();

                TextInputEditText itemPrice = findViewById(R.id.itemPrice);
                TextInputLayout itemPriceLayout = findViewById(R.id.itemPriceLayout);
                String price = itemPriceLayout.getEditText().getText().toString().trim();

                if (selectedVegetable.isEmpty() || quantity.isEmpty() || selectedLocation.isEmpty() || contactNumber.isEmpty() || price.isEmpty()) {
                    Toast.makeText(Items.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;

                }

                saveItemToFirebase(userId, selectedVegetable, quantity, selectedLocation, contactNumber, price);

            }
        });

    }

    private void saveItemToFirebase(String userId, String vegetable, String quantity, String location, String contactNumber, String price) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Items"); // Create a new node "Items" under the user

        // Generate a unique key for each item
        String itemId = userRef.push().getKey();

        // Create an Item object
        Item item = new Item(vegetable, quantity, location, contactNumber, price);

        // Save the item to Firebase
        userRef.child(itemId).setValue(item)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Items.this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                            clearFormFields();
                        } else {
                            Toast.makeText(Items.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static class Item {
        public String vegetable;
        public String quantity;
        public String location;
        public String contactNumber;
        public String price;

        public Item() {}

        public Item(String vegetable, String quantity, String location, String contactNumber, String price) {
            this.vegetable = vegetable;
            this.quantity = quantity;
            this.location = location;
            this.contactNumber = contactNumber;
            this.price = price;
        }
    }

    private void clearFormFields() {
        Spinner vegetableSpinner = findViewById(R.id.vegetableSpinner);
        TextInputEditText itemQuantity = findViewById(R.id.itemQuantity);
        Spinner locationSpinner = findViewById(R.id.locationspinner);
        TextInputEditText itemContactNumber = findViewById(R.id.itemContactNumber);
        TextInputEditText itemPrice = findViewById(R.id.itemPrice);

        // Clear the Spinner selection (set to the first item or a default value)
        vegetableSpinner.setSelection(0);
        locationSpinner.setSelection(0);

        // Clear the TextInputEditText fields
        itemQuantity.setText("");
        itemContactNumber.setText("");
        itemPrice.setText("");
    }

}