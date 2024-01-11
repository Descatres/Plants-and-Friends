package com.example.PlantsAndFriends;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlantDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlantEntity plant);

    @Query("SELECT * FROM plants")
    LiveData<List<PlantEntity>> getAllPlants();

    @Query("SELECT * FROM plants WHERE number = :plantNumber LIMIT 1")
    PlantEntity getPlantByNumber(String plantNumber);

    @Query("DELETE FROM plants WHERE number = :plantNumber")
    void deletePlantByNumber(String plantNumber);

    @Query("DELETE FROM plants")
    void deleteAllPlants();

    @Query("UPDATE plants SET name = :title WHERE number = :plantNumber")
    void updatePlantName(String plantNumber, String title);

    @Query("UPDATE plants SET description = :content WHERE number = :plantNumber")
    void updatePlantDescription(String plantNumber, String content);

    @Query("UPDATE plants SET species = :toString WHERE number = :plantNumber")
    void updatePlantSpecies(String plantNumber, String toString);

    @Query("UPDATE plants SET min_temp = :minTemp WHERE number = :plantNumber")
    void updatePlantMinTemp(String plantNumber, float minTemp);

    @Query("UPDATE plants SET max_temp = :maxTemp WHERE number = :plantNumber")
    void updatePlantMaxTemp(String plantNumber, float maxTemp);

    @Query("UPDATE plants SET min_humidity = :minHumidity WHERE number = :plantNumber")
    void updatePlantMinHumidity(String plantNumber, float minHumidity);

    @Query("UPDATE plants SET max_humidity = :maxHumidity WHERE number = :plantNumber")
    void updatePlantMaxHumidity(String plantNumber, float maxHumidity);

    @Query("UPDATE plants SET imgUri = :imgUri WHERE number = :plantNumber")
    void updatePlantImageUri(String plantNumber, String imgUri);

    @Query("SELECT imgUri FROM plants WHERE number = :plantNumber")
    String getPlantImageUri(String plantNumber);
}
