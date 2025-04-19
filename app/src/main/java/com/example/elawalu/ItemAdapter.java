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
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOAD_MORE = 1;

    private List<Item> itemList;
    private boolean showLoadMoreButton;
    private OnLoadMoreListener loadMoreListener;
    private OnItemClickListener itemClickListener;
    private List<Item> allItems;
    private boolean showPaymentDateTime;

    public ItemAdapter(List<Item> allItems, boolean showPaymentDateTime) {
        this.allItems = allItems;
        this.itemList = new ArrayList<>();
        this.showPaymentDateTime = showPaymentDateTime;
        loadInitialItems();
    }

    private void loadInitialItems() {
        int end = Math.min(6, allItems.size());
        itemList.addAll(allItems.subList(0, end));
        updateLoadMoreButton();
    }

    private void updateLoadMoreButton() {
        this.showLoadMoreButton = itemList.size() < allItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == itemList.size()) {
            return TYPE_LOAD_MORE;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOAD_MORE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_button, parent, false);
            return new LoadMoreViewHolder(view, loadMoreListener);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_layout, parent, false);
            return new ItemViewHolder(view, itemClickListener, showPaymentDateTime, itemList);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            Item item = itemList.get(position);
            ((ItemViewHolder) holder).bind(item);
        } else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size() + (showLoadMoreButton ? 1 : 0);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageVegetable;
        TextView textUserName, textVegetable, textQuantity, textLocation, textContactNumber, textPrice, paymentDateTimeTextView;
        private List<Item> itemList;
        private OnItemClickListener listener;

        public ItemViewHolder(@NonNull View itemView, OnItemClickListener listener, boolean showPaymentDateTime, List<Item> itemList) {
            super(itemView);
            this.itemList = itemList;
            this.listener = listener;

            imageVegetable = itemView.findViewById(R.id.imageVegetable);
            textUserName = itemView.findViewById(R.id.textUserName);
            textVegetable = itemView.findViewById(R.id.textVegetable);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textLocation = itemView.findViewById(R.id.textLocation);
            textContactNumber = itemView.findViewById(R.id.textContactNumber);
            textPrice = itemView.findViewById(R.id.textPrice);
            paymentDateTimeTextView = itemView.findViewById(R.id.paymentDateTimeTextView);

            paymentDateTimeTextView.setVisibility(showPaymentDateTime ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(itemList.get(position));
                }
            });
        }

        public void bind(Item item) {
            textUserName.setText("Seller : " + item.getUserName());
            textVegetable.setText("Vegetable : " + item.getVegetable());
            textQuantity.setText("Quantity : " + item.getQuantity());
            textLocation.setText("Location : " + item.getLocation());
            textContactNumber.setText("Contact : " + item.getContactNumber());
            textPrice.setText(item.getPrice());
            paymentDateTimeTextView.setText("Payment Create Date: " + item.getPaymentDateTime());
            imageVegetable.setImageResource(getVegetableImageResource(item.getVegetable()));
        }

        private int getVegetableImageResource(String vegetableName) {
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
    }

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        Button loadMoreButton;
        OnLoadMoreListener listener;

        public LoadMoreViewHolder(@NonNull View itemView, OnLoadMoreListener listener) {
            super(itemView);
            this.listener = listener;
            loadMoreButton = itemView.findViewById(R.id.loadMoreButton);
        }

        public void bind() {
            loadMoreButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLoadMore();
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.loadMoreListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void addItems(List<Item> newItems) {
        itemList.clear();
        itemList.addAll(newItems);
        updateLoadMoreButton();
        notifyDataSetChanged();
    }

    public void setShowLoadMoreButton(boolean showLoadMoreButton) {
        this.showLoadMoreButton = showLoadMoreButton;
        notifyDataSetChanged();
    }

    public Item getItem(int position) {
        return itemList.get(position);
    }
}