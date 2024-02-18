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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FoodActivity extends AppCompatActivity {


    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private RecyclerView foodRecyclerView;
    private ArrayList<Food> foodList;
    private FoodAdapter foodAdapter;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);


        String categoryKey = getIntent().getStringExtra("categoryKey");

        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Foods");
        foodAdapter = new FoodAdapter(this, foodList, databaseReference);
        foodRecyclerView.setAdapter(foodAdapter);

        //foodAdapter = new FoodAdapter(this, foodList, new FoodAdapter.OnItemClickListener() {
        // @Override
        //     public void onItemClick(int position, String foodKey) {
        // Handle item click here, e.g., start FoodDetail activity with foodKey
        //        String foodKey = foodList.get(position).getKey(); // Get the food key
        //       Intent intent = new Intent(FoodActivity.this, FoodDetail.class);
        //       intent.putExtra("foodKey", foodKey); // Pass the food key
        //       startActivity(intent);
        //     }
        //  });


        foodRecyclerView.setAdapter(foodAdapter);


        if (categoryKey != null && !categoryKey.isEmpty()) {
            // Use categoryKey to filter foods that match the MenuID
            databaseReference = FirebaseDatabase.getInstance().getReference("Foods");

            Query query = databaseReference.orderByChild("menuID").equalTo(categoryKey);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    foodList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Food food = snapshot.getValue(Food.class);
                        String foodKey = snapshot.getKey(); // Get the category key
                        food.setKey(foodKey);
                        foodList.add(food);
                    }
                    foodAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(FoodActivity.this, "Failed to load foods", Toast.LENGTH_SHORT).show();
                }
            });

            foodAdapter.setOnItemClickListener((position, foodkey) -> {
                Food selectedFFood = foodList.get(position);
                String foodKey = selectedFFood.getKey();

                // Start FoodActivity and pass category key
                /*Intent intent = new Intent(FoodActivity.this, FoodDetail.class);
                intent.putExtra("foodKey", foodKey);
                startActivity(intent);*/
            });

        } else {
            // Handle the case when categoryKey is null (e.g., display an error message)
            Toast.makeText(FoodActivity.this, "Failed to load foods", Toast.LENGTH_LONG).show();
        }

    }
        public void showAddFoodDialog(View view) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Add New Food");

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_food, null);
            alertDialogBuilder.setView(dialogView);
            String categoryKey = getIntent().getStringExtra("categoryKey");

            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etFoodName = dialogView.findViewById(R.id.etFoodName);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etIngredients = dialogView.findViewById(R.id.etIngredients);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etPrice = dialogView.findViewById(R.id.etPrice);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etPortion = dialogView.findViewById(R.id.etPortion);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText etDiscount = dialogView.findViewById(R.id.etDiscount);

            alertDialogBuilder
                    .setPositiveButton("Add", (dialogInterface, i) -> {
                        String FoodName = etFoodName.getText().toString().trim();
                        String Ingredients = etIngredients.getText().toString().trim();
                        String Price = etPrice.getText().toString().trim();
                        String Portion = etPortion.getText().toString().trim();
                        String Discount = etDiscount.getText().toString().trim();

                        if (!FoodName.isEmpty() && !Ingredients.isEmpty() && !Price.isEmpty() && !Portion.isEmpty() && !Discount.isEmpty()) {
                            // Handle Food item creation
                            Food newFood = new Food("", FoodName, "", Ingredients, Price, Portion, Discount, categoryKey);
                            DatabaseReference newFoodRef = databaseReference.push();
                            newFoodRef.setValue(newFood);


                            // You can also upload the image using btnUploadImage click listener
                            uploadImage(newFoodRef.getKey());
                        } else {
                            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

            AlertDialog alertDialog = alertDialogBuilder.create();

            // Set up the click listener for the Upload Image button
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);
            btnUploadImage.setOnClickListener(v -> {
                // Handle image upload logic here
                selectImage();
            });

            alertDialog.show();
        }

    public void uploadImage(String foodKey) {
        if (selectedImageUri != null) {
            // Upload the image to Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("food_images/" + foodKey);

            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image upload successful, get the access token (download URL)
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Save the access token (download URL) in Realtime Database
                            databaseReference.child(foodKey).child("image").setValue(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failed image upload
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle the case when selectedImageUri is null
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
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


