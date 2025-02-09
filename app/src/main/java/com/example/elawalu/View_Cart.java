package com.example.elawalu;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class View_Cart extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private List<CartItem> savedForLaterList;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private Button removeAllButton;
    private TextView emptyCartTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_cart);

        getSupportActionBar().hide();

        // Initialize views
        initializeViews();

        // Initialize lists
        cartItemList = new ArrayList<>();
        savedForLaterList = new ArrayList<>();

        // Set up RecyclerView
        setupRecyclerView();

        // Add sample data (replace with your actual cart data)
        addSampleCartItems();

        // Calculate and display initial total price
        updateTotalPrice();

        // Set up checkout button
        setupCheckoutButton();

        // Set up remove all button
        setupRemoveAllButton();

        // Handle keyboard visibility
        handleKeyboardVisibility();

        // Set up bottom navigation
        setupBottomNavigation();

        // Check if cart is empty initially
        checkIfCartIsEmpty();

        Button checkBtn = findViewById(R.id.checkoutButton);
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(View_Cart.this, Payment.class);
                startActivity(intent);
            }
        });
    }


    private void initializeViews() {
        View mainLayout = findViewById(R.id.main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        removeAllButton = findViewById(R.id.removeAllButton);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItemList, this::updateTotalPrice, this::saveForLater);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void addSampleCartItems() {
        cartItemList.add(new CartItem("Carrot", 250.00, R.drawable.carrot, 1));
        cartItemList.add(new CartItem("Product 2", 200.00, R.drawable.carrot, 2));
        cartItemList.add(new CartItem("Product 3", 300.00, R.drawable.carrot, 1));
        cartAdapter.notifyDataSetChanged();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceTextView.setText(String.format("Total: Rs%.2f", total));
    }

    private void setupCheckoutButton() {
        checkoutButton.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Proceeding to checkout", Toast.LENGTH_SHORT).show();
                // Add your checkout logic here
            }
        });
    }

    private void setupRemoveAllButton() {
        removeAllButton.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Your cart is already empty!", Toast.LENGTH_SHORT).show();
            } else {
                cartItemList.clear();
                cartAdapter.notifyDataSetChanged();
                updateTotalPrice();
                checkIfCartIsEmpty();
                Toast.makeText(this, "All items removed from cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleKeyboardVisibility() {
        View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            mainLayout.getWindowVisibleDisplayFrame(r);
            int screenHeight = mainLayout.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                // Keyboard is closed
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_Cart);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(getApplicationContext(), Search.class));
                return true;
            } else if (id == R.id.bottom_Cart) {
                return true;
            }

            return false;
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

    private void saveForLater(CartItem item, int position) {
        savedForLaterList.add(item);
        cartItemList.remove(position);
        cartAdapter.notifyItemRemoved(position);
        cartAdapter.notifyItemRangeChanged(position, cartItemList.size());
        updateTotalPrice();
        checkIfCartIsEmpty();
        Toast.makeText(this, "Item saved for later", Toast.LENGTH_SHORT).show();
    }

    // CartItem model class
    public static class CartItem {
        private String name;
        private double price;
        private int imageResId;
        private int quantity;

        public CartItem(String name, double price, int imageResId, int quantity) {
            this.name = name;
            this.price = price;
            this.imageResId = imageResId;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getImageResId() {
            return imageResId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // CartAdapter for RecyclerView
    public static class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
        private List<CartItem> cartItemList;
        private OnQuantityChangeListener quantityChangeListener;

        public CartAdapter(List<CartItem> cartItemList, OnQuantityChangeListener quantityChangeListener, OnSaveForLaterListener saveForLaterListener) {
            this.cartItemList = cartItemList;
            this.quantityChangeListener = quantityChangeListener;
        }




        @Override
        public CartViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CartViewHolder holder, int position) {
            CartItem item = cartItemList.get(position);
            holder.productImage.setImageResource(item.getImageResId());
            holder.productTitle.setText(item.getName());
            holder.productPrice.setText(String.format("Rs%.2f", item.getPrice()));
            holder.quantityTextView.setText(String.valueOf(item.getQuantity()));

            // Increase quantity
            holder.increaseButton.setOnClickListener(v -> {
                item.setQuantity(item.getQuantity() + 1);
                holder.quantityTextView.setText(String.valueOf(item.getQuantity()));
                quantityChangeListener.onQuantityChanged();
            });

            // Decrease quantity
            holder.decreaseButton.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    holder.quantityTextView.setText(String.valueOf(item.getQuantity()));
                    quantityChangeListener.onQuantityChanged();
                }
            });

            // Delete item
            holder.deleteButton.setOnClickListener(v -> {
                cartItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItemList.size());
                quantityChangeListener.onQuantityChanged();
                Toast.makeText(holder.itemView.getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
            });


        }

        @Override
        public int getItemCount() {
            return cartItemList.size();
        }

        public static class CartViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productTitle, productPrice, quantityTextView;
            Button increaseButton, decreaseButton;
            ImageButton deleteButton, saveForLaterButton;

            public CartViewHolder(View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productTitle = itemView.findViewById(R.id.productTitle);
                productPrice = itemView.findViewById(R.id.productPrice);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                increaseButton = itemView.findViewById(R.id.increaseButton);
                decreaseButton = itemView.findViewById(R.id.decreaseButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }

        public interface OnQuantityChangeListener {
            void onQuantityChanged();
        }

        public interface OnSaveForLaterListener {
            void onSaveForLater(CartItem item, int position);
        }
    }
}