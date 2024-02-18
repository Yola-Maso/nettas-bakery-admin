package com.example.nettaadmin;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.core.view.View;

import java.util.List;

public class OrderedFoodAdapter extends RecyclerView.Adapter<OrderedFoodAdapter.OrderedFoodViewHolder> {

    private List<OrderedFoodItem> orderedFoodItems;

    public OrderedFoodAdapter(List<OrderedFoodItem> orderedFoodItems) {
        this.orderedFoodItems = orderedFoodItems;
    }

    @NonNull
    @Override
    public OrderedFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ordered_food, parent, false);
        return new OrderedFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderedFoodViewHolder holder, int position) {
        OrderedFoodItem orderedFoodItem = orderedFoodItems.get(position);
        holder.bind(orderedFoodItem);
    }

    @Override
    public int getItemCount() {
        return orderedFoodItems.size();
    }

    public static class OrderedFoodViewHolder extends RecyclerView.ViewHolder {

        private TextView foodNameTextView;
        private TextView quantityTextView;

        /*public OrderedFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
        }*/

        public OrderedFoodViewHolder(android.view.View view) {
            super(view);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
        }

        public void bind(OrderedFoodItem orderedFoodItem) {
            foodNameTextView.setText("Food Name: " + orderedFoodItem.getcName());
            quantityTextView.setText("Quantity: " + orderedFoodItem.getcQuantity());
        }
    }
}
