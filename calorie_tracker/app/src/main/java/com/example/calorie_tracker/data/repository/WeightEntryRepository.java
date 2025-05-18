package com.example.calorie_tracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.calorie_tracker.data.AppDatabase;
import com.example.calorie_tracker.data.dao.WeightEntryDao;
import com.example.calorie_tracker.data.model.WeightEntry;

import java.util.Date;
import java.util.List;

public class WeightEntryRepository {
    private final WeightEntryDao weightEntryDao;

    public WeightEntryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        weightEntryDao = db.weightEntryDao();
    }

    public LiveData<WeightEntry> getWeightEntryById(long id) {
        return weightEntryDao.getWeightEntryById(id);
    }

    public LiveData<List<WeightEntry>> getAllWeightEntriesForUser(long userId) {
        return weightEntryDao.getAllWeightEntriesForUser(userId);
    }

    public LiveData<WeightEntry> getLatestWeightEntry(long userId) {
        return weightEntryDao.getLatestWeightEntry(userId);
    }

    public LiveData<List<WeightEntry>> getWeightEntriesByDateRange(long userId, Date startDate, Date endDate) {
        return weightEntryDao.getWeightEntriesByDateRange(userId, startDate, endDate);
    }

    public void insert(WeightEntry weightEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            weightEntryDao.insert(weightEntry);
        });
    }

    public void update(WeightEntry weightEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            weightEntryDao.update(weightEntry);
        });
    }

    public void delete(WeightEntry weightEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            weightEntryDao.delete(weightEntry);
        });
    }
} 