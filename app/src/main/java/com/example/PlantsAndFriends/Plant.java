package com.example.PlantsAndFriends;

public class Plant {
    private String id;
    private String number;
    private String name;
    private String species;
    private String temperature;
    private String humidity;
    private String description;
    private String imageUrl;


    public Plant() {
    }

    public Plant(String id, String number, String name, String species, String temperature, String humidity, String description, String imageUrl) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.species = species;
        this.temperature = temperature;
        this.humidity = humidity;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}