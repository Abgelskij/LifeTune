package com.example.lifetune;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GoalPreviewAdapter extends RecyclerView.Adapter<GoalPreviewAdapter.GoalPreviewViewHolder> {
    private List<GoalItem> goals;
    private OnGoalCheckedListener listener;

    public interface OnGoalCheckedListener {
        void onGoalChecked(GoalItem goal, boolean isChecked);
    }
    public GoalPreviewAdapter(List<GoalItem> goals, OnGoalCheckedListener listener) {
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_preview, parent, false);
        return new GoalPreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalPreviewViewHolder holder, int position) {
        GoalItem goal = goals.get(position);
        holder.title.setText(goal.getTitle());
        holder.checkBox.setChecked(goal.isCompleted());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onGoalChecked(goal, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public void updateGoals(List<GoalItem> newGoals) {
        goals = newGoals;
        notifyDataSetChanged();
    }

    static class GoalPreviewViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView title;

        public GoalPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cb_goal);
            title = itemView.findViewById(R.id.goal_title);
        }
    }
}