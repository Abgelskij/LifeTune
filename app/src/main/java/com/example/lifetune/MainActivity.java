package com.example.lifetune;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.app.AlarmManager;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private GoalViewModel goalViewModel;
    private TextView sleepGoalTextView, sleepTimeTextView, sleepDurationTextView;
    private SwitchCompat alarmSwitch;
    private AlarmManager alarmManager;
    private GoalPreviewAdapter goalsPreviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Инициализация View
        sleepTimeTextView = findViewById(R.id.sleep_time);
        sleepGoalTextView = findViewById(R.id.sleep_goal_preview);
        alarmSwitch = findViewById(R.id.alarm_switch);
        loadSleepData();
        setupAlarmSwitch();

        // Инициализация ViewModel
        GoalDao goalDao = GoalDatabase.getDatabase(this).goalDao();
        GoalRepository repository = new GoalRepository(goalDao);
        goalViewModel = new ViewModelProvider(this,
                new GoalViewModel.Factory(repository)).get(GoalViewModel.class);

        // Настройка RecyclerView для превью целей
        RecyclerView rvGoalsPreview = findViewById(R.id.rv_goals_preview);
        rvGoalsPreview.setLayoutManager(new LinearLayoutManager(this));
        rvGoalsPreview.setHasFixedSize(true);

        // Инициализация адаптера с обработчиком чекбоксов
        goalsPreviewAdapter = new GoalPreviewAdapter(new ArrayList<>(), (goal, isChecked) -> {
            // Обновляем цель в базе данных
            goal.setCompleted(isChecked);
            goalViewModel.update(goal);

            // Логирование для отладки
            Log.d("MainActivity", "Goal updated - ID: " + goal.getId() +
                    ", Checked: " + isChecked);
        });

        rvGoalsPreview.setAdapter(goalsPreviewAdapter);

        // Подписка на изменения последних целей
        goalViewModel.getRecentGoals(3).observe(this, goals -> {
            if (goals != null) {
                Log.d("MainActivity", "Goals loaded: " + goals.size());
                goalsPreviewAdapter.updateGoals(goals);
            } else {
                Log.d("MainActivity", "No goals found");
            }
        });

        // Обработчик клика на карточку целей
        MaterialCardView goalsCard = findViewById(R.id.card_goals);
        goalsCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GoalsActivity.class));
        });

        MaterialCardView sleepCard = findViewById(R.id.sleep_card);
        sleepCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SleepDetailActivity.class));
        });

        MaterialCardView workoutsCard = findViewById(R.id.workouts_card);
        workoutsCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, WorkoutsActivity.class));
        });
    }

    private void setupAlarmSwitch() {
        // Проверяем состояние будильников при запуске
        boolean alarmsEnabled = checkAlarmsEnabled();
        alarmSwitch.setChecked(alarmsEnabled);

        // Обработчик изменения состояния переключателя
        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleAlarms(isChecked);
            saveAlarmState(isChecked);
        });
    }

    private boolean checkAlarmsEnabled() {
        SharedPreferences prefs = getSharedPreferences("SleepAlarmPrefs", MODE_PRIVATE);
        return prefs.getBoolean("alarms_enabled", true);
    }

    private void toggleAlarms(boolean enable) {
        // Отменяем или восстанавливаем будильники
        if (enable) {
            // Восстанавливаем будильники из SharedPreferences
            loadSleepData();
        } else {
            // Отменяем все будильники
            cancelAlarms();
        }
    }

    private void cancelAlarms() {
        // Отмена будильника пробуждения
        Intent wakeupIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent wakeupPendingIntent = PendingIntent.getBroadcast(
                this, 1, wakeupIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        if (wakeupPendingIntent != null) {
            alarmManager.cancel(wakeupPendingIntent);
            wakeupPendingIntent.cancel();
        }

        // Отмена напоминания о сне
        Intent bedtimeIntent = new Intent(this, ReminderReceiver.class);
        PendingIntent bedtimePendingIntent = PendingIntent.getBroadcast(
                this, 0, bedtimeIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        if (bedtimePendingIntent != null) {
            alarmManager.cancel(bedtimePendingIntent);
            bedtimePendingIntent.cancel();
        }
    }

    private void saveAlarmState(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences("SleepAlarmPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("alarms_enabled", enabled).apply();
    }

    private void loadSleepData() {
        SharedPreferences prefs = getSharedPreferences("SleepAlarmPrefs", MODE_PRIVATE);

        // Загружаем время сна
        int bedtimeHour = prefs.getInt("bedtime_hour", 22);
        int bedtimeMinute = prefs.getInt("bedtime_minute", 45);
        int wakeupHour = prefs.getInt("wakeup_hour", 7);
        int wakeupMinute = prefs.getInt("wakeup_minute", 15);

        // Форматируем время в 12-часовой формат как в макете
        String bedtimeStr = formatTime24Hour(bedtimeHour, bedtimeMinute);
        String wakeupStr = formatTime24Hour(wakeupHour, wakeupMinute);
        if (sleepTimeTextView != null) {
            sleepTimeTextView.setText(String.format("%s — %s", bedtimeStr, wakeupStr));
        }

        // Рассчитываем продолжительность сна
        int sleepHours = wakeupHour - bedtimeHour;
        if (sleepHours < 0) sleepHours += 24;
        int sleepMinutes = wakeupMinute - bedtimeMinute;

        if (sleepMinutes < 0) {
            sleepHours--;
            sleepMinutes += 60;
        }
        if (sleepGoalTextView != null) {
            sleepGoalTextView.setText(String.format("%d hr %d min", sleepHours, sleepMinutes));
        }
    }

    private String formatTime24Hour(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }



    private String formatSleepTime(int bedtimeHour, int bedtimeMinute, int wakeupHour, int wakeupMinute) {
        return String.format(Locale.getDefault(), "%02d:%02d — %02d:%02d",
                bedtimeHour, bedtimeMinute, wakeupHour, wakeupMinute);
    }

    private String formatSleepDuration(long durationMillis) {
        long hours = durationMillis / (1000 * 60 * 60);
        long minutes = (durationMillis / (1000 * 60)) % 60;

        return String.format(Locale.getDefault(), "%d hr %d min", hours, minutes);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран обновляем данные
        loadSleepData();
        goalViewModel.refreshGoals();
    }
}
