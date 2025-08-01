package com.example.lifetune;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<GoalItem> goals;
    private final OnGoalActionListener listener;
    private boolean isDeleteMode = false;

    public interface OnGoalActionListener {
        void onGoalChecked(GoalItem goal, boolean isChecked);
        void onGoalDeleted(GoalItem goal);
    }

    public GoalAdapter(List<GoalItem> goals, OnGoalActionListener listener) {
        this.goals = new ArrayList<>(goals);
        this.listener = listener;
    }

    public void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;
        notifyDataSetChanged();
    }

    public List<GoalItem> getGoals() {
        return goals;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalItem goal = goals.get(position);

        // Установка параметров карточки
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.setMargins(16, 8, 16, 8);
        holder.itemView.setLayoutParams(layoutParams);

        // Установка данных цели
        holder.textView.setText(goal.getTitle());
        holder.checkBox.setChecked(goal.isCompleted());

        String repetitionText = getRepetitionText(goal.getRepetition());
        holder.repetitionTextView.setText(repetitionText);

        // Установка иконки категории
        switch(goal.getCategory()) {
            case "meditation":
                holder.categoryIcon.setImageResource(R.drawable.lotus);
                holder.categoryIcon.setVisibility(View.VISIBLE);
                break;
            case "read":
                holder.categoryIcon.setImageResource(R.drawable.book_open_svgrepo_com);
                holder.categoryIcon.setVisibility(View.VISIBLE);
                break;
            case "sleep":
                holder.categoryIcon.setImageResource(R.drawable.moon_svgrepo_com);
                holder.categoryIcon.setVisibility(View.VISIBLE);
                break;
            case "health":
                holder.categoryIcon.setImageResource(R.drawable.icon_health);
                holder.categoryIcon.setVisibility(View.VISIBLE);
                break;
            case "sport":
                holder.categoryIcon.setImageResource(R.drawable.icon_sport);
                holder.categoryIcon.setVisibility(View.VISIBLE);
                break;
            default:
                holder.categoryIcon.setVisibility(View.GONE);
        }

        // Рассчитываем текущий прогресс
        int currentProgress = goal.calculateProgress();

        // Установка статуса цели и прогресс-бара
        if (goal.isCompleted()) {
            holder.statusTextView.setText("Завершено");
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.green)));
            holder.progressBar.setProgress(100);
        } else if (goal.isOverdue()) {
            holder.statusTextView.setText("Просрочено");
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red)));
            holder.progressBar.setProgress(100);
        } else {
            holder.statusTextView.setText("В процессе (" + goal.getRemainingTime() + ")");
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.orange)));
            holder.progressBar.setProgress(currentProgress);
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            goal.setCompleted(isChecked);
            if (listener != null) {
                listener.onGoalChecked(goal, isChecked);
            }

            if (isChecked) {
                holder.statusTextView.setText("Завершено");
                holder.statusTextView.setTextColor(ContextCompat.getColor(
                        holder.itemView.getContext(), R.color.green));

                // Анимация изменения цвета
                ValueAnimator colorAnimation = ValueAnimator.ofArgb(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.orange),
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.green)
                );
                colorAnimation.setDuration(300);
                colorAnimation.addUpdateListener(animator -> {
                    holder.progressBar.setProgressTintList(
                            ColorStateList.valueOf((Integer) animator.getAnimatedValue())
                    );
                });
                colorAnimation.start();

                // Анимация заполнения прогресса
                ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                        holder.progressBar, "progress", holder.progressBar.getProgress(), 100
                );
                progressAnimator.setDuration(300);
                progressAnimator.start();

            } else {
                int progress = goal.calculateProgress();
                holder.statusTextView.setText("В процессе (" + goal.getRemainingTime() + ")");
                holder.statusTextView.setTextColor(ContextCompat.getColor(
                        holder.itemView.getContext(), R.color.orange));

                // Анимация изменения цвета
                ValueAnimator colorAnimation = ValueAnimator.ofArgb(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.green),
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.orange)
                );
                colorAnimation.setDuration(300);
                colorAnimation.addUpdateListener(animator -> {
                    holder.progressBar.setProgressTintList(
                            ColorStateList.valueOf((Integer) animator.getAnimatedValue())
                    );
                });
                colorAnimation.start();

                // Анимация заполнения прогресса
                ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                        holder.progressBar, "progress", holder.progressBar.getProgress(), progress
                );
                progressAnimator.setDuration(300);
                progressAnimator.start();
            }
        });
    }

    private String getRepetitionText(String repetition) {
        if (repetition == null) return "";
        switch (repetition) {
            case "Daily": return "Every day";
            case "Weekly": return "7 days";
            case "One time": return "One time only";
            default: return repetition;
        }
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public void updateGoals(List<GoalItem> newGoals) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new GoalDiffCallback(goals, newGoals));
        goals.clear();
        goals.addAll(newGoals);
        diffResult.dispatchUpdatesTo(this);
    }

    public void removeItem(int position) {
        GoalItem removedItem = goals.remove(position);
        notifyItemRemoved(position);
        if (listener != null) {
            listener.onGoalDeleted(removedItem);
        }
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkBox;
        final TextView textView;
        final TextView repetitionTextView;
        final ImageView categoryIcon;
        final TextView statusTextView;
        final ProgressBar progressBar;
        final GoalAdapter adapter;

        public GoalViewHolder(@NonNull View itemView, GoalAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            checkBox = itemView.findViewById(R.id.cb_goal);
            textView = itemView.findViewById(R.id.tv_goal_text);
            repetitionTextView = itemView.findViewById(R.id.tv_repetition);
            categoryIcon = itemView.findViewById(R.id.iv_category_icon);
            statusTextView = itemView.findViewById(R.id.tv_status);
            progressBar = itemView.findViewById(R.id.progress_bar);
            itemView.setOnLongClickListener(v -> {
                adapter.toggleDeleteMode();
                return true;
            });
        }
    }

    static class GoalDiffCallback extends DiffUtil.Callback {
        private final List<GoalItem> oldList;
        private final List<GoalItem> newList;

        GoalDiffCallback(List<GoalItem> oldList, List<GoalItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).getId().equals(newList.get(newPos).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).equals(newList.get(newPos));
        }
    }

    public void animateDeleteAll(RecyclerView recyclerView, Runnable onAnimationComplete) {
        if (goals.isEmpty()) {
            if (onAnimationComplete != null) {
                onAnimationComplete.run();
            }
            return;
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        final int totalItems = getItemCount();
        final int[] animatedCount = {0};

        for (int i = 0; i < totalItems; i++) {
            final int position = i;
            handler.postDelayed(() -> {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                
                if (viewHolder != null && viewHolder.itemView != null) {
                    Animation slideOut = AnimationUtils.loadAnimation(
                        viewHolder.itemView.getContext(), R.anim.slide_out_right);
                    
                    slideOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            animatedCount[0]++;
                            if (animatedCount[0] == totalItems && onAnimationComplete != null) {
                                onAnimationComplete.run();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    
                    viewHolder.itemView.startAnimation(slideOut);
                } else {
                    animatedCount[0]++;
                    if (animatedCount[0] == totalItems && onAnimationComplete != null) {
                        onAnimationComplete.run();
                    }
                }
            }, i * 80); // Задержка 80мс между анимациями элементов
        }
    }
}
