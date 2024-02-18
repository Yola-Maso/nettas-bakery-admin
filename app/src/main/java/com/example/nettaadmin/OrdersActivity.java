package com.example.nettaadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.view.View;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Order> orderList;
    private OrderAdapter orderAdapter;
    private DatabaseReference ordersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);


        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    startActivity(new Intent(OrdersActivity.this, Home.class));
                    return true;

                case R.id.action_Orders:
                    startActivity(new Intent(OrdersActivity.this, OrdersActivity.class));
                    return true;
                // Handle other items similarly
                default:
                    return false;
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                Intent intent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);
                intent.putExtra("orderKey", order.getOrderkey());
                startActivity(intent);
            }

            @Override
            public void onAcceptButtonClick(Order order) {

                // Handle accept click
                if ("Accepted".equals(order.getStatus())) {
                    Toast.makeText(OrdersActivity.this, "Order already accepted", Toast.LENGTH_SHORT).show();
                } else {
                    // Update the order status to "Accepted" in the database
                    updateOrderStatus(order.getOrderkey(), "Accepted");
                }
            }

            @Override
            public void onDoneButtonClick(Order order) {
                // Handle done click
                if ("Accepted".equals(order.getStatus())) {
                    // Update the order status to "Done" in the database
                    updateOrderStatus(order.getOrderkey(), "Done");
                } else {
                    Toast.makeText(OrdersActivity.this, "Order must be accepted first", Toast.LENGTH_SHORT).show();
                }
            }
            });


        //orderAdapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(orderAdapter);

        // Assuming "Requests" is the node where orders are stored
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Requests");

        /*orderAdapter.setOnOrderClickListener(order -> {
            // Handle order click, e.g., open a new activity with order details

            Intent intent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);
            intent.putExtra("orderKey", order.getOrderkey());
            startActivity(intent);
        });*/

        fetchOrders();
    }

    private void fetchOrders() {
        // Get the current user's phone number

        ordersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            orderList.clear();
                            for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                                Order order = orderSnapshot.getValue(Order.class);
                                //String orderKey = orderSnapshot.getKey();
                                //order.setOrderkey(orderKey);
                                orderList.add(order);
                            }
                            orderAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });

    }
    private void updateOrderStatus(String orderKey, String newStatus) {
        // Update the order status in the database
        ordersRef.child(orderKey).child("status").setValue(newStatus);
    }

    }
