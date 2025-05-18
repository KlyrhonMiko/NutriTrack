package com.example.calorie_tracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.calorie_tracker.data.AppDatabase;
import com.example.calorie_tracker.data.dao.UserDao;
import com.example.calorie_tracker.data.model.User;

import java.util.List;

public class UserRepository {
    private final UserDao userDao;
    private final LiveData<User> activeUser;
    private final LiveData<List<User>> allUsers;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        activeUser = userDao.getActiveUser();
        allUsers = userDao.getAllUsers();
    }

    public LiveData<User> getActiveUser() {
        return activeUser;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getUserById(long id) {
        return userDao.getUserById(id);
    }

    public LiveData<Integer> getUserDailyCalorieGoal(long userId) {
        return Transformations.map(userDao.getUserById(userId), user -> {
            final int DEFAULT_CALORIE_GOAL = 2000;

            if (user != null && user.getDailyCalorieGoal() > 0) {
                return user.getDailyCalorieGoal();
            }

            return DEFAULT_CALORIE_GOAL;
        });
    }

    public void insert(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            System.out.println("DEBUG: Inserting user into database");
            System.out.println("DEBUG: User data - Name: " + user.getName() + ", Gender: " + user.getGender());
            System.out.println("DEBUG: User metrics - Height: " + user.getHeight() + ", Weight: " + user.getWeight());
            System.out.println("DEBUG: User activity - Level: " + user.getActivityLevel() + ", Goal: " + user.getWeightGoalType());
            System.out.println("DEBUG: Calculated calorie goal: " + user.getDailyCalorieGoal());
            
            try {
                long userId = userDao.insert(user);
                System.out.println("DEBUG: User inserted successfully with ID: " + userId);

                User savedUser = userDao.getUserByIdSync(userId);
                if (savedUser != null) {
                    System.out.println("DEBUG: Retrieved saved user - ID: " + savedUser.getId() + ", Goal: " + savedUser.getDailyCalorieGoal());
                } else {
                    System.out.println("DEBUG: Failed to retrieve saved user!");
                }
            } catch (Exception e) {
                System.err.println("DEBUG: Error inserting user: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void update(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.update(user);
        });
    }

    public void delete(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.delete(user);
        });
    }
} 