package com.styro.medigo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.model.Outlet;
import com.styro.medigo.util.Constants;

public class ManageOutletsActivity extends AppCompatActivity {

    private RecyclerView rvOutlets;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;

    private DatabaseReference databaseReference;
    private List<Outlet> outletList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_outlets);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manageOutletsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        outletList = new ArrayList<>();

        rvOutlets = findViewById(R.id.rvOutlets);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);

        rvOutlets.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadOutlets);
        loadOutlets();
    }

    private void loadOutlets() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_OUTLETS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        outletList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Outlet outlet = dataSnapshot.getValue(Outlet.class);
                            if (outlet != null) outletList.add(outlet);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }
}