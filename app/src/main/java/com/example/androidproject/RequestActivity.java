package com.example.androidproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.androidproject.adapters.RequestAdapter;
import com.example.androidproject.databinding.ActivityRequestsBinding;
import com.example.androidproject.utils.AppConstant;
import com.example.androidproject.viewmodels.GarbageViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import com.google.firebase.auth.FirebaseAuth;

public class RequestActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getCanonicalName();
    private ActivityRequestsBinding binding;
    private GarbageViewModel garbageViewModel;
    private SharedPreferences prefs;
    private RequestAdapter requestsAdapter;

    private String currentUserType = "";
    private String currentUserEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityRequestsBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        setTitle("Dashboard");
        this.binding.addDetail.setOnClickListener(this);
        this.garbageViewModel = GarbageViewModel.getInstance(this.getApplication());
        prefs = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);

        startFlow();
    }

    private void startFlow() {
        currentUserType = prefs.getString(AppConstant.USER_TYPE, "");
        currentUserEmail = prefs.getString(AppConstant.USER_EMAIL, "");

        initUI();
        observer();

        if (currentUserType.equals(AppConstant.USERTYPE_COLLECTOR)) {
            initCollectorFlow();
            binding.label.setText(getString(R.string.all_requests));
            binding.addDetail.setVisibility(View.GONE);
        } else {
            //Current user is producer
            binding.label.setText(getString(R.string.history));
            getAllGarbageOfUser(currentUserEmail);

        }

    }

    private void initCollectorFlow() {
        garbageViewModel.getGarbageRepository().allRequests();
    }

    private void initUI() {
        requestsAdapter = new RequestAdapter(new Geocoder(this), currentUserType);
        binding.rvGarbageRequests.setAdapter(requestsAdapter);
        binding.rvGarbageRequests.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            switch (view.getId()) {
                case R.id.addDetail: {
                    Log.d(TAG, "onClick: Add Button Clicked");
                    Intent detail = new Intent(this, RequestPickupActivity.class);
                    startActivity(detail);
                    break;
                }
            }
        }
    }

    private void observer() {
        try {
            this.garbageViewModel.getGarbageRepository().allGarbage.observe(RequestActivity.this, garbage -> {
                requestsAdapter.submitList(garbage);

                if (garbage.isEmpty()) {
                    Toast.makeText(RequestActivity.this,
                            "No Requests found",
                            Toast.LENGTH_SHORT).show();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllGarbageOfUser(String userEmail) {
        if (!userEmail.isEmpty()) {
            this.garbageViewModel.allGarbageOfUser(userEmail);
        } else {
            Log.e(TAG, "No logged in user found");
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.miLogout) {


            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.are_you_sure_you_want_to_logout))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                        logout();
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

                    })
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {

        FirebaseAuth.getInstance().signOut();
        prefs.edit().putString("USER_EMAIL", "").apply();
        prefs.edit().putString("USER_TYPE", "").apply();

        Toast.makeText(this, "Logout successfully!!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }

}
