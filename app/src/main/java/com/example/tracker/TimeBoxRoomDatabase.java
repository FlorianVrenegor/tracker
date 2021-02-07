package com.example.tracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {TimeBoxEntity.class}, version = 1, exportSchema = false)
public abstract class TimeBoxRoomDatabase extends RoomDatabase {

    public abstract TimeBoxDao timeBoxDao();

    private static volatile TimeBoxRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static TimeBoxRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TimeBoxRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TimeBoxRoomDatabase.class, "time_box_room_database").addCallback(roomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                TimeBoxDao dao = INSTANCE.timeBoxDao();

                TimeBoxEntity timeBox = new TimeBoxEntity(0, 30 * 60 * 1000, "Task 1");
                dao.insert(timeBox);
                timeBox = new TimeBoxEntity(1000, 30 * 60 * 1000, "Task 2");
                dao.insert(timeBox);
            });
        }
    };
}
