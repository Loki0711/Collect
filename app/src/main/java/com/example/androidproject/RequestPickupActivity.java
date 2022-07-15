package com.example.androidproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.example.androidproject.databinding.ActivityRequestPickupBinding;
import com.example.androidproject.helpers.LocationHelper;
import com.example.androidproject.models.Garbage;
import com.example.androidproject.utils.AppConstant;
import com.example.androidproject.viewmodels.GarbageViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

public class RequestPickupActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getCanonicalName();
    private ActivityRequestPickupBinding binding;
    private LocationHelper locationHelper;
    private SharedPreferences prefs;
    private String user = "";

    private GarbageViewModel garbageViewModel;
    private final Garbage garbage = new Garbage();
    private final ArrayList<String> garbageType = new ArrayList<String>();
    private String type;

    AdapterView.OnItemSelectedListener garbageTypeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "PERSON SELECTED AN ITEM " + garbageType.get(i));
            type = garbageType.get(i).toString();
            // this function is activated when the person selects an item from the spinner
            // int i = is the position of the item the person clicked on
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // ignore this for now
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_detail);
        this.binding = ActivityRequestPickupBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Add New Request");

        this.binding.request.setOnClickListener(this);
        locationHelper = LocationHelper.getInstance();
        garbageViewModel = new ViewModelProvider(RequestPickupActivity.this).get(GarbageViewModel.class);

        garbageType.add("Household Waste");
        garbageType.add("Hazardous Waste");
        garbageType.add("Medical Waste");
        garbageType.add("Electrical Waste");
        garbageType.add("Recyclable Waste");
        garbageType.add("Construction & Demolition Debris");
        garbageType.add("Green Waste");

        ArrayAdapter<String> adapterUser = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, garbageType);
        this.binding.spGarbageType.setAdapter(adapterUser);
        this.binding.spGarbageType.setOnItemSelectedListener(garbageTypeListener);
        prefs = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);

        initUI();
    }

    private void initUI() {
        binding.location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    binding.locationContainer.setError(null);
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        if (view != null) {
            switch (view.getId()) {
                case R.id.request: {
                    Log.d(TAG, "onClick: Request Button Clicked");
                    this.validateData();
                    break;
                }
            }
        }

    }

    public void validateData() {
        boolean validData = true;
        if (this.binding.location.getText().toString().isEmpty()) {
            this.binding.locationContainer.setError("Please Enter location to proceed");
            validData = false;
        } else {
            Log.d(TAG, "onClick: Perform reverse geocoding to get coordinates from address.");

            if (this.locationHelper.checkPermissions(this) && this.locationHelper.locationPermissionGranted) {
                String givenAddress = this.binding.location.getText().toString();
                LatLng obtainedCoordinates = this.locationHelper.performReverseGeocoding(this, givenAddress);

                if (obtainedCoordinates != null) {
                    garbage.setLat(obtainedCoordinates.latitude);
                    garbage.setLng(obtainedCoordinates.longitude);
                    Log.e(TAG, "Lat : " + obtainedCoordinates.latitude + "\nLng : " + obtainedCoordinates.longitude);
                } else {
                    this.binding.locationContainer.setError("No coordinates obtained");
                }
            } else {
                validData = false;
                this.binding.locationContainer.setError("Couldn't get the coordinates for given address");
            }
        }

        if (validData) {

            garbage.setAddedBy(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            garbage.setStatus(AppConstant.REQUEST_STATUS_PENDING);
            garbage.setPickedOn(null);
            garbage.setAcceptedBy(null);
            garbage.setPickedOn(null);
            garbage.setGarbageType(type);
            java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
            garbage.setAddedOn(date);
            Log.d(TAG, "Garbage details are " + garbage.toString());
            garbageViewModel.getGarbageRepository().addGarbageRequest(garbage).observe(this, networkStatus -> {
                switch (networkStatus.getStatus()) {
                    case ERROR: {
                        binding.progressBar.hide();
                        Toast.makeText(this, networkStatus.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case LOADING: {
                        Log.e(TAG, "Loading");
                        binding.progressBar.show();
                        break;
                    }

                    case SUCCESS: {
                        binding.progressBar.show();
                        Toast.makeText(this, networkStatus.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    }
                }
            });
        }
    }
}