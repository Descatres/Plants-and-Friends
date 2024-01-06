package com.example.PlantsAndFriends;

public class Plant {
    private String id;
    private String number;
    private String title;
    private String content;

    public Plant() {
    }

    public Plant(String id, String number, String title, String content) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.content = content;
    }

    // Getters and setters for Note fields
    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public void setId(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}