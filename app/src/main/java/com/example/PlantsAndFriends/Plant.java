package com.example.PlantsAndFriends;

import androidx.annotation.Nullable;

public class Plant {
    private String id;
    private String number;
    private String name;
    private String species;
    private float min_temp;
    private float max_temp;
    private float min_humidity;
    private float max_humidity;
    private String description;
    private String imgUrl;

    public Plant() {
    }

    public Plant(String id, String number, String name, String species, float min_temp, float max_temp, float min_humidity, float max_humidity, String description, @Nullable String imgUrl) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.species = species;
        this.min_temp = min_temp;
        this.max_temp = max_temp;
        this.min_humidity = min_humidity;
        this.max_humidity = max_humidity;
        this.description = description;
        this.imgUrl = imgUrl;
    }

    // Getters and setters for Plant fields
    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public float getMin_temp() {
        return min_temp;
    }

    public float getMax_temp() {
        return max_temp;
    }

    public float getMin_humidity() {
        return min_humidity;
    }

    public float getMax_humidity() {
        return max_humidity;
    }

    public String getDescription() {
        return description;
    }

    public void setId(String number) {
        this.number = number;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}