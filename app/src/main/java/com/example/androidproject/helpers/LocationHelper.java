package com.example.androidproject.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationRequest;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private final String TAG = this.getClass().getCanonicalName();
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient = null;
    public boolean locationPermissionGranted = false;
    public final int REQUEST_CODE_LOCATION = 101;
    //    Location mLocation;
    MutableLiveData<Location> mLocation = new MutableLiveData<>();

    private static final LocationHelper instance = new LocationHelper();

    public static LocationHelper getInstance(){
        return instance;
    }

    private LocationHelper(){
        this.locationRequest = LocationRequest.create();
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        this.locationRequest.setInterval(10000); //10 seconds
    }

    public boolean checkPermissions(Context context){
        this.locationPermissionGranted = (PackageManager.PERMISSION_GRANTED ==
                (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)));

        Log.e(TAG, "checkPermissions: location permission granted : " + this.locationPermissionGranted );

        if (!this.locationPermissionGranted){
            requestLocationPermission(context);
        }

        return this.locationPermissionGranted;
    }

    public void requestLocationPermission(Context context){
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                this.REQUEST_CODE_LOCATION);
    }

    public FusedLocationProviderClient getFusedLocationProviderClient(Context context){
        if (this.fusedLocationProviderClient == null){
            this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }

        return this.fusedLocationProviderClient;
    }

    //    public Location getLastLocation(Context context){
    @SuppressLint("MissingPermission")
    public MutableLiveData<Location> getLastLocation(Context context){
        if (this.locationPermissionGranted){
            Log.e(TAG, "getLastLocation: Permission granted...trying to obtain location");
            try{
                this.getFusedLocationProviderClient(context)
                        .getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null){
//                                    mLocation = new Location(location);
//                                    Log.e(TAG, "onSuccess: Last Location Obtained Lat : " + mLocation.getLatitude() +
//                                            " Lng : " + mLocation.getLongitude());

                                    mLocation.setValue(location);
                                    Log.e(TAG, "onSuccess: Last Location Obtained Lat : " + mLocation.getValue().getLatitude() +
                                            " Lng : " + mLocation.getValue().getLongitude());
                                }else{
                                    Log.e(TAG, "onSuccess: Unable to access last location...probably because emulator is not having access to it.");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: Failed to get the last location " + e.getLocalizedMessage() );
                            }
                        });

            }catch (Exception ex){
                Log.e(TAG, "getLastLocation: Exception occurred while fetching last location " + ex.getLocalizedMessage() );
                return null;
            }

            return mLocation;
        }else{
            Log.e(TAG, "getLastLocation: Location Permission not granted" );
            requestLocationPermission(context);
            return null;
        }
    }

    public Address performForwardGeocoding(Context context, Location loc){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList;

        try{

            addressList = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);

            if (addressList.size() > 0){
                Address addressObj = addressList.get(0);
                Log.e(TAG, "performForwardGeocoding: Address obtained from geocoding " + addressObj.getAddressLine(0) );
                Log.e(TAG, "performForwardGeocoding: Postal Code " + addressObj.getPostalCode() );
                Log.e(TAG, "performForwardGeocoding: Country Code " + addressObj.getCountryCode() );
                Log.e(TAG, "performForwardGeocoding: Country Name " + addressObj.getCountryName());
                Log.e(TAG, "performForwardGeocoding: Locality " + addressObj.getLocality());
                Log.e(TAG, "performForwardGeocoding: getThoroughfare " + addressObj.getThoroughfare());
                Log.e(TAG, "performForwardGeocoding: getSubThoroughfare " + addressObj.getSubThoroughfare());

                return addressObj;
            }

        }catch(Exception ex){
            Log.e(TAG, "performForwardGeocoding: Couldn't get the address for the given location coordinates " + ex.getLocalizedMessage() );
        }

        return null;
    }

    public LatLng performReverseGeocoding(Context context, String address){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try{
            List<Address> locationList = geocoder.getFromLocationName(address, 1);

            if(locationList.size() > 0) {
                LatLng obtainedCoordinates = new LatLng(locationList.get(0).getLatitude(), locationList.get(0).getLongitude());

                Log.e(TAG, "performReverseGeocoding: Obtained Coordinates : " + obtainedCoordinates.toString() );

                return obtainedCoordinates;
            }
        }catch(Exception ex){
            Log.e(TAG, "performReverseGeocoding: Couldn't get the LatLng for the given address " + ex.getLocalizedMessage() );
        }

        return null;
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates(Context context, LocationCallback locationCallback){
        if (this.locationPermissionGranted){
            try{
                this.getFusedLocationProviderClient(context).requestLocationUpdates(this.locationRequest, locationCallback, Looper.getMainLooper());
            }catch(Exception ex){
                Log.e(TAG, "stopLocationUpdates: Exception occurred while receiving location updates " + ex.getLocalizedMessage() );
            }

        }else{
            Log.e(TAG, "requestLocationUpdates: No Permission.. No Location updates");
        }
    }

    public void stopLocationUpdates(Context context, LocationCallback locationCallback){
        try{
            this.getFusedLocationProviderClient(context).removeLocationUpdates(locationCallback);
        }catch(Exception ex){
            Log.e(TAG, "stopLocationUpdates: Exception occurred while stopping location updates " + ex.getLocalizedMessage() );
        }
    }
}
