package com.example.elawalu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class View_Cart extends AppCompatActivity {

    private static final int PAYHERE_REQUEST = 11001;
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private TextView totalPriceTextView;
    private Button checkoutButton, removeAllButton;
    private TextView emptyCartTextView;

    private DatabaseReference cartRef;
    private DatabaseReference userRef;
    private DatabaseReference buyingProductsRef;
    private String currentUserId;
    private ValueEventListener cartValueEventListener;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cart);
        getSupportActionBar().hide();

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app");
        cartRef = database.getReference("Carts").child(currentUserId);
        userRef = database.getReference("Users").child(currentUserId);
        buyingProductsRef = database.getReference("BuyingProducts");

        initializeViews();
        setupRecyclerView();
        loadCartItems();
        setupCheckoutButton();
        setupRemoveAllButton();
        setupBottomNavigation();

        // Initialize the toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        // Set the navigation click listener
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button press
                onBackPressed();
            }
        });

    }

    private void initializeViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        removeAllButton = findViewById(R.id.removeAllButton);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        cartItemList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItemList, this::updateTotalPrice);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        cartValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartItemList.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItem.setCartItemId(itemSnapshot.getKey());
                        cartItemList.add(cartItem);
                    }
                }
                cartAdapter.notifyDataSetChanged();
                updateTotalPrice();
                checkIfCartIsEmpty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Cart.this, "Failed to load cart: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        cartRef.addValueEventListener(cartValueEventListener);
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceTextView.setText(String.format(Locale.getDefault(), "Total: Rs%.2f", total));
    }

    private void setupCheckoutButton() {
        checkoutButton.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                initiatePayment();
            }
        });
    }

    private void initiatePayment() {
        final double[] total = {0};
        for (CartItem item : cartItemList) {
            total[0] += item.getPrice() * item.getQuantity();
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    startPayHerePayment(snapshot, total[0]);
                } else {
                    Toast.makeText(View_Cart.this, "User details not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Cart.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPayHerePayment(DataSnapshot userSnapshot, double total) {
        String email = userSnapshot.child("email").getValue(String.class);
        String firstName = userSnapshot.child("firstName").getValue(String.class);
        String lastName = userSnapshot.child("lastName").getValue(String.class);
        String phone = userSnapshot.child("phone").getValue(String.class);
        String address = userSnapshot.child("address").getValue(String.class);
        String city = userSnapshot.child("city").getValue(String.class);

        InitRequest req = new InitRequest();
        req.setMerchantId("1229451");
        req.setCurrency("LKR");
        req.setAmount(total);
        req.setOrderId(UUID.randomUUID().toString());
        req.setItemsDescription("Cart Payment");

        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(phone);
        req.getCustomer().getAddress().setAddress(address != null ? address : "");
        req.getCustomer().getAddress().setCity(city != null ? city : "");
        req.getCustomer().getAddress().setCountry("Sri Lanka");
        req.setNotifyUrl("https://sandbox.payhere.lk/notify");

        Intent intent = new Intent(View_Cart.this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK && response != null && response.isSuccess()) {
                saveOrderToDatabase();
                clearCart();
                Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveOrderToDatabase() {
        String orderId = buyingProductsRef.push().getKey();
        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());

        // Build product details
        StringBuilder itemsDesc = new StringBuilder();
        StringBuilder productIds = new StringBuilder();
        double totalAmount = 0;

        // Get the first item to get seller details (assuming all items are from same seller)
        CartItem firstItem = cartItemList.get(0);
        String sellerId = firstItem.getSellerId();

        for (CartItem item : cartItemList) {
            itemsDesc.append(item.getProductName()).append(" (").append(item.getQuantity()).append(" kg), ");
            productIds.append(item.getProductId()).append(",");
            totalAmount += item.getPrice() * item.getQuantity();
        }

        if (itemsDesc.length() > 2) itemsDesc.setLength(itemsDesc.length() - 2);
        if (productIds.length() > 0) productIds.setLength(productIds.length() - 1);

        // Format total as string with 2 decimal places
        String formattedTotal = String.format(Locale.getDefault(), "%.2f", totalAmount);

        // Create order data with seller information
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("activeStatus", "1");
        orderData.put("contactNumber", firstItem.getSellerContact());
        orderData.put("location", firstItem.getLocation());
        orderData.put("paymentDateTime", currentDateTime);
        orderData.put("price", formattedTotal);
        orderData.put("quantity", cartItemList.size() + " items");
        orderData.put("vegetable", itemsDesc.toString());
        orderData.put("productIds", productIds.toString());
        orderData.put("sellerName", firstItem.getSellerName());
        orderData.put("sellerId", sellerId);
        orderData.put("buyerId", currentUserId); // Store buyer's ID for reference

        // Save to BuyingProducts under buyer's account
        buyingProductsRef.child(orderId).setValue(orderData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save to SellingPayments under seller's account
                        saveSellerPaymentDetails(orderId, currentDateTime, itemsDesc.toString(),
                                formattedTotal, firstItem.getSellerName(),
                                sellerId, currentUserId);
                    } else {
                        Log.e("View_Cart", "Failed to save order", task.getException());
                    }
                });
    }

    private void saveSellerPaymentDetails(String orderId, String dateTime, String itemsDesc,
                                          String total, String sellerName, String sellerId,
                                          String buyerId) {
        // Get reference to seller's account
        DatabaseReference sellerRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(sellerId);

        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> sellerPayment = new HashMap<>();
                    // Seller's own information
                    sellerPayment.put("address", snapshot.child("address").getValue(String.class));
                    sellerPayment.put("birthday", snapshot.child("birthday").getValue(String.class));
                    sellerPayment.put("city", snapshot.child("city").getValue(String.class));
                    sellerPayment.put("email", snapshot.child("email").getValue(String.class));
                    sellerPayment.put("firstName", snapshot.child("firstName").getValue(String.class));
                    sellerPayment.put("gender", snapshot.child("gender").getValue(String.class));
                    sellerPayment.put("lastName", snapshot.child("lastName").getValue(String.class));
                    sellerPayment.put("nic", snapshot.child("nic").getValue(String.class));
                    sellerPayment.put("phone", snapshot.child("phone").getValue(String.class));
                    sellerPayment.put("profileImageUrl", snapshot.child("profileImageUrl").getValue(String.class));
                    sellerPayment.put("role", snapshot.child("role").getValue(String.class));

                    // Payment information
                    sellerPayment.put("activeStatus", "1");
                    sellerPayment.put("paymentDateTime", dateTime);
                    sellerPayment.put("vegetable", itemsDesc);
                    sellerPayment.put("price", total);
                    sellerPayment.put("sellerName", sellerName);
                    sellerPayment.put("sellerId", sellerId);
                    sellerPayment.put("buyerId", buyerId); // Who bought these products
                    sellerPayment.put("buyerContact", getBuyerContact()); // Optional: buyer's contact

                    // Save under seller's SellingPayments node
                    sellerRef.child("SellingPayments").child(orderId).setValue(sellerPayment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("View_Cart", "Failed to load seller details", error.toException());
            }
        });
    }

    // Helper method to get buyer's contact information
    private String getBuyerContact() {
        // Get the current user's phone number from Firebase
        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        // If phone number is not available (if user signed up with email), get it from database
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            // You could either:
            // 1. Return a placeholder (not recommended for production)
            // return "Not available";

            // 2. Or better, get it from the database (you'll need to ensure this is stored)
            // This assumes you have the phone number stored in the user's database record
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUserId);

            // This is a synchronous call which isn't ideal, but works for this case
            // In a real app, you might want to make this asynchronous
            try {
                DataSnapshot snapshot = Tasks.await(userRef.child("phone").get());
                if (snapshot.exists()) {
                    return snapshot.getValue(String.class);
                }
            } catch (Exception e) {
                Log.e("View_Cart", "Error getting user phone", e);
            }

            return ""; // Return empty if not found
        }

        return phoneNumber;
    }


    private void setupRemoveAllButton() {
        removeAllButton.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Your cart is already empty!", Toast.LENGTH_SHORT).show();
            } else {
                cartRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "All items removed from cart", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void checkIfCartIsEmpty() {
        if (cartItemList.isEmpty()) {
            emptyCartTextView.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(false);
            removeAllButton.setEnabled(false);
        } else {
            emptyCartTextView.setVisibility(View.GONE);
            checkoutButton.setEnabled(true);
            removeAllButton.setEnabled(true);
        }
    }

    private void clearCart() {
        cartRef.removeValue();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_Cart);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), Home.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), Search.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            } else if (id == R.id.bottom_Cart) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartValueEventListener != null) {
            cartRef.removeEventListener(cartValueEventListener);
        }
    }

    public static class CartItem {
        private String cartItemId;
        private String productId;
        private String productName;
        private double price;
        private int quantity;
        private String location;
        private String sellerName;
        private String sellerContact;
        private String sellerId;

        public CartItem() {}

        public String getCartItemId() { return cartItemId; }
        public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getSellerName() { return sellerName; }
        public void setSellerName(String sellerName) { this.sellerName = sellerName; }
        public String getSellerContact() { return sellerContact; }
        public void setSellerContact(String sellerContact) { this.sellerContact = sellerContact; }
        public String getSellerId() { return sellerId; }
        public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    }

    public static class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
        private List<CartItem> cartItemList;
        private OnQuantityChangeListener quantityChangeListener;

        public CartAdapter(List<CartItem> cartItemList, OnQuantityChangeListener quantityChangeListener) {
            this.cartItemList = cartItemList;
            this.quantityChangeListener = quantityChangeListener;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            CartItem item = cartItemList.get(position);

            if (holder.textWatcher != null) {
                holder.quantityEditText.removeTextChangedListener(holder.textWatcher);
            }

            holder.productTitle.setText(item.getProductName());
            holder.productPrice.setText(String.format(Locale.getDefault(), "Rs%.2f", item.getPrice()));
            holder.quantityEditText.setText(String.valueOf(item.getQuantity()));
            holder.productImage.setImageResource(getVegetableImageResource(item.getProductName()));

            holder.textWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        String quantityStr = s.toString();
                        if (!quantityStr.isEmpty()) {
                            int newQuantity = Integer.parseInt(quantityStr);
                            if (newQuantity > 0 && newQuantity != item.getQuantity()) {
                                updateCartItemQuantity(item.getCartItemId(), newQuantity);
                            }
                        }
                    } catch (NumberFormatException e) {
                        holder.quantityEditText.setText(String.valueOf(item.getQuantity()));
                    }
                }
            };
            holder.quantityEditText.addTextChangedListener(holder.textWatcher);

            holder.increaseButton.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                holder.quantityEditText.setText(String.valueOf(newQuantity));
            });

            holder.decreaseButton.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQuantity = item.getQuantity() - 1;
                    holder.quantityEditText.setText(String.valueOf(newQuantity));
                }
            });

            holder.deleteButton.setOnClickListener(v -> deleteCartItem(item.getCartItemId(), position));
        }

        private int getVegetableImageResource(String vegetableName) {
            if (vegetableName == null) return R.drawable.elavaluokkoma;
            switch (vegetableName.toLowerCase()) {
                case "tomatoes": return R.drawable.thakkali;
                case "carrots": return R.drawable.carrot;
                case "cabbage": return R.drawable.gova;
                case "pumpkin": return R.drawable.pumking;
                case "brinjols": return R.drawable.brinjol;
                case "ladies fingers": return R.drawable.ladies_fingers;
                case "onions": return R.drawable.b_onion;
                case "potato": return R.drawable.potato;
                case "beetroots": return R.drawable.beetroot;
                case "leeks": return R.drawable.leeks;
                default: return R.drawable.elavaluokkoma;
            }
        }

        private void updateCartItemQuantity(String cartItemId, int newQuantity) {
            DatabaseReference cartItemRef = FirebaseDatabase.getInstance()
                    .getReference("Carts")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(cartItemId)
                    .child("quantity");
            cartItemRef.setValue(newQuantity)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && quantityChangeListener != null) {
                            quantityChangeListener.onQuantityChanged();
                        }
                    });
        }

        private void deleteCartItem(String cartItemId, int position) {
            DatabaseReference cartItemRef = FirebaseDatabase.getInstance()
                    .getReference("Carts")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(cartItemId);
            cartItemRef.removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            cartItemList.remove(position);
                            notifyItemRemoved(position);
                            if (quantityChangeListener != null) {
                                quantityChangeListener.onQuantityChanged();
                            }
                        }
                    });
        }

        @Override
        public int getItemCount() { return cartItemList.size(); }

        public static class CartViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productTitle, productPrice;
            Button increaseButton, decreaseButton;
            ImageButton deleteButton;
            EditText quantityEditText;
            TextWatcher textWatcher;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productTitle = itemView.findViewById(R.id.productTitle);
                productPrice = itemView.findViewById(R.id.productPrice);
                quantityEditText = itemView.findViewById(R.id.quantityEditText);
                increaseButton = itemView.findViewById(R.id.increaseButton);
                decreaseButton = itemView.findViewById(R.id.decreaseButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }

        public interface OnQuantityChangeListener {
            void onQuantityChanged();
        }
    }
}