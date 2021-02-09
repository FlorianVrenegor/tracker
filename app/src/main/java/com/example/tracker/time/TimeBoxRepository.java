package com.example.tracker.time;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TimeBoxRepository {

    private TimeBoxDao timeBoxDao;
    private LiveData<List<TimeBoxEntity>> allTimeBoxes;

    TimeBoxRepository(Application application) {
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
