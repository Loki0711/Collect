package com.example.androidproject.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.androidproject.models.Collectors;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class CollectorRepository {
    private final String TAG = this.getClass().getCanonicalName();
    private final FirebaseFirestore DB;
    private final String COLLECTION_COLLECTORS = "Collectors";
    private final String FIELD_NAME = "Username";
    private final String FIELD_PASS = "Password";
    public String loggedInUserEmail = "";


    public MutableLiveData<List<Collectors>> allCollectors = new MutableLiveData<>();
    public MutableLiveData<Collectors> collectorsFromDB = new MutableLiveData<>();

    public CollectorRepository() {
        DB = FirebaseFirestore.getInstance();
    }

    public void searchCollector(String email,String password){
        try{
            DB.collection(COLLECTION_COLLECTORS)
                    .whereEqualTo(FIELD_NAME, email)
                    .whereEqualTo(FIELD_PASS,password)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().getDocuments().size() != 0){
                                    //matching friends found
                                    Collectors matchedCollector = task.getResult().getDocuments().get(0).toObject(Collectors.class);
                                    matchedCollector.setId(task.getResult().getDocuments().get(0).getId());

                                    if (matchedCollector != null){
                                        Log.d(TAG, "onComplete: User found " + matchedCollector.toString());
                                        collectorsFromDB.postValue(matchedCollector);
                                    }else{
                                        Log.e(TAG, "onComplete: Unable to convert the matching document to Collector object");
                                    }

                                }else{
                                    //no friend with given name
                                    Log.e(TAG, "onComplete: No friend with give name found");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: Unable to find friends with name " + email + e.getLocalizedMessage() );
                        }
                    });
        }catch(Exception ex){
            Log.e(TAG, "searchFriendByName: Exception occurred " + ex.getLocalizedMessage() );
            Log.e(TAG, "onFailure: Unable to find friends with name " + email + ex.getLocalizedMessage() );
        }
    }
}
