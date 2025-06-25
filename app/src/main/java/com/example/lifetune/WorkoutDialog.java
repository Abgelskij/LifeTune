package com.example.lifetune;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;

public class WorkoutDialog extends Dialog {

    public interface WorkoutDialogListener {
        void onWorkoutSaved(Workout workout);
    }

    private Workout workout;
    private WorkoutDialogListener listener;
    private EditText titleEditText, descriptionEditText, scheduleEditText;

    public WorkoutDialog(@NonNull Context context, Workout workout, WorkoutDialogListener listener) {
        super(context);
        this.workout = workout;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_workout);

        titleEditText = findViewById(R.id.edit_workout_title);
        descriptionEditText = findViewById(R.id.edit_workout_description);
        scheduleEditText = findViewById(R.id.edit_workout_schedule);
        Button saveButton = findViewById(R.id.btn_save_workout);
        Button cancelButton = findViewById(R.id.btn_cancel);

        if (workout != null) {
            titleEditText.setText(workout.getTitle());
            descriptionEditText.setText(workout.getDescription());
            scheduleEditText.setText(workout.getSchedule());
        }

        saveButton.setOnClickListener(v -> saveWorkout());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void saveWorkout() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String schedule = scheduleEditText.getText().toString().trim();

        if (title.isEmpty()) {
            ((TextInputLayout) findViewById(R.id.title_input_layout)).setError("Title is required");
            return;
        }

        if (workout == null) {
            workout = new Workout(title, description, schedule);
        } else {
            workout.setTitle(title);
            workout.setDescription(description);
            workout.setSchedule(schedule);
        }

        listener.onWorkoutSaved(workout);
        dismiss();
    }
}