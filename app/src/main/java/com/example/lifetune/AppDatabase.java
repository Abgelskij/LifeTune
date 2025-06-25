package com.example.lifetune;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SleepRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SleepDao sleepDao();
}