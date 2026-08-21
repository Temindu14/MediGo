package com.styro.medigo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.model.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;
    private final CartActionListener actionListener;

    public interface CartActionListener {
        void onQuantityChanged(int itemId, int newQuantity);
        void onItemRemoved(int itemId);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartActionListener actionListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvMedicineName.setText(item.getMedicineName());
        holder.tvUnitPrice.setText(context.getString(R.string.price_format, item.getPrice()));
        holder.tvTotalPrice.setText(context.getString(R.string.price_format, item.getTotalPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getMedicineImageUrl())
                .placeholder(R.drawable.placeholder_medicine)
                .error(R.drawable.placeholder_medicine)
                .override(150)
                .into(holder.ivMedicineImage);

        holder.btnIncrease.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onQuantityChanged(item.getId(), item.getQuantity() + 1);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onQuantityChanged(item.getId(), item.getQuantity() - 1);
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onItemRemoved(item.getId());
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedicineImage;
        TextView tvMedicineName, tvUnitPrice, tvQuantity, tvTotalPrice;
        ImageButton btnIncrease, btnDecrease, btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedicineImage = itemView.findViewById(R.id.ivMedicineImage);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}