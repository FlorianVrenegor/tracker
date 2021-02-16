package com.example.tracker.time;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tracker.time.room.TimeBoxRoomRepository;

import java.util.List;

public class TimeViewModel extends AndroidViewModel {

    private TimeBoxRoomRepository roomRepository;
    private TimeBoxFirebaseRepository firebaseRepository;

//    private final LiveData<List<TimeBoxEntity>> allTimeBoxes;
    private final LiveData<List<TimeBoxDto>> allTimeBoxes;

    public TimeViewModel(Application application) {
        super(application);
        roomRepository = new TimeBoxRoomRepository(application);
        firebaseRepository = new TimeBoxFirebaseRepository();
//        allTimeBoxes = roomRepository.getAllTimeBoxes();
        allTimeBoxes = firebaseRepository.getAllTimeBoxes();

    }

    LiveData<List<TimeBoxDto>> getAllTimeBoxes() {
        return allTimeBoxes;
    }

    public void insert(TimeBoxDto timeBox) {
        allTimeBoxes.getValue().add(timeBox);
        firebaseRepository.saveTimeBox(timeBox);
//        roomRepository.insert(timeBox);
    }
}
