package com.example.calorie_tracker.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.calorie_tracker.data.model.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> getUserById(long id);
    
    @Query("SELECT * FROM users WHERE id = :id")
    User getUserByIdSync(long id);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    LiveData<User> getActiveUser();
} 