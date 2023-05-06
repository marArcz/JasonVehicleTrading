package com.example.jasonvehicletrading;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.view.ActionMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

public class AddVehicleActivity extends AppCompatActivity {
    final int SELECT_IMAGE_REQUEST_CODE = 100;
    CustomProgressDialog progressDialog;
    Button btnSave,btnCancel,btnChangePicture;
    ImageView imageViewUnitPicture;
    EditText editTextChassisNumber, editTextYearModel, editTextPrice, editTextContainerNumber, editTextSpecs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

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

        editTextChassisNumber = findViewById(R.id.editText_chassis_number);
        editTextYearModel = findViewById(R.id.editText_year_model);
        editTextPrice = findViewById(R.id.editText_price);
        editTextContainerNumber = findViewById(R.id.editText_container_number);
        editTextSpecs = findViewById(R.id.editText_specs);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!allFilled()){
                    return;
                }

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

                UploadTask uploadTask = storageRef.child(imageViewUnitPicture.getTag().toString()).putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(AddVehicleActivity.this, "Failed to upload photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        String unitPicture = imageViewUnitPicture.getTag().toString();

                        //save new vehicle to db
                        String chassisNumber = editTextChassisNumber.getText().toString();
                        String unit = editTextChassisNumber.getText().toString();
                        int yearModel = Integer.parseInt(editTextYearModel.getText().toString());
                        String price = editTextPrice.getText().toString();
                        int containerNumber = Integer.parseInt(editTextContainerNumber.getText().toString());
                        String specs = editTextSpecs.getText().toString();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Vehicle vehicle = new Vehicle(unit,chassisNumber,specs,price,"",yearModel,containerNumber,false,unitPicture);
                        db.collection("vehicles").add(vehicle)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        if(progressDialog.isShowing()) progressDialog.hide();
                                        Toast.makeText(AddVehicleActivity.this, "Successfully added!", Toast.LENGTH_SHORT).show();
                                        clearFields();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if(progressDialog.isShowing()) progressDialog.hide();
                                        Toast.makeText(AddVehicleActivity.this, "Error Adding: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });
            }
        });

        btnChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mGetContent.launch("image/*");
                ImagePicker.with(AddVehicleActivity.this)
                        .compress(2048)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
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
        super.onBackPressed();
    }

    private boolean allFilled(){
        String unitPicture = imageViewUnitPicture.getTag().toString();

        //save new vehicle to db
        String chassisNumber = editTextChassisNumber.getText().toString();
        String unit = editTextChassisNumber.getText().toString();
        String yearModel = editTextYearModel.getText().toString();
        String price = editTextPrice.getText().toString();
        String containerNumber = editTextContainerNumber.getText().toString();
        String specs = editTextSpecs.getText().toString();

        return !unitPicture.isEmpty() && !chassisNumber.isEmpty() && !unit.isEmpty() && !yearModel.isEmpty() && !price.isEmpty() && !containerNumber.isEmpty() && !specs.isEmpty();
    }

    private void clearFields(){
        imageViewUnitPicture.setTag("");
        imageViewUnitPicture.setImageDrawable(getDrawable(R.drawable.image_placeholder));
        editTextChassisNumber.setText("");
        editTextSpecs.setText("");
        editTextPrice.setText("");
        editTextYearModel.setText("");
        editTextContainerNumber.setText("");
    }

}