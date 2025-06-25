package com.example.lifetune;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface GoalDao {
    @Insert
    void insert(GoalItem goal);

    @Update
    void update(GoalItem goal);


    @Delete
    void delete(GoalItem goal);

    @Query("DELETE FROM goals")
    void deleteAllGoals();

    @Query("SELECT * FROM goals ORDER BY text ASC")
    LiveData<List<GoalItem>> getAllGoals();

    @Query("SELECT * FROM goals WHERE completed = 0 ORDER BY text ASC")
    LiveData<List<GoalItem>> getActiveGoals();

    @Query("SELECT * FROM goals ORDER BY id DESC LIMIT :limit")
    LiveData<List<GoalItem>> getRecentGoals(int limit);

    @Query("UPDATE goals SET category = :category WHERE id = :id")
    void updateCategory(String id, String category);

    @Query("UPDATE goals SET repetition = :repetition WHERE id = :id")
    void updateRepetition(String id, String repetition);


}