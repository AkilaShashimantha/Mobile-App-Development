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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Hide the ActionBar for a cleaner UI (Optional)
        getSupportActionBar().hide();

        // Initialize views
        paymentBtn = findViewById(R.id.paymentBtn);
        paymentStatus = findViewById(R.id.paymentStatus);

        // Set up the OnClickListener for the payment button
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePayment();
            }
        });

        // Handle window insets for better layout adjustment
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initiatePayment() {
        // Set the PayHere base URL for sandbox environment
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

        // Prepare the payment request
        InitRequest req = new InitRequest();
        req.setMerchantId("1229451");  // Replace with your PayHere Merchant ID
        req.setCurrency("LKR");  // Currency (LKR/USD/GBP/EUR)
        req.setAmount(1000.00);   // Payment amount
        req.setOrderId(UUID.randomUUID().toString());  // Unique Order ID
        req.setItemsDescription("Test Item Purchase");  // Item description
        req.setCustom1("Custom Message 1");
        req.setCustom2("Custom Message 2");

        // Customer Details
        req.getCustomer().setFirstName("John");
        req.getCustomer().setLastName("Doe");
        req.getCustomer().setEmail("johndoe@gmail.com");
        req.getCustomer().setPhone("+94771234567");
        req.getCustomer().getAddress().setAddress("123, Sample Street");
        req.getCustomer().getAddress().setCity("Colombo");
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
                        startActivity(intent);
                        finish();

                    } else {
                        Log.e("PAYHERE", "paymentStatus is null!");

                        Intent intent = new Intent(Payment.this,Home.class);
                        startActivity(intent);
                        finish();

                    }

                    // Show success toast
                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show();
                } else {
                    // Payment failed
                    if (paymentStatus != null) {
                        paymentStatus.setText("Payment Failed: " );
                        paymentStatus.setTextColor(Color.RED);
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(Payment.this,Home.class);
                        startActivity(intent);
                        finish();

                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Payment was canceled by the user
                if (paymentStatus != null) {
                    paymentStatus.setText("Payment Cancelled by User");

                    Intent intent = new Intent(Payment.this,Home.class);
                    startActivity(intent);
                    finish();

                }
            }
        }
    }
}
