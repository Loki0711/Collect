package com.example.androidproject;

import static com.example.androidproject.utils.AppConstant.REQUEST_STATUS_ACCEPTED;
import static com.example.androidproject.utils.AppConstant.REQUEST_STATUS_PENDING;
import static com.example.androidproject.utils.AppConstant.REQUEST_STATUS_PICKED;
import static com.example.androidproject.utils.AppConstant.REQUEST_STATUS_REJECTED;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.databinding.ActivityDetailBinding;
import com.example.androidproject.models.Garbage;
import com.example.androidproject.utils.AppConstant;
import com.example.androidproject.viewmodels.GarbageViewModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private static final String GARBAGE = "garbage";
    private final String TAG = this.getClass().getCanonicalName();


    public static void startActivity(Context context, Garbage garbage) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(GARBAGE, garbage);

        Intent detailActivity = new Intent(context, DetailActivity.class);
        detailActivity.putExtra("Bundle", bundle);
        context.startActivity(detailActivity);
    }

    private SharedPreferences prefs;
    private Garbage garbage;
    private ActivityDetailBinding binding;
    private GarbageViewModel garbageViewModel;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.garbageViewModel = GarbageViewModel.getInstance(this.getApplication());
        prefs = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);

        garbage = getIntent().getBundleExtra("Bundle").getParcelable(GARBAGE);
        currentUserEmail = prefs.getString(AppConstant.USER_EMAIL, "");

        if (garbage != null)
            initUI();
        else Log.e("TAG", "Garbage is empty");

    }

    private void initUI() {

        try {
            binding.tvName.setText(garbage.getGarbageType());
            binding.tvAddedBy.setText("Added By: " + garbage.getAddedBy());
            binding.tvStatus.setText("Status: " + garbage.getStatus());

            if (garbage.getAddedOn() != null) {
                binding.tvDate.setText("Added on " + getFormattedDate(garbage.getAddedOn()));
                binding.tvTime.setText("At " + getTime(garbage.getAddedOn()));
            }


            if (garbage.getPickedOn() != null) {
                binding.tvPickedOn.setText("Picked on: " + getTime(garbage.getPickedOn()) + " , at " + getFormattedDate(garbage.getPickedOn()));
            }


            Log.e(TAG,"Current user is: "+currentUserEmail);


            if (garbage.getStatus() != null)
                switch (garbage.getStatus()) {
                    case REQUEST_STATUS_PENDING: {
                        Log.e("TAG","Pending Status Triggered");
                        binding.rgOptions.setVisibility(View.VISIBLE);
                        binding.btnSubmit.setVisibility(View.VISIBLE);
                        break;
                    }

                    case REQUEST_STATUS_ACCEPTED: {
                        Log.e("TAG","Accepted Status Triggered : " + garbage.getAcceptedBy() );

                        if (garbage.getAcceptedBy() != null && garbage.getAcceptedBy().equals(currentUserEmail)) {
                            binding.btnSubmit.setVisibility(View.VISIBLE);
                            binding.btnSubmit.setText(getString(R.string.picked));
                        } else {
                            binding.btnSubmit.setVisibility(View.GONE);
                        }

                        binding.rgOptions.setVisibility(View.GONE);
                        break;
                    }

                    case REQUEST_STATUS_REJECTED: {
                        Log.e("TAG","Accepted Status Rejected : " + garbage.getAcceptedBy() );
                        binding.rgOptions.setVisibility(View.GONE);
                        binding.btnSubmit.setVisibility(View.GONE);
                        break;
                    }

                    case REQUEST_STATUS_PICKED: {
                        Log.e("TAG","Accepted Status Picked : " + garbage.getAcceptedBy() );

                        binding.rgOptions.setVisibility(View.GONE);
                        binding.btnSubmit.setVisibility(View.GONE);
                        if (garbage.getPickedOn() != null) {
                            binding.tvPickedOn.setText("Picked on: " + getTime(garbage.getPickedOn()) + " , " + getFormattedDate(garbage.getPickedOn()));
                        }
                        break;
                    }

                    default: {
                        binding.rgOptions.setVisibility(View.GONE);
                        binding.btnSubmit.setVisibility(View.GONE);
                    }
                }


            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> address = geocoder.getFromLocation(garbage.getLat(), garbage.getLng(), 4);
                if (!address.isEmpty()) {
                    binding.tvAddress.setText(address.get(0).getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        onClick();
    }

    private void onClick() {
        binding.btFindRoute.setOnClickListener(v -> {
            String geoUri = "http://maps.google.com/maps?q=loc:" + garbage.getLat() + "," + garbage.getLng() + " (" + garbage.getGarbageType() + ")";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            startActivity(intent);
        });

        binding.btnSubmit.setOnClickListener(v -> {
            if (binding.rbAccept.isChecked()) {
                Log.e(TAG,"Accepted Clicked");
                garbage.setAcceptedBy(currentUserEmail);
                garbage.setAcceptedBy(currentUserEmail);
                updateStatus(AppConstant.REQUEST_STATUS_ACCEPTED);
            } else if (binding.rbReject.isChecked()) {
                Log.e(TAG,"Rejected Clicked");
                updateStatus(AppConstant.REQUEST_STATUS_REJECTED);
            } else if (garbage.getStatus().equals(REQUEST_STATUS_ACCEPTED)) {
                Log.e(TAG,"Picked Clicked");
                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
                garbage.setPickedOn(date);
                garbage.setPickedBy(currentUserEmail);
                updateStatus(REQUEST_STATUS_PICKED);
            } else {
                Toast.makeText(this, "Please Select options", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFormattedDate(Date date) {
        return new SimpleDateFormat("dd/MMM/yyyy").format(date);
    }

    private String getTime(Date date) {
        return new SimpleDateFormat("H:mm").format(date);
    }

    private void updateStatus(String status) {
        garbage.setStatus(status);
        garbageViewModel.updateGarbageRequest(garbage).observe(this, networkStatus -> {
            switch (networkStatus.getStatus()) {
                case ERROR: {
                    binding.progressBar.hide();
                    Toast.makeText(this, networkStatus.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                }

                case LOADING: {
                    binding.progressBar.show();
                    break;
                }

                case SUCCESS: {
                    binding.progressBar.hide();
                    Toast.makeText(this, networkStatus.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
            }
        });
    }
}