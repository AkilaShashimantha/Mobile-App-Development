package com.example.elawalu;

import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SellerItemAdapter extends RecyclerView.Adapter<SellerItemAdapter.ViewHolder> {

    private static final String TAG = "SellerItemAdapter"; // For logging
    private List<SellerItem> itemList;
    private String userId;

    public SellerItemAdapter(List<SellerItem> itemList, String userId) {
        this.itemList = itemList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SellerItem item = itemList.get(position);

        // Log the item being bound
        Log.d(TAG, "Binding item: " + item.getItemId());

        // Set item details
        holder.vegetableName.setText("Vegetable: " + item.getVegetable());
        holder.quantity.setText("Quantity: " + item.getQuantity());
        holder.location.setText("Location: " + item.getLocation());
        holder.contactNumber.setText("Contact: " + item.getContactNumber());
        holder.price.setText("Price: Rs. " + item.getPrice() + "/kg");

        // Remove the listener before setting the Switch state
        holder.activateBtn.setOnCheckedChangeListener(null);

        int vegetableImageResId = getVegetableImageResource(item.getVegetable());
        holder.imageVegetable.setImageResource(vegetableImageResId);

        // Set the Switch state based on activeStatus
        if (item.getActiveStatus().equals("1")) {
            holder.activateBtn.setChecked(true);
            holder.activateBtn.setText("Activate");
            // Set thumb color to green
            holder.activateBtn.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.veggreen)));
        } else {
            holder.activateBtn.setChecked(false);
            holder.activateBtn.setText("Deactivate");
            // Set thumb color to grey
            holder.activateBtn.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.grey)));
        }

        // Reattach the listener after setting the state
        holder.activateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String newStatus = isChecked ? "1" : "0"; // "1" for active, "0" for deactive
                updateActiveStatus(item, newStatus, holder);
            }
        });

        // Delete button
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before deleting
                showDeleteConfirmationDialog(item, holder);
            }
        });
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
            // Add more cases for other vegetables
            default:
                return R.drawable.elavaluokkoma; // A default image if no match is found
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView vegetableName, quantity, location, contactNumber, price;
        Switch activateBtn;
        ImageButton deleteBtn;
        ImageView imageVegetable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vegetableName = itemView.findViewById(R.id.vegetableName);
            quantity = itemView.findViewById(R.id.quantity);
            location = itemView.findViewById(R.id.location);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            price = itemView.findViewById(R.id.price);
            activateBtn = itemView.findViewById(R.id.activatebtn);
            deleteBtn = itemView.findViewById(R.id.imageButton);
            imageVegetable = itemView.findViewById(R.id.imageVegetable);
        }
    }

    private void updateActiveStatus(SellerItem item, String newStatus, ViewHolder holder) {
        // Check if itemId is null
        if (item.getItemId() == null) {
            Log.e(TAG, "Item ID is null for item: " + item.getVegetable());
            Toast.makeText(holder.itemView.getContext(), "Error: Item ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Updating active status for item: " + item.getItemId() + " to " + newStatus);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference itemRef = database.getReference("Users")
                .child(userId)
                .child("Items")
                .child(item.getItemId()); // Use itemId

        itemRef.child("activeStatus").setValue(newStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update the item in the list
                        item.setActiveStatus(newStatus);
                        notifyItemChanged(holder.getAdapterPosition()); // Refresh the specific item in the RecyclerView

                        // Show a toast message using the context from the ViewHolder
                        Toast.makeText(holder.itemView.getContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to update status: " + task.getException());
                        Toast.makeText(holder.itemView.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmationDialog(SellerItem item, ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Delete Product");
        builder.setMessage("Are you sure you want to delete this product?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Proceed with deletion
            deleteItem(item, holder);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // Dismiss the dialog
            dialog.dismiss();
        });
        builder.show();
    }

    private void deleteItem(SellerItem item, ViewHolder holder) {
        // Check if itemId is null
        if (item.getItemId() == null) {
            Log.e(TAG, "Item ID is null for item: " + item.getVegetable());
            Toast.makeText(holder.itemView.getContext(), "Error: Item ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Deleting item: " + item.getItemId());

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference itemRef = database.getReference("Users")
                .child(userId)
                .child("Items")
                .child(item.getItemId()); // Use itemId

        itemRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove the item from the list
                        itemList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition()); // Refresh the RecyclerView

                        // Show a toast message using the context from the ViewHolder
                        Toast.makeText(holder.itemView.getContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to delete item: " + task.getException());
                        Toast.makeText(holder.itemView.getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}