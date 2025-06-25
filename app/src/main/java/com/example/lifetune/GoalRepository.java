package com.example.lifetune;

import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.Executors;

public class GoalRepository {
    private final GoalDao goalDao;
    private final LiveData<List<GoalItem>> allGoals;

    public GoalRepository(GoalDao goalDao) {
        this.goalDao = goalDao;
        this.allGoals = goalDao.getAllGoals();
    }
    public void updateGoal(GoalItem goal) {
        Executors.newSingleThreadExecutor().execute(() -> {
            goalDao.update(goal);
        });
    }
    public LiveData<List<GoalItem>> getAllGoals() {
        return allGoals;
    }

    public LiveData<List<GoalItem>> getRecentGoals(int limit) {
        return goalDao.getRecentGoals(limit);
    }

    public void insert(GoalItem goal) {
        new Thread(() -> goalDao.insert(goal)).start();
    }

    public void update(GoalItem goal) {
        new Thread(() -> goalDao.update(goal)).start();
    }

    public void delete(GoalItem goal) {
        new Thread(() -> goalDao.delete(goal)).start();
    }

    public void updateCategory(String id, String category) {
        new Thread(() -> goalDao.updateCategory(id, category)).start();
    }

    public void updateRepetition(String id, String repetition) {
        new Thread(() -> goalDao.updateRepetition(id, repetition)).start();
    }

    public void deleteAllGoals() {
        new Thread(() -> goalDao.deleteAllGoals()).start();
    }
}
