package com.example.jasonvehicletrading;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Vector;

import de.codecrafters.tableview.TableHeaderAdapter;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.providers.TableDataRowBackgroundProvider;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

public class MainActivity extends AppCompatActivity {
    TableView tableView;
    EditText editTextSearch;
    ArrayList<Vehicle> vehicles;
    Button btnAdd, btnRefresh;
    LinearLayout appbar;

    int columnWidth = 200;
    int editTextSearchHeight = 48;
    boolean isFetching = false;
    QuerySnapshot currentDataSnaphot;
    VehicleTableAdapter vehicleTableAdapter;
    ListenerRegistration dbListenerRegistration;

    private static final String[][] DATA_TO_SHOW = { { "This", "is", "a", "test" },
            { "and", "a", "second", "test" } };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRefresh = findViewById(R.id.btn_refresh);
        appbar = findViewById(R.id.appbar);
        vehicles = new ArrayList<>();

        String tableHeaders[] = {"","Container Number","Unit","Yr. Model","Specs","Price","Updated Price"};
        editTextSearch = findViewById(R.id.editText_search);

        tableView = findViewById(R.id.tableView);
        VehicleTableHeaderAdapter headerAdapter = new VehicleTableHeaderAdapter(this, tableHeaders);
        tableView.setHeaderAdapter(headerAdapter);
        tableView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TableColumnWeightModel columnModel = new TableColumnWeightModel(tableHeaders.length);
        columnModel.setColumnWeight(0,1);
        columnModel.setColumnWeight(1,2);
        columnModel.setColumnWeight(2,3);
        columnModel.setColumnWeight(3,2);
        columnModel.setColumnWeight(4,4);
        columnModel.setColumnWeight(5,2);
        columnModel.setColumnWeight(6,2);

        tableView.setColumnModel(columnModel);

        int colorEvenRows = getResources().getColor(R.color.green);
        int colorOddRows = getResources().getColor(R.color.table_gray);

        tableView.setDataRowBackgroundProvider(new TableRowBgProvider(getApplicationContext()));

        tableView.setSwipeToRefreshEnabled(true);

        tableView.setSwipeToRefreshListener(new SwipeToRefreshListener() {
            @Override
            public void onRefresh(RefreshIndicator refreshIndicator) {
                loadVehicles(new FirebaseRequestCallback.onCompleteListener() {
                    @Override
                    public void requestCompleted() {
                        refreshIndicator.hide();
                    }
                });
            }
        });

        tableView.setEmptyDataIndicatorView(findViewById(R.id.table_empty_indicator_textview));

        vehicleTableAdapter = new VehicleTableAdapter(this, vehicles);
        tableView.setDataAdapter(vehicleTableAdapter);
        loadVehicles(new FirebaseRequestCallback.onCompleteListener() {
            @Override
            public void requestCompleted() {}
        });

        btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddVehicleActivity.class));
            }
        });

        tableView.addDataLongClickListener(new TableDataLongClickListener() {
            @Override
            public boolean onDataLongClicked(int rowIndex, Object clickedData) {
                ShowDialogMenu(rowIndex);
                return true;
            }
        });

        tableView.addDataClickListener(new TableDataClickListener() {
            @Override
            public void onDataClicked(int rowIndex, Object clickedData) {
                Vehicle v = vehicles.get(rowIndex);
                String ref = v.getId();

                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("vehicle_reference",ref);

                startActivity(intent);
            }
        });


        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                filterRows();
                return true;
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterRows();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFetching){
                    loadVehicles(new FirebaseRequestCallback.onCompleteListener() {
                        @Override
                        public void requestCompleted() {
                            Toast.makeText(MainActivity.this, "Table is now up to date!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

     initDbChangesListener();
    }

    private void initDbChangesListener(){
        //listener for changes in database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        dbListenerRegistration = db.collection("vehicles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if(currentDataSnaphot != null && !currentDataSnaphot.equals(snapshots) && !isFetching){
                          loadVehicles(new FirebaseRequestCallback.onCompleteListener() {
                              @Override
                              public void requestCompleted() {
                                  Snackbar.make(appbar,"New data updates are loaded!",Snackbar.LENGTH_LONG)
                                          .show();
                              }
                          });
                        }
                    }
                });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void loadVehicles(FirebaseRequestCallback.onCompleteListener onCompleteListener){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        isFetching = true;
        db.collection("vehicles")
                .whereEqualTo("deleted",false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            vehicles.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Vehicle v = document.toObject(Vehicle.class);
                                v.setId(document.getId());
                                vehicles.add(v);
                                Log.d("FIREBASE", document.getId() + " => " + document.getData());
                            }
                            filterRows();
                            isFetching = false;
                            currentDataSnaphot = task.getResult();

                            if(onCompleteListener != null) onCompleteListener.requestCompleted();
                        }
                    }
                });
    }

    private void filterRows(){
        if(vehicles != null){
            ArrayList<Vehicle> filteredRows = new ArrayList<>();

            String searchQuery = editTextSearch.getText().toString().toLowerCase();

            for(Vehicle v : vehicles){
                if(v.getChassis_number().toLowerCase().contains(searchQuery) || searchQuery.contains(v.getChassis_number().toLowerCase()))
                {
                    filteredRows.add(v);
                }
            }

            vehicleTableAdapter = new VehicleTableAdapter(getApplicationContext(),filteredRows);
            tableView.setDataAdapter(vehicleTableAdapter);
        }
    }

    private void ShowDialogMenu(int rowIndex){
        Vehicle vehicle = vehicles.get(rowIndex);

        new AlertDialog.Builder(this)
                .setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1 ,new String[]{"Update",(vehicle.isIs_sold()? "Mark As Unsold":"Mark As Sold"),"Delete"}), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            String vehicleRef = vehicles.get(rowIndex).getId();
                            Intent intent = new Intent(MainActivity.this, UpdateVechicleActivity.class);
                            intent.putExtra("vehicle_reference",vehicleRef);

                            startActivity(intent);
                        }else if (which == 1){
                            dialog.dismiss();
                            markAsSold(vehicle);
                        }else{
                            dialog.dismiss();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Deleting Unit")
                                    .setMessage("Are you sure you want to delete this unit?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteVehicle(vehicle);
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    }
                })
                .show();

    }


    private void deleteVehicle(Vehicle vehicle){
        vehicle.setDeleted(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vehicles")
                .document(vehicle.getId()).set(vehicle)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Successfully deleted!", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void markAsSold(Vehicle vehicle){
        vehicle.setIs_sold(!vehicle.isIs_sold());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vehicles")
                .document(vehicle.getId()).set(vehicle)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}