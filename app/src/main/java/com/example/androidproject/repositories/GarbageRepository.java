package com.example.androidproject.repositories;

import android.net.Network;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.androidproject.models.Collectors;
import com.example.androidproject.models.Garbage;
import com.example.androidproject.models.NetworkStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GarbageRepository {

    private final String TAG = this.getClass().getCanonicalName();
    private final FirebaseFirestore DB;
    private final String COLLECTION_Garbage = "Requests";
    private final String COLLECTION_USER_REQUEST = "userRequest";
    private final String FIELD_NAME = "addedBy";
    private final String FIELD_TYPE = "garbageType";
    private final String FIELD_STATUS = "status";
    private final String FIELD_LAT = "lat";
    private final String FIELD_LONG = "lng";
    private final String FIELD_ADDED_ON = "addedOn";
    private final String FIELD_PICKED_ON = "pickedOn";
    private final String FIELD_PICKED_BY = "pickedBy";
    private final String FIELD_ACCEPTED_BY = "acceptedBy";

    public MutableLiveData<List<Garbage>> allGarbage = new MutableLiveData<>();
    public MutableLiveData<Garbage> garbageFromDB = new MutableLiveData<>();

    public GarbageRepository() {
        DB = FirebaseFirestore.getInstance();
    }

    public LiveData<NetworkStatus> addGarbageRequest(Garbage garbageRequest) {
        MutableLiveData<NetworkStatus> status = new MutableLiveData<>();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put(FIELD_NAME, garbageRequest.getAddedBy());
            data.put(FIELD_ADDED_ON, garbageRequest.getAddedOn());
            data.put(FIELD_LAT, garbageRequest.getLat());
            data.put(FIELD_LONG, garbageRequest.getLng());
            data.put(FIELD_STATUS, garbageRequest.getStatus());
            data.put(FIELD_PICKED_ON, garbageRequest.getPickedOn());
            data.put(FIELD_TYPE, garbageRequest.getGarbageType());
            data.put(FIELD_ACCEPTED_BY,garbageRequest.getAcceptedBy());
            data.put(FIELD_PICKED_BY,garbageRequest.getPickedBy());

            status.postValue(new NetworkStatus("Loading", NetworkStatus.Status.LOADING));

            //create subcollections containing documents
            DB.collection(COLLECTION_Garbage)
                    .add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference reference) {
                            status.postValue(new NetworkStatus("Request Added Successfully!", NetworkStatus.Status.SUCCESS));
                            Log.d(TAG, "onSuccess: Document Added successfully with ID : " + reference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            status.postValue(new NetworkStatus(e.getMessage(), NetworkStatus.Status.ERROR));
                            Log.e(TAG, "onFailure: Error while creating document " + e.getLocalizedMessage());
                        }
                    });
        } catch (Exception ex) {
            status.postValue(new NetworkStatus(ex.getMessage(), NetworkStatus.Status.ERROR));
            Log.e(TAG, "addRequest: " + ex.getLocalizedMessage());
        }


        return status;
    }

    public LiveData<NetworkStatus> updateGarbageRequest(Garbage garbageRequest) {
        MutableLiveData<NetworkStatus> networkStatus = new MutableLiveData<>();

        Map<String, Object> updatedInfo = new HashMap<>();
        updatedInfo.put(FIELD_STATUS, garbageRequest.getStatus());
        updatedInfo.put(FIELD_PICKED_ON, garbageRequest.getPickedOn());
        updatedInfo.put(FIELD_ACCEPTED_BY,garbageRequest.getAcceptedBy());
        updatedInfo.put(FIELD_PICKED_BY,garbageRequest.getPickedBy());

        try {

            networkStatus.postValue(new NetworkStatus("Loading", NetworkStatus.Status.LOADING));

            DB.collection(COLLECTION_Garbage)
                    .document(garbageRequest.getId())
                    .update(updatedInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            networkStatus.postValue(new NetworkStatus("Successfully Updated", NetworkStatus.Status.ERROR));

                            Log.d(TAG, "onSuccess: Document successfully updated");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            networkStatus.postValue(new NetworkStatus(e.getLocalizedMessage(), NetworkStatus.Status.ERROR));
                            Log.e(TAG, "onFailure: Unable to update document" + e.getLocalizedMessage());
                        }
                    });
        } catch (Exception ex) {
            networkStatus.postValue(new NetworkStatus(ex.getLocalizedMessage(), NetworkStatus.Status.ERROR));
            Log.e(TAG, "updateRequest: Exception occurred " + ex.getLocalizedMessage());
        }

        return networkStatus;
    }


    public void allRequestOfUser(String email) {
        try {
            DB.collection(COLLECTION_Garbage)
                    .whereEqualTo(FIELD_NAME,email)
                    .orderBy(FIELD_ADDED_ON, Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e(TAG, "onEvent1: Unable to get document changes " + error);
                                return;
                            }

                            List<Garbage> garbageList = new ArrayList<>();
                            assert snapshot != null;
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                if (document.exists()) {
                                    try {
                                        Garbage garbage = document.toObject(Garbage.class);
                                        assert garbage != null;
                                        garbage.setId(document.getId());
                                        garbageList.add(garbage);
                                    }catch (Exception e ){
                                        e.printStackTrace();
                                    }

                                }
                            }

                            Log.e(TAG, "onEvent1: Garbage List Found of " + email + "\n " + snapshot.getDocuments());
                            allGarbage.postValue(garbageList);

                        }
                    });

        } catch (Exception ex) {
            Log.e(TAG, "getAllFriends1: Exception occured " + ex.getLocalizedMessage());
            Log.e(TAG, String.valueOf(ex.getStackTrace()));
        }


    }


    public void allRequests() {
        try {
            DB.collection(COLLECTION_Garbage)
                    .orderBy(FIELD_ADDED_ON, Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e(TAG, "onEvent2: Unable to get document changes " + error);
                                return;
                            }

                            List<Garbage> garbageList = new ArrayList<>();
                            assert snapshot != null;
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                if (document.exists()) {
                                    try {
                                        Garbage garbage = document.toObject(Garbage.class);
                                        assert garbage != null;
                                        garbage.setId(document.getId());
                                        garbageList.add(garbage);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }

                            Log.e(TAG, "onEvent2: Garbage List Found of  " + snapshot.getDocuments());
                            allGarbage.postValue(garbageList);

                        }
                    });

        } catch (Exception ex) {
            Log.e(TAG, "getAllFriends2: Exception occured " + ex.getLocalizedMessage());
            Log.e(TAG, String.valueOf(ex.getStackTrace()));
        }

    }



}
