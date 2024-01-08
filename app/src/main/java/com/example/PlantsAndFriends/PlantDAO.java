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

    @Query("SELECT * FROM plants WHERE id = :plantId LIMIT 1")
    PlantEntity getPlantByNumber(int plantId);

    @Query("DELETE FROM plants WHERE id = :plantId")
    void deletePlantByNumber(String plantId);

    @Query("DELETE FROM plants")
    void deleteAllPlants();

    @Query("UPDATE plants SET name = :title WHERE id = :plantId")
    void updatePlantName(int plantId, String title);

    @Query("UPDATE plants SET description = :content WHERE id = :plantId")
    void updatePlantDescription(int plantId, String content);

    @Query("UPDATE plants SET species = :toString WHERE id = :plantId")
    void updatePlantSpecies(int plantId, String toString);

    @Query("UPDATE plants SET min_temp = :minTemp WHERE id = :plantId")
    void updatePlantMinTemp(int plantId, float minTemp);

    @Query("UPDATE plants SET max_temp = :maxTemp WHERE id = :plantId")
    void updatePlantMaxTemp(int plantId, float maxTemp);

    @Query("UPDATE plants SET min_humidity = :minHumidity WHERE id = :plantId")
    void updatePlantMinHumidity(int plantId, float minHumidity);

    @Query("UPDATE plants SET max_humidity = :maxHumidity WHERE id = :plantId")
    void updatePlantMaxHumidity(int plantId, float maxHumidity);

    @Query("UPDATE plants SET imgUri = :imgUri WHERE id = :plantId")
    void updatePlantImageUri(int plantId, String imgUri);

    @Query("SELECT imgUri FROM plants WHERE id = :plantId")
    String getPlantImageUri(String plantId);
}
