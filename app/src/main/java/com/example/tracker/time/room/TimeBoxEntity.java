package com.example.tracker.time.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "time_box_table")
public class TimeBoxEntity {

    @PrimaryKey
    @ColumnInfo(name = "started")
    private final long timeStartedInMilliseconds;
    @ColumnInfo(name = "duration")
    private final long durationInMilliseconds;
    @ColumnInfo(name = "task")
    private final String task;

    public TimeBoxEntity(long timeStartedInMilliseconds, long durationInMilliseconds, @NonNull String task) {
        this.timeStartedInMilliseconds = timeStartedInMilliseconds;
        this.durationInMilliseconds = durationInMilliseconds;
        this.task = task;
    }

    public long getTimeStartedInMilliseconds() {
        return timeStartedInMilliseconds;
    }

    public long getDurationInMilliseconds() {
        return durationInMilliseconds;
    }

    public String getTask() {
        return task;
    }
}
