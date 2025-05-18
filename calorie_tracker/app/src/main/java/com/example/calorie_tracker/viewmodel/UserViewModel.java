package com.example.calorie_tracker.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.calorie_tracker.data.model.User;
import com.example.calorie_tracker.data.repository.UserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final LiveData<User> activeUser;
    private final LiveData<List<User>> allUsers;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
        activeUser = userRepository.getActiveUser();
        allUsers = userRepository.getAllUsers();
    }

    public LiveData<User> getActiveUser() {
        return activeUser;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getUserById(long id) {
        return userRepository.getUserById(id);
    }

    public void insert(User user) {
        userRepository.insert(user);
    }

    public void update(User user) {
        userRepository.update(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }
} 