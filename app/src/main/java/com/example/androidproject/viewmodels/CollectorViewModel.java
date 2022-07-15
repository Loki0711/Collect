package com.example.androidproject.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.androidproject.models.Collectors;
import com.example.androidproject.repositories.CollectorRepository;

import java.util.List;

public class CollectorViewModel extends AndroidViewModel {
    private static CollectorViewModel ourInstance;
    private final CollectorRepository collectorRepository = new CollectorRepository();
    public MutableLiveData<List<Collectors>> allCollectors;

    public CollectorViewModel(@NonNull Application application) {
        super(application);
    }

    public static CollectorViewModel getInstance(Application application){
        if (ourInstance == null){
            ourInstance = new CollectorViewModel(application);
        }
        return ourInstance;
    }

    public CollectorRepository getCollectorRepository(){
        return this.collectorRepository;
    }

    public void searchCollector(String email,String password){
        this.collectorRepository.searchCollector(email,password);
    }
}
