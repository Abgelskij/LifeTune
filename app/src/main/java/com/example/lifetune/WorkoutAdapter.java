package com.example.lifetune;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;

    public WorkoutAdapter(List<Workout> workoutList) {
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView scheduleTextView;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.workout_title);
            descriptionTextView = itemView.findViewById(R.id.workout_description);
            scheduleTextView = itemView.findViewById(R.id.workout_schedule);
        }

        public void bind(Workout workout) {
            titleTextView.setText(workout.getTitle());
            descriptionTextView.setText(workout.getDescription());
            scheduleTextView.setText(workout.getSchedule());
        }
    }
}