package com.example.androidproject.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.androidproject.models.Collectors;
import com.example.androidproject.models.Garbage;
import com.example.androidproject.models.NetworkStatus;
import com.example.androidproject.repositories.CollectorRepository;
import com.example.androidproject.repositories.GarbageRepository;

import java.util.List;

public class GarbageViewModel extends AndroidViewModel {
    private static GarbageViewModel ourInstance;
    private final GarbageRepository garbageRepository = new GarbageRepository();
    public MutableLiveData<List<Garbage>> AllGarbage;

    public GarbageViewModel(@NonNull Application application) {
        super(application);
    }

    public static GarbageViewModel getInstance(Application application){
        if (ourInstance == null){
            ourInstance = new GarbageViewModel(application);
        }
        return ourInstance;
    }

    public GarbageRepository getGarbageRepository(){
        return this.garbageRepository;
    }

    public LiveData<NetworkStatus> updateGarbageRequest(Garbage garbage){
        return this.garbageRepository.updateGarbageRequest(garbage);
    }

    public void allGarbageOfUser(String email){
        this.garbageRepository.allRequestOfUser(email);
    }

    public void setAllGarbageRequests(){
        this.garbageRepository.allRequests();
    }

    public void addGarbageRequest(Garbage garbage){
        this.garbageRepository.addGarbageRequest(garbage);
    }

}
