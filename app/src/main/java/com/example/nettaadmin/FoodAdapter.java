package com.example.nettaadmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private Context context;
    private ArrayList<Food> foodList;
    private OnItemClickListener listener;
    private DatabaseReference databaseReference;

    //private AlertDialog alertDialog;

    public FoodAdapter(Context context, ArrayList<Food> foodList, DatabaseReference databaseReference) {
        this.context = context;
        this.foodList = foodList;
        this.databaseReference =  databaseReference;
        //   this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String foodKey);
    }

    public void setOnItemClickListener(FoodAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_item, parent, false);
        return new FoodViewHolder(view, databaseReference);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        //holder.bind(food);
        holder.foodName.setText(food.getName());
        holder.foodPrice.setText(food.getPrice());
        Glide.with(context).load(food.getImage()).into(holder.foodImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    String foodKey = foodList.get(clickedPosition).getKey();
                    listener.onItemClick(clickedPosition, foodKey);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName;
        TextView foodPrice;
        private Button btnDeleteF;
        private Button btnUpdateF;

        private DatabaseReference databaseReference;

        public FoodViewHolder(@NonNull View itemView, DatabaseReference databaseReference) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            btnDeleteF = itemView.findViewById(R.id.btnDeleteFood);
            btnUpdateF = itemView.findViewById(R.id.btnUpdateFood);

            this.databaseReference = databaseReference;

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position, foodList.get(position).getKey());
                    }
                }
            });

            btnDeleteF.setOnClickListener(v -> {
                // Handle delete button click
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Food removedItem = foodList.get(position);

                        // Remove the item from the adapter
                        foodList.remove(position);
                        notifyItemRemoved(position);

                        // Remove the item from the Firebase Realtime Database
                        databaseReference.child(removedItem.getKey()).removeValue();
                    }
                }
                
                
        
            });
            btnUpdateF.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Food selectedFood = foodList.get(position);
                    String foodKey = selectedFood.getKey();
                    showUpdateCategoryDialog(foodKey, selectedFood.getName(), selectedFood.getImage(),
                            selectedFood.getIngredients(), selectedFood.getPrice(),
                            selectedFood.getPortion(), selectedFood.getDiscount());
                }
            });



        }

        private void showUpdateCategoryDialog(String foodKey, String name, String image,
                                              String ingredients, String price, String portion, String discount) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(itemView.getContext());
            alertDialogBuilder.setTitle("Update Food Item");

            View dialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_update_food, null);
            alertDialogBuilder.setView(dialogView);

            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etFoodName = dialogView.findViewById(R.id.etUpdateFoodName);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etIngredients = dialogView.findViewById(R.id.etUpdateIngredients);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etPrice = dialogView.findViewById(R.id.etUpdatePrice);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etPortion = dialogView.findViewById(R.id.etUpdatePortion);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etDiscount = dialogView.findViewById(R.id.etUpdateDiscount);

            etFoodName.setText(name);
            etIngredients.setText(ingredients);
            etPrice.setText(price);
            etPortion.setText(portion);
            etDiscount.setText(discount);

            alertDialogBuilder
                    .setPositiveButton("Update", (dialogInterface, i) -> {
                        // Get updated values
                        String updatedName = etFoodName.getText().toString().trim();
                        String updatedIngredients = etIngredients.getText().toString().trim();
                        String updatedPrice = etPrice.getText().toString().trim();
                        String updatedPortion = etPortion.getText().toString().trim();
                        String updatedDiscount = etDiscount.getText().toString().trim();

                        // Update the food item in the Firebase Realtime Database
                        updateFoodItem(foodKey, updatedName, updatedIngredients, updatedPrice, updatedPortion, updatedDiscount);
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        private void updateFoodItem(String foodKey, String updatedName, String updatedIngredients,
                                    String updatedPrice, String updatedPortion, String updatedDiscount) {
            // Update the food item in the Firebase Realtime Database
            DatabaseReference foodRef = databaseReference.child(foodKey);
            foodRef.child("name").setValue(updatedName);
            foodRef.child("ingredients").setValue(updatedIngredients);
            foodRef.child("price").setValue(updatedPrice);
            foodRef.child("portion").setValue(updatedPortion);
            foodRef.child("discount").setValue(updatedDiscount);

            // You can also update the image here if needed
            // Call the method to handle image upload and update image URL in the database
            // For example: uploadImage(foodKey);

            Toast.makeText(itemView.getContext(), "Food item updated successfully", Toast.LENGTH_SHORT).show();
        }

        public void bind(Food food) {

            btnDeleteF.setOnClickListener(v -> {

                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Food removedItem = foodList.get(position);

                        // Remove the item from the adapter
                        foodList.remove(position);
                        notifyItemRemoved(position);

                        // Remove the item from the Firebase Realtime Database
                        databaseReference.child(removedItem.getKey()).removeValue();
                    }
                }

            });

        }
    }
}
