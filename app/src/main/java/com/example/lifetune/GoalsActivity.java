package com.example.lifetune;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

public class GoalsActivity extends AppCompatActivity {
    private GoalAdapter adapter;
    private GoalViewModel goalViewModel;
    private EditText etGoalText;
    private ImageButton btnCategoryMeditation, btnCategoryRead, btnCategorySleep, btnCategoryHealth, btnCategorySport;
    private RadioGroup radioGroupRepetition;
    private Button btnAddGoal, btnShowAddGoal;
    private String selectedCategory = null;
    private String selectedRepetition = "Daily";
    private Toolbar toolbar;
    private TextView tvNoGoals;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        // Инициализация ViewModel (один раз!)
        GoalDao goalDao = GoalDatabase.getDatabase(this).goalDao();
        GoalRepository repository = new GoalRepository(goalDao);
        goalViewModel = new ViewModelProvider(this,
                new GoalViewModel.Factory(repository)).get(GoalViewModel.class);

        // Настройка RecyclerView (используем rv_goals)
        RecyclerView recyclerView = findViewById(R.id.rv_goals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Добавляем аниматор
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setRemoveDuration(300);
        recyclerView.setItemAnimator(animator);

        // Инициализация адаптера
        adapter = new GoalAdapter(new ArrayList<>(), new GoalAdapter.OnGoalActionListener() {
            @Override
            public void onGoalChecked(GoalItem goal, boolean isChecked) {
                goal.setCompleted(isChecked);
                goalViewModel.update(goal);
            }

            @Override
            public void onGoalDeleted(GoalItem goal) {
                goalViewModel.delete(goal);
                Toast.makeText(GoalsActivity.this, "Цель удалена!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Подписка на данные
        TextView tvNoGoals = findViewById(R.id.tv_no_goals);
        goalViewModel.getAllGoals().observe(this, goals -> {
            adapter.updateGoals(goals);
            if (goals.isEmpty()) {
                tvNoGoals.setVisibility(View.VISIBLE); // Показываем сообщение
            } else {
                tvNoGoals.setVisibility(View.GONE); // Скрываем сообщение
            }
        });

        // Добавляем ItemTouchHelper для свайпа
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, // Не поддерживаем перетаскивание
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT // Разрешаем свайп в обе стороны
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);
            }
        }).attachToRecyclerView(recyclerView);

        // Инициализация остальных элементов
        initViews();
        setupClickListeners();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void initViews() {
        etGoalText = findViewById(R.id.et_goal_text);
        btnCategoryMeditation = findViewById(R.id.btn_category_meditation);
        btnCategoryRead = findViewById(R.id.btn_category_read);
        btnCategorySleep = findViewById(R.id.btn_category_sleep);
        btnCategoryHealth = findViewById(R.id.btn_category_health);
        btnCategorySport = findViewById(R.id.btn_category_sport);
        radioGroupRepetition = findViewById(R.id.radio_group_repetition);
        btnAddGoal = findViewById(R.id.btn_add_goal);
        btnShowAddGoal = findViewById(R.id.btn_show_add_goal);
        tvNoGoals = findViewById(R.id.tv_no_goals);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_goals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Добавляем аниматор для плавного удаления
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setRemoveDuration(300);
        recyclerView.setItemAnimator(animator);

        GoalDao goalDao = GoalDatabase.getDatabase(this).goalDao();
        GoalRepository repository = new GoalRepository(goalDao);
        goalViewModel = new ViewModelProvider(this,
                new GoalViewModel.Factory(repository)).get(GoalViewModel.class);

        adapter = new GoalAdapter(new ArrayList<>(), new GoalAdapter.OnGoalActionListener() {
            @Override
            public void onGoalChecked(GoalItem goal, boolean isChecked) {
                goal.setCompleted(isChecked);
                goalViewModel.update(goal);
            }

            @Override
            public void onGoalDeleted(GoalItem goal) {
                goalViewModel.delete(goal);
                Toast.makeText(GoalsActivity.this, "Цель удалена!", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        goalViewModel.getAllGoals().observe(this, goals -> {
            adapter.updateGoals(goals);
        });

        // Добавляем ItemTouchHelper для свайпа
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Не разрешаем перетаскивание
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);
            }
        }).attachToRecyclerView(recyclerView);
    }
    private void showDeleteConfirmationDialog(final int position) {
        final Dialog dialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_delete_dialog, null);
        dialog.setContentView(dialogView);

        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        MaterialButton deleteButton = dialogView.findViewById(R.id.dialog_delete_button);

        titleTextView.setText("Удалить цель?");
        messageTextView.setText("Вы уверены, что хотите удалить цель?");

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            adapter.notifyItemChanged(position); // Вернуть элемент в исходное состояние
        });

        deleteButton.setOnClickListener(v -> {
            GoalItem goalToDelete = adapter.getGoals().get(position);
            goalViewModel.delete(goalToDelete);
            Toast.makeText(GoalsActivity.this, "Цель удалена!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Установим прозрачный фон для окна диалога (только для фона вокруг карточки)
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }

    private void setupClickListeners() {

        View.OnClickListener categoryListener = v -> {
            resetCategoryButtons();
            v.setSelected(true);

            // Добавляем визуальную обратную связь
            if (v == btnCategoryMeditation) {
                selectedCategory = "meditation"; // Важно: используйте те же значения, что и в адаптере
                btnCategoryMeditation.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            } else if (v == btnCategoryRead) {
                selectedCategory = "read";
                btnCategoryRead.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
} else if (v == btnCategorySleep) {
                selectedCategory = "sleep";
                btnCategorySleep.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            } else if (v == btnCategoryHealth) {
                selectedCategory = "health";
                btnCategoryHealth.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            } else if (v == btnCategorySport) {
                selectedCategory = "sport";
                btnCategorySport.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            }
        };

        Button btnShowAddGoal = findViewById(R.id.btn_show_add_goal);
        androidx.cardview.widget.CardView addGoalCard = findViewById(R.id.add_goal_card);
        btnShowAddGoal.setOnClickListener(v -> {
            addGoalCard.setVisibility(View.VISIBLE); // Показываем карточку
            btnShowAddGoal.setVisibility(View.GONE); // Скрываем кнопку
        });

        btnCategoryMeditation.setOnClickListener(categoryListener);
        btnCategoryRead.setOnClickListener(categoryListener);
        btnCategorySleep.setOnClickListener(categoryListener);
        btnCategoryHealth.setOnClickListener(categoryListener);
        btnCategorySport.setOnClickListener(categoryListener);

        // Обработчик повторения
        radioGroupRepetition.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_daily) {
                selectedRepetition = "День";
            } else if (checkedId == R.id.radio_weekly) {
                selectedRepetition = "Неделя";
            } else if (checkedId == R.id.radio_one_time) {
                selectedRepetition = "Месяц";
            }
        });

        // Обработчик добавления цели
        btnAddGoal.setOnClickListener(v -> {
            addNewGoal();

        });
    }

    private void resetCategoryButtons() {
        btnCategoryMeditation.setSelected(false);
        btnCategoryRead.setSelected(false);
        btnCategorySleep.setSelected(false);
        btnCategoryHealth.setSelected(false);
        btnCategorySport.setSelected(false);

        // Сбрасываем подсветку
        btnCategoryMeditation.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btnCategoryRead.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btnCategorySleep.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btnCategoryHealth.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btnCategorySport.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
    }

    private void addNewGoal() {
        String goalText = etGoalText.getText().toString().trim();
        if (!goalText.isEmpty()) {
            if (selectedCategory == null) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            GoalItem newGoal = new GoalItem(
                    UUID.randomUUID().toString(), // ID
                    goalText,
                    false, // completed
                    selectedCategory, // категория
                    selectedRepetition, // повторение
                    System.currentTimeMillis() // время создания
            );

            goalViewModel.insert(newGoal);
            clearInputFields();

            androidx.cardview.widget.CardView addGoalCard = findViewById(R.id.add_goal_card);
            Button btnShowAddGoal = findViewById(R.id.btn_show_add_goal);
            addGoalCard.setVisibility(View.GONE);
            btnShowAddGoal.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Please enter a goal", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkOverdueGoals();
    }

    private void checkOverdueGoals() {
        new Thread(() -> {
            List<GoalItem> goals = goalViewModel.getAllGoals().getValue();
            if (goals != null) {
                for (GoalItem goal : goals) {
                    if (goal.isOverdue() && !goal.isCompleted()) {
                        goalViewModel.update(goal);
                    }
                }
            }
        }).start();
    }
    private void clearInputFields() {
        etGoalText.setText("");
        selectedCategory = null;
        resetCategoryButtons();
        radioGroupRepetition.check(R.id.radio_daily);
    }
}