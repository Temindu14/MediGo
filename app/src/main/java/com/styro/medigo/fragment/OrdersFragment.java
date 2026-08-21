package com.styro.medigo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.activity.OrderTrackingActivity;
import com.styro.medigo.adapter.OrderAdapter;
import com.styro.medigo.model.Order;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvOrders;
    private TextView tvEmpty;
    private View progressBar;

    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initFirebase();
        setupRecyclerView();
        loadOrders();
    }

    private void initViews(View view) {
        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(requireContext());
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(requireContext(), orderList, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);

        Query ordersQuery = databaseReference.child(Constants.NODE_ORDERS)
                .orderByChild("userId")
                .equalTo(sessionManager.getUserId());

        ordersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                orderList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) orderList.add(order);
                }

                orderAdapter.notifyDataSetChanged();
                tvEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(requireContext(), OrderTrackingActivity.class);
        intent.putExtra(Constants.KEY_ORDER_ID, order.getOrderId());
        startActivity(intent);
    }
}