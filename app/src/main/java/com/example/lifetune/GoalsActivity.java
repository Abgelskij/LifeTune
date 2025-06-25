package com.example.lifetune;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


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
    private NotificationHelper notificationHelper;
    private Handler progressHandler;
    private Runnable progressUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);
        progressHandler = new Handler(Looper.getMainLooper());

        progressUpdater = new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.notifyDataSetChanged(); // Обновляем прогресс
                }
                progressHandler.postDelayed(this, TimeUnit.MINUTES.toMillis(1)); // Обновляем каждую минуту
            }
        };

        // Инициализация ViewModel (один раз!)
        GoalDao goalDao = GoalDatabase.getDatabase(this).goalDao();
        GoalRepository repository = new GoalRepository(goalDao);
        goalViewModel = new ViewModelProvider(this,
                new GoalViewModel.Factory(repository)).get(GoalViewModel.class);
        
        // Инициализация NotificationHelper
        notificationHelper = new NotificationHelper(this);

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
                notificationHelper.showGoalDeletedNotification();
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
                notificationHelper.showGoalDeletedNotification();
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
            notificationHelper.showGoalDeletedNotification();
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

        // Добавление подсказок для категорий
        View.OnLongClickListener tooltipListener = v -> {
            String message = "";
            if (v == btnCategoryMeditation) {
                message = "Медитация";
            } else if (v == btnCategoryRead) {
                message = "Чтение";
            } else if (v == btnCategorySleep) {
                message = "Сон";
            } else if (v == btnCategoryHealth) {
                message = "Здоровье";
            } else if (v == btnCategorySport) {
                message = "Спорт";
            }
if (!message.isEmpty()) {
                showCustomTooltip(v, message);
            }
            return true;
        };

        btnCategoryMeditation.setOnLongClickListener(tooltipListener);
        btnCategoryRead.setOnLongClickListener(tooltipListener);
        btnCategorySleep.setOnLongClickListener(tooltipListener);
        btnCategoryHealth.setOnLongClickListener(tooltipListener);
        btnCategorySport.setOnLongClickListener(tooltipListener);

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

        // Обработчик кнопки удаления всех целей
        ImageButton btnDeleteAllGoals = findViewById(R.id.btn_delete_all_goals);
        btnDeleteAllGoals.setOnClickListener(v -> {
            showDeleteAllGoalsConfirmationDialog();
        });
    }

    private void showCustomTooltip(View anchorView, String message) {
        if (anchorView == null) return;

        // 1. Создаем и настраиваем View
        View tooltipView = LayoutInflater.from(this).inflate(R.layout.tooltip_layout, null);
        TextView textView = tooltipView.findViewById(R.id.tooltip_text);
        textView.setText(message != null ? message : "Подсказка");

        // 2. Обязательно делаем предварительное измерение
        tooltipView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        // 3. Настройка PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                tooltipView,
                tooltipView.getMeasuredWidth(), // Используем измеренные размеры
                tooltipView.getMeasuredHeight(),
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        // 4. Позиционирование
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int xPos = location[0] + (anchorView.getWidth() - tooltipView.getMeasuredWidth()) / 2;
        int yPos = location[1] - tooltipView.getMeasuredHeight() - dpToPx(8);

        // 5. Начальное состояние для анимации
        tooltipView.setAlpha(0f);
        tooltipView.setScaleX(0.5f);
        tooltipView.setScaleY(0.5f);
        tooltipView.setTranslationY(dpToPx(20)); // Начальное смещение вниз

        // 6. Показываем и анимируем
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, yPos);

        tooltipView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(0.5f))
                .start();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
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
                // Можно добавить визуальную индикацию необходимости выбора категории
                // или просто присвоить дефолтную категорию
                selectedCategory = "meditation"; // Дефолтная категория
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
            // Визуальная индикация необходимости ввода текста
            etGoalText.setError("Введите название цели");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkOverdueGoals();
        progressHandler.post(progressUpdater);
    }
    @Override
    protected void onPause() {
        super.onPause();
        progressHandler.removeCallbacks(progressUpdater); // Останавливаем при паузе
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

    private void showDeleteAllGoalsConfirmationDialog() {
        final Dialog dialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_delete_dialog, null);
        dialog.setContentView(dialogView);

        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        MaterialButton deleteButton = dialogView.findViewById(R.id.dialog_delete_button);

        titleTextView.setText("Удалить все цели?");
        messageTextView.setText("Вы уверены, что хотите удалить все цели? Это действие нельзя отменить.");

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            dialog.dismiss();
            
            // Запускаем анимацию удаления
            RecyclerView recyclerView = findViewById(R.id.rv_goals);
            adapter.animateDeleteAll(recyclerView, () -> {
                // После завершения анимации удаляем из базы данных
                goalViewModel.deleteAllGoals();
                notificationHelper.showAllGoalsDeletedNotification();
            });
        });

        // Установим прозрачный фон для окна диалога
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }
}
