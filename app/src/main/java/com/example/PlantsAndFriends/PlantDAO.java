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
}
