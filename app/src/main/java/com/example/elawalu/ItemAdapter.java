package com.example.elawalu; // Replace with your package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    public List<Item> itemList; // Make this public for easy access

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        int vegetableImageResId = getVegetableImageResource(item.getVegetable());
        holder.imageVegetable.setImageResource(vegetableImageResId);

        holder.textUserName.setText("Seller : " + item.getUserName());
        holder.textVegetable.setText("Vegetable : " + item.getVegetable());
        holder.textQuantity.setText("Quantity : " + item.getQuantity());
        holder.textLocation.setText("Location : " + item.getLocation());
        holder.textContactNumber.setText("Contact : " + item.getContactNumber());
        holder.textPrice.setText("Price: Rs. " + item.getPrice() + " per kg");
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


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView imageVegetable;
        TextView textUserName, textVegetable, textQuantity, textLocation, textContactNumber, textPrice;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageVegetable = itemView.findViewById(R.id.imageVegetable);
            textUserName = itemView.findViewById(R.id.textUserName);
            textVegetable = itemView.findViewById(R.id.textVegetable);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textLocation = itemView.findViewById(R.id.textLocation);
            textContactNumber = itemView.findViewById(R.id.textContactNumber);
            textPrice = itemView.findViewById(R.id.textPrice);
        }
    }
}