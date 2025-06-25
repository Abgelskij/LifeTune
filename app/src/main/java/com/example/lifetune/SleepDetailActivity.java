package com.example.lifetune;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.view.Window;
public class SleepDetailActivity extends AppCompatActivity {
    // В начало класса SleepDetailActivity добавьте:
    private static final String PREFS_NAME = "SleepAlarmPrefs";
    private static final String PREF_BEDTIME_HOUR = "bedtime_hour";
    private static final String PREF_BEDTIME_MINUTE = "bedtime_minute";
    private static final String PREF_WAKEUP_HOUR = "wakeup_hour";
    private static final String PREF_WAKEUP_MINUTE = "wakeup_minute";
    private TextView bedtimeText, wakeupText, timeAwakeText, lastSleepText, sleepRange, currentTime;
    private Calendar wakeupTime, bedtime;
    private AlarmManager alarmManager;
    private AppDatabase db;
    private SleepDao sleepDao;
    private static final String PREF_ALARMS_ENABLED = "alarms_enabled";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Или для обычного ActionBar:
        // getActionBar().hide();
        // Проверяем, не запущены ли мы из-за будильника
        if (getIntent() != null && getIntent().getBooleanExtra("from_alarm", false)) {
            finish();
            return;
        }
        // Инициализация Room Database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sleep-database").allowMainThreadQueries().build();
        sleepDao = db.sleepDao();

