package com.example.jasonvehicletrading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailsActivity extends AppCompatActivity {
    CustomProgressDialog progressDialog;
    Button btnCancel,btnUpdate,btnDelete;
    ImageView imageViewUnitPicture;
    EditText editTextChassisNumber, editTextYearModel, editTextPrice, editTextContainerNumber, editTextSpecs, editTextUpdatedPrice;
    String vehicleReference;
    Vehicle vehicle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        progressDialog = new CustomProgressDialog(this);

        btnCancel = findViewById(R.id.btn_cancel);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vehicle != null){
                    Intent intent = new Intent(DetailsActivity.this, UpdateVechicleActivity.class);
                    intent.putExtra("vehicle_reference",vehicle.getId());
                    startActivity(intent);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DetailsActivity.this)
                        .setTitle("Deleting Unit")
                        .setMessage("Are you sure you want to delete this unit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                progressDialog.show();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                vehicle.setDeleted(true);
                                db.collection("vehicles")
                                        .document(vehicleReference)
                                        .set(vehicle)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                if(progressDialog.isShowing()) progressDialog.hide();
                                                Toast.makeText(DetailsActivity.this, "Successfully deleted!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                if(progressDialog.isShowing()) progressDialog.hide();
                                                Toast.makeText(DetailsActivity.this, "Something went wrong, please try again later!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
            }
        });

        editTextChassisNumber = findViewById(R.id.editText_chassis_number);
        editTextUpdatedPrice = findViewById(R.id.editText_updated_price);
        editTextYearModel = findViewById(R.id.editText_year_model);
        editTextPrice = findViewById(R.id.editText_price);
        editTextContainerNumber = findViewById(R.id.editText_container_number);
        editTextSpecs = findViewById(R.id.editText_specs);
        imageViewUnitPicture = findViewById(R.id.image_view_unit_picture);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        vehicleReference = getIntent().getStringExtra("vehicle_reference");
        loadVehicleData();
    }

    private void loadVehicleData(){
        progressDialog.show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("vehicles").document(vehicleReference)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            vehicle = task.getResult().toObject(Vehicle.class);

                            editTextChassisNumber.setText(vehicle.getChassis_number());
                            editTextPrice.setText(vehicle.getPrice());
                            editTextSpecs.setText(vehicle.getSpecs());
                            editTextYearModel.setText(String.valueOf(vehicle.getYear_model()));
                            editTextContainerNumber.setText(String.valueOf(vehicle.getContainer_number()));
                            editTextUpdatedPrice.setText(vehicle.getUpdated_price());
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();

                            storageRef.child(vehicle.getPhoto()).getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            progressDialog.hide();
                                            Glide.with(getApplicationContext()).load(uri).into(imageViewUnitPicture);

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.hide();

                                            Toast.makeText(DetailsActivity.this, "Failed to get photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }else{
                            progressDialog.hide();
                            Toast.makeText(DetailsActivity.this, "Something went wrong please try again!", Toast.LENGTH_SHORT).show();
                        }
                        
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadVehicleData();

    }
}