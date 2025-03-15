package com.example.elawalu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SellerItemAdapter extends RecyclerView.Adapter<SellerItemAdapter.SellerItemViewHolder> {
    private List<SellerItem> itemList;

    public SellerItemAdapter(List<SellerItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public SellerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller_layout, parent, false);
        return new SellerItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerItemViewHolder holder, int position) {
        SellerItem item = itemList.get(position);
        holder.vegetableName.setText("Vegetable: " + item.getVegetable());
        holder.quantity.setText("Quantity: " + item.getQuantity());
        holder.price.setText("Price: Rs" + item.getPrice());
        holder.location.setText("Location: " + item.getLocation());
        holder.contactNumber.setText("Contact: " + item.getContactNumber());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class SellerItemViewHolder extends RecyclerView.ViewHolder {
        TextView vegetableName, quantity, price, location, contactNumber;

        public SellerItemViewHolder(@NonNull View itemView) {
            super(itemView);
            vegetableName = itemView.findViewById(R.id.vegetableName);
            quantity = itemView.findViewById(R.id.quantity);
            price = itemView.findViewById(R.id.price);
            location = itemView.findViewById(R.id.location);
            contactNumber = itemView.findViewById(R.id.contactNumber);
        }
    }
}