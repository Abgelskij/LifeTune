package com.example.lifetune;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SleepDao {
    @Insert
    void insert(SleepRecord record);

    @Query("SELECT * FROM SleepRecord ORDER BY date DESC LIMIT :limit")
    List<SleepRecord> getLastRecords(int limit);
}