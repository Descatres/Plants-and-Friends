package com.example.PlantsAndFriends;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plants")
public class PlantEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String number;
    private String name;
    private String species;
    private String temperature;
    private String humidity;
    private String description;

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
    public String getName() {
        return name;
    }

    // Setter for title
    public void setName(String name) {
        this.name = name;
    }

    // Getter for species
    public String getSpecies() {
        return species;
    }

    // Setter for species
    public void setSpecies(String species) {
        this.species = species;
    }

    // Getter for temperature
    public String getTemperature() {
        return temperature;
    }

    // Setter for temperature
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    // Getter for humidity
    public String getHumidity() {
        return humidity;
    }

    // Setter for humidity
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    // Getter for content
    public String getDescription() {
        return description;
    }

    // Setter for content
    public void setDescription(String description) {
        this.description = description;
    }


}
