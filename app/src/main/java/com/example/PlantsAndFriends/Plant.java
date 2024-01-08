package com.example.PlantsAndFriends;

import androidx.annotation.Nullable;

public class Plant {
    private int id;
    private String name;
    private String species;
    private double min_temp;
    private double max_temp;
    private double min_humidity;
    private double max_humidity;
    private String description;
    private String imgUri;

    public Plant() {
    }

    public Plant(int id, String name, String species, double min_temp, double max_temp, double min_humidity, double max_humidity, String description, @Nullable String imgUri) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.min_temp = min_temp;
        this.max_temp = max_temp;
        this.min_humidity = min_humidity;
        this.max_humidity = max_humidity;
        this.description = description;
        this.imgUri = imgUri;
    }

    // Getters and setters for Plant fields

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public double getMin_temp() {
        return min_temp;
    }

    public double getMax_temp() {
        return max_temp;
    }

    public double getMin_humidity() {
        return min_humidity;
    }

    public double getMax_humidity() {
        return max_humidity;
    }

    public String getDescription() {
        return description;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setId(int number) {
        this.id = number;
    }

    public String setNumber(String number) {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setMin_temp(float min_temp) {
        this.min_temp = min_temp;
    }

    public void setMax_temp(float max_temp) {
        this.max_temp = max_temp;
    }

    public void setMin_humidity(float min_humidity) {
        this.min_humidity = min_humidity;
    }

    public void setMax_humidity(float max_humidity) {
        this.max_humidity = max_humidity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }
}