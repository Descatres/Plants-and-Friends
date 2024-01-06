package com.example.PlantsAndFriends;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plants")
public class PlantEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String number;
    private String title;
    private String content;

    public PlantEntity() {
    }

    // getters and setters

    // Getter for id
    public int getId() {
        return id;
    }

    // Setter for id
    public void setId(int id) {
        this.id = id;
    }

    // Getter for number
    public String getNumber() {
        return number;
    }

    // Setter for number
    public void setNumber(String number) {
        this.number = number;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Setter for title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for content
    public String getContent() {
        return content;
    }

    // Setter for content
    public void setContent(String content) {
        this.content = content;
    }


}
