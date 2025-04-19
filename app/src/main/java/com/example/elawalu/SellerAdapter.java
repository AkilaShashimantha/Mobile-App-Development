package com.example.elawalu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.SellerViewHolder> {

    private Context context;
    private List<Seller> sellerList;

    public SellerAdapter(Context context, List<Seller> sellerList) {
        this.context = context;
        this.sellerList = sellerList;
    }

    @NonNull
    @Override
    public SellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vegetable_seller_card, parent, false);
        return new SellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerViewHolder holder, int position) {
        Seller seller = sellerList.get(position);

        // Set seller name
        String name = "";
        if (seller.getFirstName() != null) name += seller.getFirstName();
        if (seller.getLastName() != null) name += " " + seller.getLastName();
        holder.sellerName.setText(name.trim().isEmpty() ? "Unknown Seller" : name.trim());

        // Set city - handle null or empty
        String city = seller.getCity();
        holder.city.setText(city == null || city.isEmpty() ? "City not specified" : city);

        // Set vegetables - handle null or empty
        List<String> vegetables = seller.getVegetables();
        if (vegetables == null || vegetables.isEmpty()) {
            holder.vegetables.setText("No vegetables listed");
        } else {
            String vegText = String.join(", ", vegetables);
            if (vegetables.size() > 3) {
                vegText = String.join(", ", vegetables.subList(0, 3)) + "...";
            }
            holder.vegetables.setText(vegText);
        }

        // Load profile image
        try {
            if (seller.getProfileImageUrl() != null && !seller.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(seller.getProfileImageUrl())
                        .placeholder(R.drawable.account_circle_btn)
                        .error(R.drawable.account_circle_btn)
                        .into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.account_circle_btn);
            }
        } catch (Exception e) {
            holder.profileImage.setImageResource(R.drawable.account_circle_btn);
        }
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    public static class SellerViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView sellerName, city, vegetables;

        public SellerViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.imageView);
            sellerName = itemView.findViewById(R.id.textView24);
            city = itemView.findViewById(R.id.textView29);
            vegetables = itemView.findViewById(R.id.textView35);
        }
    }
}