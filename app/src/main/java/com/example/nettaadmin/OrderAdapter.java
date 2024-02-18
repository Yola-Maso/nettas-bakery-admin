package com.example.nettaadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    private static OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onAcceptButtonClick(Order order);
        void onDoneButtonClick(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        private TextView orderKeyTextView;
        private TextView statusTextView;
        private TextView totalTextView;
        private TextView userPhoneTextView;
        private Button acceptButton;
        private Button doneButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderKeyTextView = itemView.findViewById(R.id.orderKeyTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            totalTextView = itemView.findViewById(R.id.totalPriceTextView);
            userPhoneTextView = itemView.findViewById(R.id.phoneNumberTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            doneButton = itemView.findViewById(R.id.doneButton);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onOrderClick(orderList.get(position));
                    }
                }
            });

            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAcceptButtonClick(orderList.get(position));
                    }
                }
            });

            doneButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDoneButtonClick(orderList.get(position));
                    }
                }
            });


        }

        public void bind(Order order) {
            orderKeyTextView.setText("Order : " + order.getOrderkey());
            statusTextView.setText("Status: " + order.getStatus());
            totalTextView.setText("Total: " + order.getTotalPrice());
            userPhoneTextView.setText("User Phone: " + order.getUserPhone());

            // Disable or hide buttons based on order status
            if ("Accepted".equals(order.getStatus())) {
                acceptButton.setEnabled(false);
                doneButton.setEnabled(true);
            } else if ("Done".equals(order.getStatus())) {
                acceptButton.setEnabled(false);
                doneButton.setEnabled(false);
            } else {
                acceptButton.setEnabled(true);
                doneButton.setEnabled(false);
            }

        }
    }
}
