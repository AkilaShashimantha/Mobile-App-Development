package com.example.elawalu;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
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
import androidx.core.content.ContextCompat;
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

    String userId;
    String lastName, firstName, email,address,contactNumber,selectedLocation,selectedVegetable,quantity,price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_items);

        getSupportActionBar().hide();

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", "");
        String role = sharedPreferences.getString("ROLE", "");
         email = sharedPreferences.getString("EMAIL", "");
         firstName = sharedPreferences.getString("FIRST_NAME", "");
         lastName = sharedPreferences.getString("LAST_NAME", "");
        address = sharedPreferences.getString("ADDRESS", "");

        // Check if the user is a Buyer
        if ("Buyer".equals(role)) {
            showSellerRegistrationDialog(userId);
        } else {
            initializeUI();
        }
    }

    private void showSellerRegistrationDialog(String userId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_seller_registration);

        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
        Button btnCancel = dialog.findViewById(R.id.btnCancle);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        ImageView closeBtn = dialog.findViewById(R.id.closeBtn);

        // Set the image (assuming you have an image in your drawable folder)
        dialogImage.setImageResource(R.drawable.elawalu);


        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                navigateToHome();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                navigateToHome();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserRoleToSeller(userId);
                dialog.dismiss();

            }
        });

        // Handle dialog dismissal (e.g., back button pressed)
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                navigateToHome();
            }
        });

        dialog.show();
    }

    private void updateUserRoleToSeller(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId);

        userRef.child("role").setValue("Seller")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update role in SharedPreferences
                            updateRoleInSession("Seller");

                            // Show success message
                            Toast.makeText(Items.this, "You are now registered as a Seller", Toast.LENGTH_SHORT).show();

                            // Refresh the page
                            recreate();
                        } else {
                            // Show error message
                            Toast.makeText(Items.this, "Failed to update role", Toast.LENGTH_SHORT).show();

                            // Navigate to Home if the update fails
                            navigateToHome();
                        }
                    }
                });
    }

    // Helper method to update role in SharedPreferences
    private void updateRoleInSession(String newRole) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ROLE", newRole);
        editor.apply();
    }


    private void navigateToHome() {
        Intent intent = new Intent(Items.this, Home.class);
        // Clear the back stack and start a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeUI() {
        // Vegetable Flipper
        ViewFlipper viewFlipper = findViewById(R.id.viewFlipper);
        viewFlipper.setFlipInterval(3000);
        viewFlipper.startFlipping();

        // Vegetable Spinner
        Spinner vegetableSpinner = findViewById(R.id.vegetableSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.vegetable_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vegetableSpinner.setAdapter(adapter);

        // Location Spinner
        Spinner locationSpinner = findViewById(R.id.locationspinner);
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

        // Back Button
        ImageButton itemBackBtn = findViewById(R.id.itemBackBtn);
        itemBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
            }
        });

        // Ready Sale Button
        MaterialButton readySaleButton = findViewById(R.id.readyToSaleBtn);
        readySaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner vegetableSpinner = findViewById(R.id.vegetableSpinner);
                 selectedVegetable = vegetableSpinner.getSelectedItem().toString();

                TextInputEditText itemQuantity = findViewById(R.id.itemQuantity);
                TextInputLayout itemQuantityLayout = findViewById(R.id.itemQuantityLayout);
                 quantity = itemQuantityLayout.getEditText().getText().toString().trim();

                Spinner locationSpinner = findViewById(R.id.locationspinner);
                 selectedLocation = locationSpinner.getSelectedItem().toString();

                TextInputEditText itemContactNumber = findViewById(R.id.itemContactNumber);
                TextInputLayout itemContactNumberLayout = findViewById(R.id.itemContactNumberlayout);
                 contactNumber = itemContactNumberLayout.getEditText().getText().toString().trim();

                TextInputEditText itemPrice = findViewById(R.id.itemPrice);
                TextInputLayout itemPriceLayout = findViewById(R.id.itemPriceLayout);
                 price = itemPriceLayout.getEditText().getText().toString().trim();

                if (!isValidPhoneNumber(contactNumber)) {
                    itemContactNumberLayout.setError("Invalid phone number! Start with 0 and have 9 digits after that.");
                    return;
                }

                if (selectedVegetable.isEmpty() || quantity.isEmpty() || selectedLocation.isEmpty() || contactNumber.isEmpty() || price.isEmpty()) {
                    Toast.makeText(Items.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (address.equals("")) {
                    // Show the address input dialog
                    showAddressInputDialog();
                } else {
                    // Proceed to payment
                    showAdvertisementChargeDialog();
                    }
//
            }
        });
    }



    private void clearFormFields() {
        Spinner vegetableSpinner = findViewById(R.id.vegetableSpinner);
        TextInputEditText itemQuantity = findViewById(R.id.itemQuantity);
        Spinner locationSpinner = findViewById(R.id.locationspinner);
        TextInputEditText itemContactNumber = findViewById(R.id.itemContactNumber);
        TextInputEditText itemPrice = findViewById(R.id.itemPrice);

        vegetableSpinner.setSelection(0);
        locationSpinner.setSelection(0);
        itemQuantity.setText("");
        itemContactNumber.setText("");
        itemPrice.setText("");
    }

    private void navigatePayment(String userId,String email, String fName, String lName, String selectedVegetable,String quantity, String city,String contactNumber,String price,String address){

        String advertismentCharge = "1500.00";
        String paymentFrom = "Advertisment";

        Intent intent = new Intent(Items.this, Payment.class);

        intent.putExtra("userId", userId);
        intent.putExtra("email", email);
        intent.putExtra("fName", fName);
        intent.putExtra("lName", lName);
        intent.putExtra("selectedVegetable", selectedVegetable);
        intent.putExtra("quantity", quantity);
        intent.putExtra("contactNumber", contactNumber);
        intent.putExtra("city", city);
        intent.putExtra("price", price);
        intent.putExtra("address", address);
        intent.putExtra("advertismentCharge", advertismentCharge);
        intent.putExtra("paymentFromAdvertisment",paymentFrom);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();


    }

    private boolean isValidPhoneNumber(String phone) {
        // Regex for Sri Lankan phone number validation
        return phone.matches("^(0\\d{9}|\\+94\\d{9})$");
    }

    private void showAddressInputDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please Enter Your Address");

        // Create a TextInputLayout and TextInputEditText for the address
        TextInputLayout addressInputLayout = new TextInputLayout(this);

        TextInputEditText addressInputEditText = new TextInputEditText(this);

        addressInputLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.veggreen));
        addressInputLayout.setBoxStrokeWidth(2);

        // Add the TextInputEditText to the TextInputLayout
        addressInputLayout.addView(addressInputEditText);

        // Set the view of the dialog to the TextInputLayout
        builder.setView(addressInputLayout);

        // Add a "Confirm" button to the dialog
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String enteredAddress = addressInputEditText.getText().toString().trim();
            if (enteredAddress.isEmpty()) {
                Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show();
            } else {
                // Update the address and proceed to payment
                address = enteredAddress;
                showAdvertisementChargeDialog();

                }
        });

        // Add a "Cancel" button to the dialog
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAdvertisementChargeDialog() {
        // Create an AlertDialog builder for the Advertisement Charge dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Advertisement Charge")
                .setMessage("Rs. 1500.00 will be charged as an Advertisement payment. Is that OK with you?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed to payment
                    navigatePayment(userId, email, firstName, lastName, selectedVegetable, quantity, selectedLocation, contactNumber, price, address);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }


}