        // Инициализация элементов
        initViews();
        setupAlarmManager();
        loadLastSleepTime();
        updateUI();
    }

    private void initViews() {
        bedtimeText = findViewById(R.id.bedtime_time);
        wakeupText = findViewById(R.id.wakeup_time);
        timeAwakeText = findViewById(R.id.time_awake);
        lastSleepText = findViewById(R.id.last_sleep);

        sleepRange = findViewById(R.id.sleep_range); // Инициализация
        currentTime = findViewById(R.id.current_time); // Инициализация

        Button editBedtime = findViewById(R.id.edit_bedtime);
        Button editWakeup = findViewById(R.id.edit_wakeup);

        editBedtime.setOnClickListener(v -> showTimePicker(false));
        editWakeup.setOnClickListener(v -> showTimePicker(true));
    }

    private void setupAlarmManager() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    private void loadLastSleepTime() {
        List<SleepRecord> records = sleepDao.getLastRecords(1);
        if (!records.isEmpty()) {
            SleepRecord lastRecord = records.get(0);
            lastSleepText.setText(String.format("%s - %s",
                    lastRecord.bedtime, lastRecord.wakeupTime));
        } else {
            lastSleepText.setText("Нет данных"); // Дефолтный текст при отсутствии истории
        }
    }

    private void updateUI() {
        // Восстановление сохраненных значений
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean alarmsEnabled = prefs.getBoolean(PREF_ALARMS_ENABLED, true);

        // Обновляем UI в зависимости от состояния будильников
        TextView sleepQualityText = findViewById(R.id.sleep_quality);
        sleepQualityText.setText("Alarms disabled");
        sleepQualityText.setTextColor(Color.parseColor("#FFA500"));

        // Установка значений по умолчанию, если нет сохраненных
        int bedtimeHour = prefs.getInt(PREF_BEDTIME_HOUR, 23); // 23:00 по умолчанию
        int bedtimeMinute = prefs.getInt(PREF_BEDTIME_MINUTE, 0);
        int wakeupHour = prefs.getInt(PREF_WAKEUP_HOUR, 7); // 07:00 по умолчанию
        int wakeupMinute = prefs.getInt(PREF_WAKEUP_MINUTE, 0);

        Calendar now = Calendar.getInstance();

        wakeupTime = Calendar.getInstance();
        wakeupTime.set(Calendar.HOUR_OF_DAY, wakeupHour);
        wakeupTime.set(Calendar.MINUTE, wakeupMinute);

        bedtime = Calendar.getInstance();
        bedtime.set(Calendar.HOUR_OF_DAY, bedtimeHour);
        bedtime.set(Calendar.MINUTE, bedtimeMinute);

        updateTimeDisplays();
        setAlarms();
    }

    private void updateTimeDisplays() {
        // Обновление текстов времени
        wakeupText.setText(formatTime(wakeupTime));
        bedtimeText.setText(formatTime(bedtime));

        // Расчёт бодрствования
        long awakeMillis = calculateAwakeDuration();
        int awakeHours = (int) (awakeMillis / (1000 * 60 * 60));
        int awakeMinutes = (int) ((awakeMillis / (1000 * 60)) % 60);

        // Форматируем вывод более понятно
        String awakeText;
        if (awakeHours > 0) {
            awakeText = String.format(Locale.getDefault(), "%d ч %d мин", awakeHours, awakeMinutes);
        } else {
            awakeText = String.format(Locale.getDefault(), "%d мин", awakeMinutes);
        }
        timeAwakeText.setText(awakeText);

        // Обновление цели сна
        int sleepHours = calculateSleepHours();
        int sleepMinutes = calculateSleepMinutes();
        ((TextView) findViewById(R.id.sleep_goal)).setText(
                String.format("%d hr %d min", sleepHours, sleepMinutes));

        // Оценка качества сна (ИСПРАВЛЕНО - используем calculateSleepDuration())
        float totalSleepHours = (float)calculateSleepDuration() / (1000 * 60 * 60);
        TextView qualityText = findViewById(R.id.sleep_quality);

        if (totalSleepHours < 7f) {
            qualityText.setText("Плохой сон!");
            qualityText.setTextColor(Color.parseColor("#FF0000")); // красный
            qualityText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            qualityText.setText("Отличный сон!");
            qualityText.setTextColor(Color.parseColor("#4CAF50")); // зелёный

            // Устанавливаем иконку слева
            Drawable checkIcon = ContextCompat.getDrawable(this, R.drawable.ic_check_green_circle);
            qualityText.setCompoundDrawablesWithIntrinsicBounds(checkIcon, null, null, null);
            qualityText.setCompoundDrawablePadding(8); // Отступ между иконкой и текстом
        }

        // Отображаем график сна пользователя
        sleepRange.setText(String.format("Сон: %s - %s", formatTime(bedtime), formatTime(wakeupTime)));

        // Получаем текущее время
        Calendar now = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedDate = df.format(now.getTime());

        // Выводим
        currentTime.setText("Текущее время: " + formattedDate);
    }

    private void setAlarms() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean alarmsEnabled = prefs.getBoolean(PREF_ALARMS_ENABLED, true);

        if (alarmsEnabled) {
            setBedtimeReminder();
            setWakeupAlarm();
        } else {
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
    private void saveSleepData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Сохраняем время сна
        editor.putInt("bedtime_hour", bedtime.get(Calendar.HOUR_OF_DAY));
        editor.putInt("bedtime_minute", bedtime.get(Calendar.MINUTE));
        editor.putInt("wakeup_hour", wakeupTime.get(Calendar.HOUR_OF_DAY));
        editor.putInt("wakeup_minute", wakeupTime.get(Calendar.MINUTE));

        editor.apply(); // Сохраняем только время, продолжительность не сохраняем
    }
    private void setBedtimeReminder() {
        // Проверяем, не установлено ли уже такое же напоминание
        boolean reminderAlreadyUp = (PendingIntent.getBroadcast(this, 0,
                new Intent(this, ReminderReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE) != null);

        if (!reminderAlreadyUp) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("type", "bedtime");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            Calendar reminderTime = (Calendar) bedtime.clone();
            reminderTime.add(Calendar.MINUTE, -10); // Напоминание за 30 мин

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private void setWakeupAlarm() {
        // 1. Проверка разрешений для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Показываем системный диалог запроса разрешения
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e("Alarm", "Could not request exact alarms permission", e);
                }
                return; // Прекращаем установку будильника
            }
        }

        // 2. Отмена предыдущего будильника
        Intent oldIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent oldPendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                oldIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);

        if (oldPendingIntent != null) {
            alarmManager.cancel(oldPendingIntent);
            oldPendingIntent.cancel();
        }

        // 3. Проверка и корректировка времени
        Calendar now = Calendar.getInstance();
        if (wakeupTime.before(now)) {
            wakeupTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Проверяем, не установлен ли уже такой же будильник
        boolean alarmAlreadyUp = (PendingIntent.getBroadcast(this, 1,
                new Intent(this, AlarmReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmAlreadyUp) {
            // 4. Установка нового будильника
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("alarm_type", "wakeup");
            intent.putExtra("alarm_time", formatTime(wakeupTime));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    1,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Устанавливаем будильник с учетом версии Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        wakeupTime.getTimeInMillis(),
                        pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        wakeupTime.getTimeInMillis(),
                        pendingIntent);
            }

            Log.d("Alarm", "Wakeup alarm set for: " + formatTime(wakeupTime));
        }
    }

    private void showTimePicker(boolean isWakeupTime) {
        Calendar calendar = isWakeupTime ? wakeupTime : bedtime;

        // Создаем кастомное диалоговое окно
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);
        builder.setView(view);

        // Настройка TimePicker
        TimePicker timePicker = view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));

        // Настройка заголовка (если элемент существует)
        TextView title = view.findViewById(R.id.dialog_title);
        if (title != null) {
            title.setText(isWakeupTime ?
                    getString(R.string.select_wakeup_time) :
                    getString(R.string.select_bedtime));
        }

        // Создаем диалог
        AlertDialog dialog = builder.create();

        // Обработчики кнопок
        TextView positiveButton = view.findViewById(R.id.positive_button);
        positiveButton.setOnClickListener(v -> {
            // Обновляем время
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());

            // Сохраняем изменения
            saveAlarmTimes();
            saveSleepRecord();
            updateUI();
            saveSleepData();
            dialog.dismiss();
        });

        TextView negativeButton = view.findViewById(R.id.negative_button);
        negativeButton.setOnClickListener(v -> dialog.dismiss());

        // Настройка внешнего вида диалога
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
    }

    private void saveAlarmTimes() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(PREF_BEDTIME_HOUR, bedtime.get(Calendar.HOUR_OF_DAY));
        editor.putInt(PREF_BEDTIME_MINUTE, bedtime.get(Calendar.MINUTE));
        editor.putInt(PREF_WAKEUP_HOUR, wakeupTime.get(Calendar.HOUR_OF_DAY));
        editor.putInt(PREF_WAKEUP_MINUTE, wakeupTime.get(Calendar.MINUTE));
        editor.apply();
    }
    private void saveSleepRecord() {
        SleepRecord record = new SleepRecord();
        record.bedtime = formatTime(bedtime);
        record.wakeupTime = formatTime(wakeupTime);
        record.date = Calendar.getInstance().getTimeInMillis();

        sleepDao.insert(record);

        // ⬅️ Добавьте это:
        loadLastSleepTime();  // обновляет текст последнего сна сразу после сохранения
    }


    // Методы расчета времени
    private long calculateSleepDuration() {
        long duration = wakeupTime.getTimeInMillis() - bedtime.getTimeInMillis();
        return duration < 0 ? duration + 24 * 60 * 60 * 1000 : duration;
    }

    private int calculateSleepHours() {
        return (int) (calculateSleepDuration() / (1000 * 60 * 60));
    }

    private int calculateSleepMinutes() {
        return (int) ((calculateSleepDuration() / (1000 * 60)) % 60);
    }

    private long calculateAwakeDuration() {
        long duration = bedtime.getTimeInMillis() - wakeupTime.getTimeInMillis();
        if (duration < 0) {
            duration += 24 * 60 * 60 * 1000; // добавить сутки, если лег спать "на следующий день"
        }
        return duration;
    }

    private float getBedtimeHour() {
        String bedtimeStr = bedtimeText.getText().toString();
        String[] parts = bedtimeStr.split(":");
        return Float.parseFloat(parts[0]) + Float.parseFloat(parts[1]) / 60f;
    }

    private float getWakeupHour() {
        String wakeupStr = wakeupText.getText().toString();
        String[] parts = wakeupStr.split(":");
        return Float.parseFloat(parts[0]) + Float.parseFloat(parts[1]) / 60f;
    }

    private String formatTime(Calendar calendar) {
        return String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
