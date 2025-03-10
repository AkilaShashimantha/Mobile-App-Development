package com.example.elawalu;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0; // Type for regular items
    private static final int TYPE_LOAD_MORE = 1; // Type for the "Load More" button

    private List<Item> itemList; // List of items currently displayed
    private boolean showLoadMoreButton; // Flag to show/hide the "Load More" button

    private OnLoadMoreListener loadMoreListener; // Listener for "Load More" button
    private OnItemClickListener itemClickListener; // Listener for item clicks

    private List<Item> allItems; // Store all items fetched from Firebase



    public ItemAdapter(List<Item> allItems) {
        this.allItems = allItems;
        this.itemList = new ArrayList<>(); // Initialize itemList as an empty list
        loadInitialItems(); // Load the first 5 items initially
    }

    // Method to load the first 5 items
    private void loadInitialItems() {
        int end = Math.min(5, allItems.size()); // Load up to 5 items
        itemList.addAll(allItems.subList(0, end)); // Add the first 5 items to itemList
        updateLoadMoreButton(); // Update the flag based on the remaining items

        // Log the items in itemList for debugging
        for (Item item : itemList) {
            Log.d("ItemAdapter", "Item: " + item.getVegetable() + ", Quantity: " + item.getQuantity());
        }
    }

    // Method to update the showLoadMoreButton flag
    private void updateLoadMoreButton() {
        this.showLoadMoreButton = itemList.size() < allItems.size(); // Show "Load More" if there are more items
    }

    @Override
    public int getItemViewType(int position) {
        if (position == itemList.size()) {
            return TYPE_LOAD_MORE; // "Load More" button
        }
        return TYPE_ITEM; // Regular item
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOAD_MORE) {
            // Inflate the "Load More" button layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_button, parent, false);
            return new LoadMoreViewHolder(view, loadMoreListener); // Pass the listener to the ViewHolder
        } else {
            // Inflate the regular item layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_layout, parent, false);
            return new ItemViewHolder(view, itemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            // Bind regular item data
            Item item = itemList.get(position);
            ((ItemViewHolder) holder).bind(item);
        } else if (holder instanceof LoadMoreViewHolder) {
            // Bind the "Load More" button
            ((LoadMoreViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        // Return the number of items + 1 if "Load More" button is visible
        return itemList.size() + (showLoadMoreButton ? 1 : 0);
    }

    // ViewHolder for regular items
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageVegetable;
        TextView textUserName, textVegetable, textQuantity, textLocation, textContactNumber, textPrice;

        public ItemViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imageVegetable = itemView.findViewById(R.id.imageVegetable);
            textUserName = itemView.findViewById(R.id.textUserName);
            textVegetable = itemView.findViewById(R.id.textVegetable);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textLocation = itemView.findViewById(R.id.textLocation);
            textContactNumber = itemView.findViewById(R.id.textContactNumber);
            textPrice = itemView.findViewById(R.id.textPrice);

            // Set click listener for the item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }

        public void bind(Item item) {
            // Bind item data to views
            textUserName.setText("Seller : " + item.getUserName());
            textVegetable.setText("Vegetable : " + item.getVegetable());
            textQuantity.setText("Quantity : " + item.getQuantity());
            textLocation.setText("Location : " + item.getLocation());
            textContactNumber.setText("Contact : " + item.getContactNumber());
            textPrice.setText("Price: Rs. " + item.getPrice() + " per kg");

            // Set vegetable image dynamically
            int vegetableImageResId = getVegetableImageResource(item.getVegetable());
            imageVegetable.setImageResource(vegetableImageResId);
        }

        private int getVegetableImageResource(String vegetableName) {
            switch (vegetableName.toLowerCase()) {
                case "tomato":
                    return R.drawable.thakkali;
                case "carrots":
                    return R.drawable.carrot;
                case "cabbage":
                    return R.drawable.gova;
                // Add more cases for other vegetables
                default:
                    return R.drawable.elavaluokkoma; // A default image if no match is found
            }
        }
    }

    // ViewHolder for the "Load More" button
    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        Button loadMoreButton;
        OnLoadMoreListener listener;

        public LoadMoreViewHolder(@NonNull View itemView, OnLoadMoreListener listener) {
            super(itemView);
            this.listener = listener; // Initialize the listener
            loadMoreButton = itemView.findViewById(R.id.loadMoreButton);
        }

        public void bind() {
            // Handle "Load More" button click
            loadMoreButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLoadMore(); // Notify the listener
                }
            });
        }
    }

    // Interface for "Load More" button click
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    // Interface for item clicks
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Set the "Load More" listener
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.loadMoreListener = listener;
    }

    // Set the item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    // Method to add items to the adapter
    public void addItems(List<Item> newItems) {
        itemList.clear(); // Clear the list before adding new items
        itemList.addAll(newItems); // Add new items to the list
        updateLoadMoreButton(); // Update the "Load More" button flag
        notifyDataSetChanged(); // Notify the adapter of data changes
    }

    // Method to set the "Load More" button visibility
    public void setShowLoadMoreButton(boolean showLoadMoreButton) {
        this.showLoadMoreButton = showLoadMoreButton;
        notifyDataSetChanged(); // Notify the adapter of data changes
    }

    // Method to get an item at a specific position
    public Item getItem(int position) {
        return itemList.get(position);
    }
}