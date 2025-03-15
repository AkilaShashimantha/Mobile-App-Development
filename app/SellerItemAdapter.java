package com.example.elawalu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

public class SellerItemAdapter extends RecyclerView.Adapter<SellerItemAdapter.SellerItemViewHolder> {

    private List<Item> itemList;
    private OnItemClickListener listener;

    public SellerItemAdapter(List<Item> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SellerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_dash_cart_layout, parent, false);
        return new SellerItemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class SellerItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textVegetable, textUserName, textQuantity, textLocation, textContactNumber, textPrice;
        private ImageView imageVegetable;
        private Chip activeChip;
        private ImageButton deleteButton;

        public SellerItemViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            textVegetable = itemView.findViewById(R.id.textVegetable);
            textUserName = itemView.findViewById(R.id.textUserName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textLocation = itemView.findViewById(R.id.textLocation);
            textContactNumber = itemView.findViewById(R.id.textContactNumber);
            textPrice = itemView.findViewById(R.id.textPrice);
            imageVegetable = itemView.findViewById(R.id.imageVegetable);
            activeChip = itemView.findViewById(R.id.active);
            deleteButton = itemView.findViewById(R.id.imageButton);

            // Set click listener for the delete button
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }

        public void bind(Item item) {
            textVegetable.setText("Vegetable: " + item.getVegetable());
            textUserName.setText("Seller: " + item.getUserName());
            textQuantity.setText("Quantity: " + item.getQuantity());
            textLocation.setText("Location: " + item.getLocation());
            textContactNumber.setText("Contact: " + item.getContactNumber());
            textPrice.setText("Price: Rs. " + item.getPrice() + " per kg");

            // Set vegetable image dynamically
            int vegetableImageResId = getVegetableImageResource(item.getVegetable());
            imageVegetable.setImageResource(vegetableImageResId);

            // Set active status
            if (item.getActiveStatus().equals("1")) {
                activeChip.setText("Active");
            } else {
                activeChip.setText("Inactive");
            }
        }

        private int getVegetableImageResource(String vegetableName) {
            switch (vegetableName.toLowerCase()) {
                case "tomatoes":
                    return R.drawable.thakkali;
                case "carrots":
                    return R.drawable.carrot;
                case "cabbage":
                    return R.drawable.gova;
                case "pumpkin":
                    return R.drawable.pumking;
                case "brinjols":
                    return R.drawable.brinjol;
                case "ladies fingers":
                    return R.drawable.ladies_fingers;
                case "onions":
                    return R.drawable.b_onion;
                case "potato":
                    return R.drawable.potato;
                case "beetroots":
                    return R.drawable.beetroot;
                case "leeks":
                    return R.drawable.leeks;
                default:
                    return R.drawable.elavaluokkoma; // Default image
            }
        }
    }

    // Interface for item click events
    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }
}