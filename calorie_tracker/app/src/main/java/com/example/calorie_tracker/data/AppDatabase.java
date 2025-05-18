package com.example.calorie_tracker.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.calorie_tracker.data.dao.FoodDao;
import com.example.calorie_tracker.data.dao.MealEntryDao;
import com.example.calorie_tracker.data.dao.UserDao;
import com.example.calorie_tracker.data.dao.WeightEntryDao;
import com.example.calorie_tracker.data.model.Food;
import com.example.calorie_tracker.data.model.MealEntry;
import com.example.calorie_tracker.data.model.User;
import com.example.calorie_tracker.data.model.WeightEntry;
import com.example.calorie_tracker.utils.DateConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Food.class, MealEntry.class, WeightEntry.class}, version = 3, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract UserDao userDao();
    public abstract FoodDao foodDao();
    public abstract MealEntryDao mealEntryDao();
    public abstract WeightEntryDao weightEntryDao();
    
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, 
                            "calorie_tracker_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration() // This ensures database recreation if schema changes
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void clearInstance() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }
    
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            databaseWriteExecutor.execute(() -> {
                UserDao userDao = INSTANCE.userDao();
                FoodDao foodDao = INSTANCE.foodDao();

                INSTANCE.getOpenHelper().getWritableDatabase().execSQL("PRAGMA foreign_keys = ON;");
            });
        }
        
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    };
} 