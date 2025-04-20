package com.example.elawalu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import com.google.android.material.appbar.MaterialToolbar;

public class Payment_History extends AppCompatActivity {

    private ImageView paymentHistoryBackBtn;
    private RecyclerView recyclerView;
    private TextView userNameTextView;
    private ImageView userImage;
    private PaymentHistoryAdapter adapter;
    private List<PaymentHistoryItem> paymentHistoryList;

    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);
        getSupportActionBar().hide();

        // Initialize views
        recyclerView = findViewById(R.id.recycleView);
        userNameTextView = findViewById(R.id.userNameTextView);
        userImage = findViewById(R.id.userImage);

        // Setup RecyclerView
        paymentHistoryList = new ArrayList<>();
        adapter = new PaymentHistoryAdapter(paymentHistoryList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load data
        loadUserData();
        loadPaymentHistory();

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String profileImageUrl = sharedPreferences.getString("PROFILE_IMAGE", "");

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.account_circle_btn)
                    .error(R.drawable.account_circle_btn)
                    .into(userImage);
        }
        // Initialize the toolbar
        topAppBar = findViewById(R.id.topAppBar);

        // Set the navigation click listener
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button press
                onBackPressed();
            }
        });

    }


    @Override
    public void onBackPressed() {
        // Custom back navigation
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        // Or use default back behavior:
        // super.onBackPressed();
    }

    private void loadUserData() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users")
                .child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    userNameTextView.setText(firstName + " " + lastName);



                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void loadPaymentHistory() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference buyingProductsRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("BuyingProducts");

        buyingProductsRef.orderByChild("buyerId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        paymentHistoryList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PaymentHistoryItem item = new PaymentHistoryItem();

                            // Set all fields from database
                            item.setOrderId(snapshot.getKey()); // BuyingProducts ID as OrderId
                            item.setProductName(snapshot.child("vegetable").getValue(String.class)); // vegetable as productName
                            item.setQuantity(snapshot.child("quantity").getValue(String.class));
                            item.setPrice(snapshot.child("price").getValue(String.class));
                            item.setPaymentDateTime(snapshot.child("paymentDateTime").getValue(String.class));
                            item.setSellerName(snapshot.child("sellerName").getValue(String.class));
                            item.setSellerContact(snapshot.child("contactNumber").getValue(String.class)); // contactNumber as sellerContact
                            item.setLocation(snapshot.child("location").getValue(String.class));
                            item.setProductId(snapshot.child("productIds").getValue(String.class)); // productIds as productId
                            item.setSellerId(snapshot.child("sellerId").getValue(String.class));
                            item.setBuyerId(snapshot.child("buyerId").getValue(String.class));

                            paymentHistoryList.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    public static class PaymentHistoryItem {
        private String orderId;
        private String productName;
        private String quantity;
        private String price;
        private String paymentDateTime;
        private String sellerName;
        private String sellerContact;
        private String location;
        private String productId;
        private String sellerId;
        private String buyerId;

        public PaymentHistoryItem() {}

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getPaymentDateTime() { return paymentDateTime; }
        public void setPaymentDateTime(String paymentDateTime) { this.paymentDateTime = paymentDateTime; }
        public String getSellerName() { return sellerName; }
        public void setSellerName(String sellerName) { this.sellerName = sellerName; }
        public String getSellerContact() { return sellerContact; }
        public void setSellerContact(String sellerContact) { this.sellerContact = sellerContact; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getSellerId() { return sellerId; }
        public void setSellerId(String sellerId) { this.sellerId = sellerId; }
        public String getBuyerId() { return buyerId; }
        public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    }

    public static class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentHistoryViewHolder> {

        private List<PaymentHistoryItem> paymentHistoryList;

        public PaymentHistoryAdapter(List<PaymentHistoryItem> paymentHistoryList) {
            this.paymentHistoryList = paymentHistoryList;
        }

        @NonNull
        @Override
        public PaymentHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_history_card, parent, false);
            return new PaymentHistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PaymentHistoryViewHolder holder, int position) {
            PaymentHistoryItem item = paymentHistoryList.get(position);

            // Set all data to views
            holder.productTitle.setText("Order ID : " + (item.getOrderId() != null ? item.getOrderId() : "N/A"));
            holder.productId.setText("Product ID : " + (item.getProductId() != null ? item.getProductId() : "N/A"));
            holder.sellerId.setText("Seller ID : " + (item.getSellerId() != null ? item.getSellerId() : "N/A"));
            holder.sellerName.setText("Seller : " + (item.getSellerName() != null ? item.getSellerName() : "N/A"));
            holder.sellerContact.setText("Contact : " + (item.getSellerContact() != null ? item.getSellerContact() : "N/A"));
            holder.productnameQuantity.setText("Items : " + (item.getProductName() != null ? item.getProductName() : "N/A"));
            holder.totalPrice.setText("Total: Rs." + (item.getPrice() != null ? item.getPrice() : "0.00"));
            holder.locationTextView.setText("Location : " + (item.getLocation() != null ? item.getLocation() : "N/A"));
            holder.paymentDate.setText("Payment Date : " + (item.getPaymentDateTime() != null ? item.getPaymentDateTime() : "N/A"));
        }

        @Override
        public int getItemCount() {
            return paymentHistoryList.size();
        }

        public static class PaymentHistoryViewHolder extends RecyclerView.ViewHolder {
            TextView productTitle, productId, sellerId, sellerName, sellerContact,
                    productnameQuantity, totalPrice, locationTextView, paymentDate;

            public PaymentHistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                productTitle = itemView.findViewById(R.id.productTitle);
                productId = itemView.findViewById(R.id.productId);
                sellerId = itemView.findViewById(R.id.sellerId);
                sellerName = itemView.findViewById(R.id.sellerName);
                sellerContact = itemView.findViewById(R.id.sellerContact);
                productnameQuantity = itemView.findViewById(R.id.productnameQuantity);
                totalPrice = itemView.findViewById(R.id.totalPrice);
                locationTextView = itemView.findViewById(R.id.locationTextView);
                paymentDate = itemView.findViewById(R.id.paymentDate);
            }
        }
    }
}