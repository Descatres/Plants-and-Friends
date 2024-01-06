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

    @Query("SELECT * FROM plants WHERE number = :noteNumber LIMIT 1")
    PlantEntity getPlantByNumber(String noteNumber);

    @Query("DELETE FROM plants WHERE number = :noteNumber")
    void deletePlantByNumber(String noteNumber);

    @Query("DELETE FROM plants")
    void deleteAllPlants();

    @Query("UPDATE plants SET title = :title WHERE number = :noteNumber")
    void updatePlantTitle(String noteNumber, String title);

    @Query("UPDATE plants SET content = :content WHERE number = :noteNumber")
    void updatePlantContent(String noteNumber, String content);
}
