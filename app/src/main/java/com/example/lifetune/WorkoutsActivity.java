package com.example.lifetune;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class WorkoutsActivity extends AppCompatActivity {

    private RecyclerView workoutsRecyclerView;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        // Инициализация элементов UI
        workoutsRecyclerView = findViewById(R.id.workouts_recycler_view);
        FloatingActionButton fabAddWorkout = findViewById(R.id.fab_add_workout);

        // Настройка RecyclerView
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutAdapter = new WorkoutAdapter(workoutList);
        workoutsRecyclerView.setAdapter(workoutAdapter);

        // Загрузка данных
        loadWorkouts();

        // Обработчик добавления новой тренировки
        fabAddWorkout.setOnClickListener(view -> {
            showWorkoutDialog(null); // null означает новую тренировку
        });
    }

    private void loadWorkouts() {
        // Здесь должна быть логика загрузки тренировок из базы данных
        // Временные данные для примера:
        workoutList.clear();
        workoutList.add(new Workout("Morning Cardio", "15 min run, 10 min stretching", "Mon, Wed, Fri"));
        workoutList.add(new Workout("Full Body Workout", "Push-ups, Squats, Plank", "Tue, Thu"));
        workoutList.add(new Workout("Yoga Routine", "Sun salutation, Warrior poses", "Daily"));
        workoutAdapter.notifyDataSetChanged();
    }

    private void showWorkoutDialog(Workout workout) {
        // Реализация диалога для создания/редактирования тренировки
        new WorkoutDialog(this, workout, new WorkoutDialog.WorkoutDialogListener() {
            @Override
            public void onWorkoutSaved(Workout workout) {
                // Сохранение тренировки и обновление списка
                if (workout.getId() == 0) {
                    // Новая тренировка
                    workoutList.add(workout);
                } else {
                    // Обновление существующей
                    for (int i = 0; i < workoutList.size(); i++) {
                        if (workoutList.get(i).getId() == workout.getId()) {
                            workoutList.set(i, workout);
                            break;
                        }
                    }
                }
                workoutAdapter.notifyDataSetChanged();
            }
        }).show();
    }
}