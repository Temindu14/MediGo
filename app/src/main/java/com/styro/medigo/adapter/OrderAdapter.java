package com.styro.medigo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.styro.medigo.R;
import com.styro.medigo.model.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderClickListener clickListener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList,
                        OnOrderClickListener clickListener) {
        this.context = context;
        this.orderList = orderList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Short order ID for display
        String shortId = order.getOrderId().substring(0, 8).toUpperCase();
        holder.tvOrderId.setText(context.getString(R.string.label_order_id, shortId));

        holder.tvOrderDate.setText(formatDate(order.getCreatedAt()));
        holder.tvOrderAmount.setText(context.getString(R.string.price_format, order.getTotalAmount()));
        holder.tvOrderStatus.setText(formatStatus(order.getStatus()));

        // Color code the status
        int statusColor = getStatusColor(order.getStatus());
        holder.tvOrderStatus.setTextColor(statusColor);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onOrderClick(order);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatStatus(String status) {
        switch (status) {
            case Order.STATUS_CONFIRMED:
                return context.getString(R.string.status_confirmed);
            case Order.STATUS_PROCESSING:
                return context.getString(R.string.status_processing);
            case Order.STATUS_OUT_FOR_DELIVERY:
                return context.getString(R.string.status_out_for_delivery);
            case Order.STATUS_DELIVERED:
                return context.getString(R.string.status_delivered);
            case Order.STATUS_CANCELLED:
                return context.getString(R.string.status_cancelled);
            default:
                return context.getString(R.string.status_pending);
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case Order.STATUS_DELIVERED:
                return context.getColor(R.color.green_success);
            case Order.STATUS_CANCELLED:
                return context.getColor(R.color.red_error);
            case Order.STATUS_OUT_FOR_DELIVERY:
            case Order.STATUS_PROCESSING:
                return context.getColor(R.color.orange_warning);
            default:
                return context.getColor(R.color.medigo_primary);
        }
    }


    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvOrderId;
        TextView tvOrderDate;
        TextView tvOrderAmount;
        TextView tvOrderStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
        }

    }

}