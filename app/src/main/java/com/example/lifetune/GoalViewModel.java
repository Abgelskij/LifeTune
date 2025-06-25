package com.example.lifetune;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GoalViewModel extends ViewModel {
    private final GoalRepository repository;
    private final LiveData<List<GoalItem>> allGoals;


    private final MutableLiveData<Boolean> refreshTrigger = new MutableLiveData<>();


    public LiveData<Boolean> getRefreshTrigger() {
        return refreshTrigger;
    }

    public void refreshGoals() {
        refreshTrigger.postValue(true);
    }

    // Обновляем метод getRecentGoals
    public LiveData<List<GoalItem>> getRecentGoals(int limit) {
        return Transformations.switchMap(refreshTrigger, trigger ->
                repository.getRecentGoals(limit)
        );
    }
    public GoalViewModel(@NonNull GoalRepository repository) {
        this.repository = repository;
        this.allGoals = repository.getAllGoals();
    }

    public LiveData<List<GoalItem>> getAllGoals() {
        return allGoals;
    }



    public void resetRefreshTrigger() {
        refreshTrigger.setValue(false);
    }

    public void insert(GoalItem goal) {
        repository.insert(goal);
    }

    public void update(GoalItem goal) {
        repository.update(goal);
    }

    public void delete(GoalItem goal) {
        repository.delete(goal);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final GoalRepository repository;

        public Factory(GoalRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GoalViewModel.class)) {
                @SuppressWarnings("unchecked")
                T result = (T) new GoalViewModel(repository);
                return result;
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public void updateCategory(String id, String category) {
        repository.updateCategory(id, category);
    }

    public void updateRepetition(String id, String repetition) {
        repository.updateRepetition(id, repetition);
    }
}