package com.example.elawalu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class Payment extends AppCompatActivity {

    private static final int PAYHERE_REQUEST = 11001;
    private Button paymentBtn;
    private TextView paymentStatus;

    String userId;
    String lastName, firstName, email,address,contactNumber,selectedLocation,selectedVegetable,quantity,price,advertismentCharge;
    String payment,paymentFromAdvertisment,paymentFrom;


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Hide the ActionBar for a cleaner UI (Optional)
        getSupportActionBar().hide();


        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        email = intent.getStringExtra("email");
        firstName = intent.getStringExtra("fName");
        lastName = intent.getStringExtra("lName");
        selectedVegetable = intent.getStringExtra("selectedVegetable");
        quantity = intent.getStringExtra("quantity");
        selectedLocation = intent.getStringExtra("city");
        contactNumber = intent.getStringExtra("contactNumber");
        price = intent.getStringExtra("price");
        address = intent.getStringExtra("address");
        advertismentCharge = intent.getStringExtra("advertismentCharge");
        paymentFromAdvertisment = intent.getStringExtra("paymentFromAdvertisment");

        payment = advertismentCharge;

       paymentFrom = paymentFromAdvertisment;

        // Initialize views
        paymentBtn = findViewById(R.id.paymentBtn);
        paymentStatus = findViewById(R.id.paymentStatus);

        // Set up the OnClickListener for the payment button
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePayment(email,firstName,lastName,selectedLocation,contactNumber,address,payment,paymentFrom);
            }
        });

        // Handle window insets for better layout adjustment
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initiatePayment(String email, String firstName,String lastName, String city,String contactNumber, String address,String payment,String paymentFrom) {
        // Set the PayHere base URL for sandbox environment
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

        // Prepare the payment request
        InitRequest req = new InitRequest();
        req.setMerchantId("1229451");  // Replace with your PayHere Merchant ID
        req.setCurrency("LKR");  // Currency (LKR/USD/GBP/EUR)
        req.setAmount(Double.parseDouble(payment)); // Payment amount
        req.setOrderId(UUID.randomUUID().toString());  // Unique Order ID
        req.setItemsDescription(paymentFrom);  // Item description


        // Customer Details
        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(contactNumber);
        req.getCustomer().getAddress().setAddress(address);
        req.getCustomer().getAddress().setCity(city);
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Backend URL to receive payment notifications
        req.setNotifyUrl("https://sandbox.payhere.lk/notify");

        // Create an Intent to start the PayHere payment activity
        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            // Retrieve the response from the PayHere payment activity
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK) {
                // Payment was successful
                if (response != null && response.isSuccess()) {
                    String message = "Payment Successful: " ;
                    Log.d("PAYHERE", message);

                    // Update the payment status UI
                    if (paymentStatus != null) {
                        paymentStatus.setText(message);

                        Intent intent = new Intent(Payment.this,Home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Log.e("PAYHERE", "paymentStatus is null!");

                        Intent intent = new Intent(Payment.this,Home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }

                    // Show success toast
                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show();

                    saveItemToFirebase(userId, selectedVegetable, quantity, selectedLocation, contactNumber, price);

                } else {
                    // Payment failed
                    if (paymentStatus != null) {
                        paymentStatus.setText("Payment Failed: " );
                        paymentStatus.setTextColor(Color.RED);
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(Payment.this,Home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Payment was canceled by the user
                if (paymentStatus != null) {
                    paymentStatus.setText("Payment Cancelled by User");

                    Intent intent = new Intent(Payment.this,Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }
            }
        }
    }

    private void saveItemToFirebase(String userId, String vegetable, String quantity, String location, String contactNumber, String price) {
        // Reference to the user's Items node
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("Items");

        // Reference to the user's SellerPayments node
        DatabaseReference sellerPaymentRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("SellingPayments");

        // Generate a unique key for the item
        String itemId = userRef.push().getKey();

        // Create the Item object with activeStatus set to "1"
        Items.Item item = new Items.Item(vegetable, quantity, location, contactNumber, price);

        // Save the item under the user's Items node
        userRef.child(itemId).setValue(item)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Payment.this, "Item saved successfully", Toast.LENGTH_SHORT).show();

                            // Save a copy of the item under the user's SellerPayments node
                            sellerPaymentRef.child(itemId).setValue(item)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Payment.this, "SellerPayment saved successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Payment.this, "Failed to save SellerPayment", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(Payment.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                            navigateToHome();
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
        public String activeStatus; // Add activeStatus field

        // Default constructor required for Firebase
        public Item() {}

        // Constructor with five parameters (without activeStatus)
        public Item(String vegetable, String quantity, String location, String contactNumber, String price) {
            this.vegetable = vegetable;
            this.quantity = quantity;
            this.location = location;
            this.contactNumber = contactNumber;
            this.price = price;
            this.activeStatus = "1"; // Set activeStatus to "1" by default
        }

        // New constructor with six parameters (including activeStatus)
        public Item(String vegetable, String quantity, String location, String contactNumber, String price, String activeStatus) {
            this.vegetable = vegetable;
            this.quantity = quantity;
            this.location = location;
            this.contactNumber = contactNumber;
            this.price = price;
            this.activeStatus = activeStatus; // Initialize activeStatus
        }
    }
    private void navigateToHome() {
        Intent intent = new Intent(Payment.this, Home.class);
        // Clear the back stack and start a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
