package com.example.lifetune;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SleepRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String bedtime;
    public String wakeupTime;
    public long date;
}