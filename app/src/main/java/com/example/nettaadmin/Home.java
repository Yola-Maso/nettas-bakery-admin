package com.example.nettaadmin;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class Home extends AppCompatActivity {


    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private RecyclerView categoryRecyclerView;
    private ArrayList<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    private DatabaseReference databaseReference;
    private LayoutInflater inflater;


    private FloatingActionButton viewCart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    startActivity(new Intent(Home.this, Home.class));
                    return true;

                case R.id.action_Orders:
                    startActivity(new Intent(Home.this, OrdersActivity.class));
                    return true;
                // Handle other items similarly
                default:
                    return false;
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) FloatingActionButton addAdmin = findViewById(R.id.btnAddAdmin);

                addAdmin.setOnClickListener(view -> {

                        startActivity(new Intent(Home.this, Register.class));

    });

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        categoryRecyclerView.setLayoutManager(layoutManager);
        categoryList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Category");
        categoryAdapter = new CategoryAdapter(this, categoryList, databaseReference, getLayoutInflater());
        categoryRecyclerView.setAdapter(categoryAdapter);


        /*btnDelete = findViewById(R.id.btnDelete);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnViewFoods = findViewById(R.id.btnViewFoods);*/

        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Intent intent = new Intent(Home.this, OrdersActivity.class);
                //startActivity(intent);

            }
        });

        viewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               // Intent intent = new Intent(Home.this, CartActivity.class);
                //startActivity(intent);

            }
        });*/

        databaseReference = FirebaseDatabase.getInstance().getReference("Category");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    String categoryKey = snapshot.getKey(); // Get the category key
                    category.setKey(categoryKey); // Set the key in your Category class
                    categoryList.add(category);
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Home.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
       /* @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                // Now you have the URI of the selected image, pass it to the upload method
                uploadImageToStorage(categoryKey, updatedName, selectedImageUri);
            }
        }*/





        categoryAdapter.setOnItemClickListener((position, categoryID) -> {
            Category selectedCategory = categoryList.get(position);
            String categoryKey = selectedCategory.getKey();

            // Start FoodActivity and pass category key
            Intent intent = new Intent(Home.this, FoodActivity.class);
            intent.putExtra("categoryKey", categoryKey);
            startActivity(intent);
        });
    }

    public void showAddCategoryDialog(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Add New Category");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        alertDialogBuilder.setView(dialogView);

        EditText categoryNameEditText = dialogView.findViewById(R.id.editTextCategoryName);

        alertDialogBuilder
                .setPositiveButton("Add", (dialogInterface, i) -> {
                    String categoryName = categoryNameEditText.getText().toString().trim();

                    if (!categoryName.isEmpty()) {
                        // Handle category creation
                        Category newCategory = new Category(categoryName, "", ""); // You may need to generate a unique key
                        DatabaseReference newCategoryRef = databaseReference.push();
                        newCategoryRef.setValue(newCategory);

                        // Upload the image and save the access token in Realtime Database
                        uploadImage(newCategoryRef.getKey(), categoryName, selectedImageUri);
                    } else {
                        Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();

        // Set up the click listener for the Upload Image button
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);
        btnUploadImage.setOnClickListener(v -> {
            // Handle image upload logic here
            selectImage();
        });

        alertDialog.show();
    }

    public void uploadImage(String categoryKey, String categoryName, Uri imageUri) {
        // Upload the image to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("category_images/" + categoryKey);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload successful, get the access token (download URL)
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Save the access token (download URL) in Realtime Database
                        databaseReference.child(categoryKey).child("image").setValue(imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle failed image upload
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }


    public void selectImage() {
        // Implement the logic to allow the user to select an image from the device
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData(); // Remove the "Uri" type declaration
            // Now you have the URI of the selected image, pass it to the upload method
            // You may want to create a separate method for image upload and call it here
            // For example: uploadImageToStorage(categoryKey, updatedName, selectedImageUri);
        }
    }
}
