package com.example.PlantsAndFriends;

public class Plant {
    private String id;
    private String number;
    private String name;
    private String species;
    private String temperature;
    private String humidity;
    private String description;

    public Plant() {
    }

    public Plant(String id, String number, String name, String species, String temperature, String humidity, String description) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.species = species;
        this.temperature = temperature;
        this.humidity = humidity;
        this.description = description;
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

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
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

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}