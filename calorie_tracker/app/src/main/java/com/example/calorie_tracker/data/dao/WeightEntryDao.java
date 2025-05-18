package com.example.calorie_tracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.calorie_tracker.data.model.WeightEntry;

import java.util.Date;
import java.util.List;

@Dao
public interface WeightEntryDao {
    @Insert
    long insert(WeightEntry weightEntry);

    @Update
    void update(WeightEntry weightEntry);

    @Delete
    void delete(WeightEntry weightEntry);

    @Query("SELECT * FROM weight_entries WHERE id = :id")
    LiveData<WeightEntry> getWeightEntryById(long id);

    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<WeightEntry>> getAllWeightEntriesForUser(long userId);

    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    LiveData<WeightEntry> getLatestWeightEntry(long userId);

    @Query("SELECT * FROM weight_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    LiveData<List<WeightEntry>> getWeightEntriesByDateRange(long userId, Date startDate, Date endDate);
} 