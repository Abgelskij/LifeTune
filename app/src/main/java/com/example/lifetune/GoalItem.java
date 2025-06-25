package com.example.lifetune;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Entity(tableName = "goals")
public class GoalItem {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "completed")
    private boolean completed;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "repetition")
    private String repetition;

    @ColumnInfo(name = "created_at")
    private long createdAt;



    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isOverdue() {
        if (completed) return false;

        long currentTime = System.currentTimeMillis();
        long deadline = 0;

        switch (repetition) {
            case "День":
                deadline = createdAt + TimeUnit.DAYS.toMillis(1);
                break;
            case "Неделя":
                deadline = createdAt + TimeUnit.DAYS.toMillis(7);
                break;
            case "Месяц":
                deadline = createdAt + TimeUnit.DAYS.toMillis(30);
                break;
            default:
                return false;
        }

        return currentTime > deadline;
    }


    // Основной конструктор для Room
    public GoalItem(@NonNull String id, String text, boolean completed,
                    String category, String repetition, long createdAt) {
        this.id = id;
        this.text = text;
        this.completed = completed;
        this.category = category;
        this.repetition = repetition;
        this.createdAt = createdAt;
    }

    // Упрощенный конструктор для создания новых целей
    @Ignore
    public GoalItem(String text, boolean completed, String category, String repetition) {
        this(UUID.randomUUID().toString(), text, completed, category, repetition, System.currentTimeMillis());
    }


    // Геттеры и сеттеры
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRepetition() {
        return repetition;
    }

    public void setRepetition(String repetition) {
        this.repetition = repetition;
    }
    private String title;
    // другие поля

    // Конструктор
    @Ignore GoalItem(String title) {
        this.title = title;
    }

    // Геттер для title
    public String getTitle() {
        return text; // или text, в зависимости от вашего поля
    }


    // Геттер и сеттер



    // Сеттер (если нужен)
    public void setTitle(String title) {
        this.title = title;
    }
    // Переопределение equals для корректной работы DiffUtil
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoalItem goalItem = (GoalItem) o;
        return completed == goalItem.completed &&
                id.equals(goalItem.id) &&
                text.equals(goalItem.text) &&
                (category != null ? category.equals(goalItem.category) : goalItem.category == null) &&
                (repetition != null ? repetition.equals(goalItem.repetition) : goalItem.repetition == null);
    }

    // Переопределение hashCode
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + (completed ? 1 : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (repetition != null ? repetition.hashCode() : 0);
        return result;
    }
}