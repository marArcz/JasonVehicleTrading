package com.example.jasonvehicletrading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class UpdateVechicleActivity extends AppCompatActivity {

    final int SELECT_IMAGE_REQUEST_CODE = 100;
    CustomProgressDialog progressDialog;
    Button btnSave,btnCancel,btnChangePicture;
    ImageView imageViewUnitPicture;
    EditText editTextChassisNumber, editTextYearModel, editTextPrice, editTextContainerNumber, editTextSpecs, editTextUpdatedPrice;
    Vehicle vehicle;
    private String vehicleRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_vechicle);

        vehicleRef = getIntent().getStringExtra("vehicle_reference");

        progressDialog = new CustomProgressDialog(this);

        imageViewUnitPicture = findViewById(R.id.image_view_unit_picture);

        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChangePicture = findViewById(R.id.btn_change_picture);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        btnChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(UpdateVechicleActivity.this)
                        .compress(2048)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                //upload photo
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                // Get the data from an ImageView as bytes
                imageViewUnitPicture.setDrawingCacheEnabled(true);
                imageViewUnitPicture.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) imageViewUnitPicture.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

             if(imageViewUnitPicture.getTag() != vehicle.getPhoto()){
                 UploadTask uploadTask = storageRef.child(imageViewUnitPicture.getTag().toString()).putBytes(data);
                 uploadTask.addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception exception) {
                         // Handle unsuccessful uploads
                         Toast.makeText(UpdateVechicleActivity.this, "Failed to upload photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                     }
                 }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                         SaveVehicle();
                     }
                 });
             }else{
                 SaveVehicle();
             }
            }
        });


        editTextChassisNumber = findViewById(R.id.editText_chassis_number);
        editTextYearModel = findViewById(R.id.editText_year_model);
        editTextPrice = findViewById(R.id.editText_price);
        editTextContainerNumber = findViewById(R.id.editText_container_number);
        editTextSpecs = findViewById(R.id.editText_specs);
        editTextUpdatedPrice = findViewById(R.id.editText_updated_price);
        LoadVehicle();
    }

    private void SaveVehicle(){
        String unitPicture = imageViewUnitPicture.getTag().toString();

        //save new vehicle to db
        String chassisNumber = editTextChassisNumber.getText().toString();
        String unit = editTextChassisNumber.getText().toString();
        int yearModel = Integer.parseInt(editTextYearModel.getText().toString());
        String price = editTextPrice.getText().toString();
        int containerNumber = Integer.parseInt(editTextContainerNumber.getText().toString());
        String specs = editTextSpecs.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        vehicle.setChassis_number(chassisNumber);
        vehicle.setUnit(unit);
        vehicle.setYear_model(yearModel);
        vehicle.setPrice(price);
        vehicle.setContainer_number(containerNumber);
        vehicle.setSpecs(specs);
        vehicle.setPhoto(unitPicture);
        vehicle.setUpdated_price(editTextUpdatedPrice.getText().toString());
        db.collection("vehicles").document(vehicleRef).set(vehicle)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if(progressDialog.isShowing()) progressDialog.hide();
                        Toast.makeText(UpdateVechicleActivity.this, "Changes are successfully saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(progressDialog.isShowing()) progressDialog.hide();
                        Toast.makeText(UpdateVechicleActivity.this, "Error Saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void LoadVehicle(){
        Toast.makeText(this, "row id: " + vehicleRef, Toast.LENGTH_SHORT).show();
        progressDialog.show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vehicles")
                .document(vehicleRef)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            vehicle = task.getResult().toObject(Vehicle.class);

                            if(vehicle != null){
                                editTextContainerNumber.setText(String.valueOf(vehicle.getContainer_number()));
                                editTextSpecs.setText(String.valueOf(vehicle.getSpecs()));
                                editTextPrice.setText(vehicle.getPrice());
                                editTextYearModel.setText(String.valueOf(vehicle.getYear_model()));
                                editTextChassisNumber.setText(vehicle.getChassis_number());
                                editTextUpdatedPrice.setText(vehicle.getUpdated_price());

                                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                storageRef.child(vehicle.getPhoto()).getDownloadUrl()
                                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                progressDialog.hide();
                                                if(task.isSuccessful()){
                                                    Glide.with(getApplicationContext()).load(task.getResult()).into(imageViewUnitPicture);
                                                    imageViewUnitPicture.setTag(vehicle.getPhoto());
                                                }else{
                                                    Toast.makeText(UpdateVechicleActivity.this, "Something went wrong while loading the image!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }else{
                                progressDialog.hide();
                                Toast.makeText(UpdateVechicleActivity.this, "Vehicle is not found!!", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            progressDialog.hide();
                            Toast.makeText(UpdateVechicleActivity.this, "Something went wrong please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ImagePicker.REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                // Handle the returned Uri
                Uri uri = data.getData();
                File tempFile = new File(uri.getPath());
                String fileName = tempFile.getName();
                Log.d("UNIT_PICTURE",fileName);
                imageViewUnitPicture.setTag(fileName);
                imageViewUnitPicture.setImageURI(uri);
            }
            else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
//        new AlertDialog.Builder(this)
//                .setTitle("Cancel Adding Vehicle")
//                .setMessage("Your changes will be discarded are you sure to cancel?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                    }
//
//                })
//                .setNegativeButton("No", null)
//                .show();
        super.onBackPressed();
    }
}