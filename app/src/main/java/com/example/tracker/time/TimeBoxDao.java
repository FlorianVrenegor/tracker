package com.example.tracker.time;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TimeBoxDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TimeBoxEntity entity);
    @Delete
    void delete(TimeBoxEntity entity);
//    @Query("DELETE FROM time_box_table")
//    void deleteAll();
    @Query("SELECT * FROM time_box_table ORDER BY started DESC")
    LiveData<List<TimeBoxEntity>> getAllEntities();
}
