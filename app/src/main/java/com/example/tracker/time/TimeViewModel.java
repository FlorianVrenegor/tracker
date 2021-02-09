package com.example.tracker.time;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TimeViewModel extends AndroidViewModel {

    private TimeBoxRepository repository;

    private final LiveData<List<TimeBoxEntity>> allTimeBoxes;

    public TimeViewModel(Application application) {
        super(application);
        repository = new TimeBoxRepository(application);
        allTimeBoxes = repository.getAllTimeBoxes();
    }

    LiveData<List<TimeBoxEntity>> getAllTimeBoxes() {
        return allTimeBoxes;
    }

    public void insert(TimeBoxEntity timeBox) {
        repository.insert(timeBox);
    }
}
