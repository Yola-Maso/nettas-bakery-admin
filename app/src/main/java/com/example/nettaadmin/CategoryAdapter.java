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
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private ArrayList<Category> categoryList;
    private OnItemClickListener listener;
    private DatabaseReference databaseReference;



    private AlertDialog alertDialog;

    private LayoutInflater inflater;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }


    public CategoryAdapter(Context context, ArrayList<Category> categoryList, DatabaseReference databaseReference, LayoutInflater inflater) {
        this.context = context;
        this.categoryList = categoryList;
        this.databaseReference = databaseReference;
        this.inflater =inflater;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String categoryID);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view, databaseReference, LayoutInflater.from(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category);
        holder.categoryName.setText(category.getName());
        Glide.with(context).load(category.getImage()).into(holder.categoryImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    setSelectedPosition(clickedPosition); // Set selected position on item click
                    String categoryKey = categoryList.get(clickedPosition).getKey();
                    listener.onItemClick(clickedPosition, categoryKey);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView categoryImage;
        private TextView categoryName;
        private Button btnDelete;
        private Button btnUpdate;
        private Button btnViewFoods;


        private DatabaseReference databaseReference;

        public CategoryViewHolder(@NonNull View itemView, DatabaseReference databaseReference, LayoutInflater inflater) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            //btnViewFoods = itemView.findViewById(R.id.btnViewFoods);
            this.databaseReference = databaseReference;

            //View viewItem = inflater.inflate(R.layout.dialog_update_category, null);


            // Set onClickListeners for the buttons
            btnDelete.setOnClickListener(v -> {
                // Handle delete button click
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Category removedItem = categoryList.get(position);

                        // Remove the item from the adapter
                        categoryList.remove(position);
                        notifyItemRemoved(position);

                        // Remove the item from the Firebase Realtime Database
                        databaseReference.child(removedItem.getKey()).removeValue();
                }
            }

                btnUpdate.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Category selectedCategory = categoryList.get(position);
                        String categoryKey = selectedCategory.getKey();
                        showUpdateCategoryDialog(categoryKey, selectedCategory.getName(), selectedCategory.getImage());
                    }
                });

            /*btnViewFoods.setOnClickListener(view -> {
                // Handle view foods button click
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Category selectedCategory = categoryList.get(selectedPosition);
                        String categoryKey = selectedCategory.getKey();

                        // Start FoodActivity and pass category key
                        Intent intent = new Intent(Home.this, FoodActivity.class);
                        intent.putExtra("categoryKey", categoryKey);
                        startActivity(intent);
                    }
                }
            });*/

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position, categoryList.get(position).getKey());
                    }
                }
            });
        });

    }


        public void bind(Category category) {
            btnUpdate.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Category selectedCategory = categoryList.get(position);
                    String categoryKey = selectedCategory.getKey();
                    showUpdateCategoryDialog(categoryKey, selectedCategory.getName(), selectedCategory.getImage());
                }
            });
        }


            private void showUpdateCategoryDialog(String categoryKey, String name, String imageUrl) {
                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle("Update Category");

                // Inflate the custom layout for the dialog
                View view = inflater.inflate(R.layout.dialog_update_category, null);
                builder.setView(view);

                // Get references to the EditText and Button in the dialog layout
                EditText categoryNameEditText = view.findViewById(R.id.editTextCategoryName);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button updateButton = view.findViewById(R.id.btnUpdateCategory);


                // Set the current category name in the EditText
                categoryNameEditText.setText(name);

                // Set the click listener for the update button
                updateButton.setOnClickListener(v -> {
                    // Retrieve the updated category name
                    String updatedName = categoryNameEditText.getText().toString().trim();

                    // Validate if the updated name is not empty
                    if (!updatedName.isEmpty()) {
                        // Update the category in the database
                        updateCategory(categoryKey, updatedName, imageUrl);


                        // Dismiss the dialog
                        alertDialog.dismiss();
                    } else {
                        // Show an error message if the updated name is empty
                        // Show an error message if the updated name is empty
                        Toast.makeText(itemView.getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
                    }
                });


                // Create and show the AlertDialog
                alertDialog = builder.create();
                alertDialog.show();
            }



        /*private LayoutInflater getLayoutInflater() {
            return null;
        }*/

        private void updateCategory(String categoryKey, String updatedName, String imageUrl) {
            DatabaseReference categoryRef = databaseReference.child(categoryKey);

            // Update the category name in the database
            categoryRef.child("name").setValue(updatedName);


            // You can add additional fields to update if needed, for example:
            // categoryRef.child("imageUrl").setValue(newImageUrl);
        }



    }}


