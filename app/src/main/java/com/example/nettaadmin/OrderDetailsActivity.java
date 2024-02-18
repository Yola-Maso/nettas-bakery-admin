package com.example.nettaadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderedFoodAdapter orderedFoodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Retrieve the order from the intent
        recyclerView = findViewById(R.id.orderedFoodRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve the order key from the intent
        String orderKey = getIntent().getStringExtra("orderKey");

        // Now, you can use the orderKey to fetch the corresponding ordered food items from Firebase
        fetchOrderedFoodItems(orderKey);
    }

    private void fetchOrderedFoodItems(String orderKey) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Requests");

        ordersRef.child(orderKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Order order = dataSnapshot.getValue(Order.class);

                    // Now, you can access the ordered food items from order.getOrderedFoodItems()
                    List<OrderedFoodItem> orderedFoodItems = order.getOrderedFoodItems();

                    // Set up the RecyclerView with the OrderedFoodAdapter
                    orderedFoodAdapter = new OrderedFoodAdapter(orderedFoodItems);
                    recyclerView.setAdapter(orderedFoodAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    }


