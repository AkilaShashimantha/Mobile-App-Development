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
import java.util.List;
import java.util.Locale;
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

        // Initialize views
        initializeViews();

        // Set up RecyclerView
        setupRecyclerView();

        // Load cart items from Firebase
        loadCartItems();

        // Set up buttons
        setupCheckoutButton();
        setupRemoveAllButton();

        // Set up bottom navigation
        setupBottomNavigation();
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
        cartAdapter = new CartAdapter(cartItemList, () -> updateTotalPrice());
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
                    String email = snapshot.child("email").getValue(String.class);
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);

                    // Prepare payment request
                    InitRequest req = new InitRequest();
                    req.setMerchantId("1229451");
                    req.setCurrency("LKR");
                    req.setAmount(total[0]);
                    req.setOrderId(UUID.randomUUID().toString());
                    req.setItemsDescription("Cart Payment");

                    // Customer Details
                    req.getCustomer().setFirstName(firstName);
                    req.getCustomer().setLastName(lastName);
                    req.getCustomer().setEmail(email);
                    req.getCustomer().setPhone(phone);
                    req.getCustomer().getAddress().setAddress(address != null ? address : "");
                    req.getCustomer().getAddress().setCity(city != null ? city : "");
                    req.getCustomer().getAddress().setCountry("Sri Lanka");
                    req.setNotifyUrl("https://sandbox.payhere.lk/notify");

                    // Start PayHere payment
                    Intent intent = new Intent(View_Cart.this, PHMainActivity.class);
                    intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
                    startActivityForResult(intent, PAYHERE_REQUEST);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                if (response != null && response.isSuccess()) {
                    saveOrderToBuyingProducts();
                    clearCart();
                    Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                } else {
                    Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveOrderToBuyingProducts() {
        DatabaseReference buyingProductsRef = userRef.child("BuyingProducts");
        String orderId = buyingProductsRef.push().getKey();
        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());

        StringBuilder itemsDesc = new StringBuilder();
        double total = 0;
        for (CartItem item : cartItemList) {
            itemsDesc.append(item.getProductName())
                    .append(" (")
                    .append(item.getQuantity())
                    .append(" kg), ");
            total += item.getPrice() * item.getQuantity();
        }
        if (itemsDesc.length() > 2) {
            itemsDesc.setLength(itemsDesc.length() - 2);
        }

        Order order = new Order(
                itemsDesc.toString(),
                cartItemList.size() + " items",
                cartItemList.get(0).getLocation(),
                cartItemList.get(0).getSellerContact(),
                String.format(Locale.getDefault(), "%.2f", total),
                "1",
                currentDateTime
        );

        buyingProductsRef.child(orderId).setValue(order)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("View_Cart", "Failed to save order", task.getException());
                    }
                });
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

    // Model classes
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

    public static class Order {
        public String vegetable;
        public String quantity;
        public String location;
        public String contactNumber;
        public String price;
        public String activeStatus;
        public String paymentDateTime;

        public Order() {}

        public Order(String vegetable, String quantity, String location,
                     String contactNumber, String price, String activeStatus,
                     String paymentDateTime) {
            this.vegetable = vegetable;
            this.quantity = quantity;
            this.location = location;
            this.contactNumber = contactNumber;
            this.price = price;
            this.activeStatus = activeStatus;
            this.paymentDateTime = paymentDateTime;
        }
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
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
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
            int imageResId = getVegetableImageResource(item.getProductName());
            holder.productImage.setImageResource(imageResId);

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