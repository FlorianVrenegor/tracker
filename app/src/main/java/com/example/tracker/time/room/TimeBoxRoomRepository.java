package com.example.tracker.time.room;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.tracker.time.room.TimeBoxDao;
import com.example.tracker.time.room.TimeBoxEntity;
import com.example.tracker.time.room.TimeBoxRoomDatabase;

import java.util.List;

public class TimeBoxRoomRepository {

    private TimeBoxDao timeBoxDao;
    private LiveData<List<TimeBoxEntity>> allTimeBoxes;

    public TimeBoxRoomRepository(Application application) {
        TimeBoxRoomDatabase db = TimeBoxRoomDatabase.getDatabase(application);
        timeBoxDao = db.timeBoxDao();
        allTimeBoxes = timeBoxDao.getAllEntities();
    }

    LiveData<List<TimeBoxEntity>> getAllTimeBoxes() {
        return allTimeBoxes;
    }

    void insert(TimeBoxEntity timeBoxEntity) {
        TimeBoxRoomDatabase.databaseWriteExecutor.execute(() -> {
            timeBoxDao.insert(timeBoxEntity);
        });
    }
}
