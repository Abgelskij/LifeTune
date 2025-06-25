package com.example.lifetune;

public class Workout {
    private int id;
    private String title;
    private String description;
    private String schedule;

    public Workout(String title, String description, String schedule) {
        this.title = title;
        this.description = description;
        this.schedule = schedule;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
}