package com.example.calorie_tracker.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "foods")
public class Food {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String brand;
    private int calories; // per serving
    private float servingSize; // in grams
    private float protein; // in grams
    private float carbs; // in grams
    private float fat; // in grams
    private float fiber; // in grams
    private float sugar; // in grams
    private boolean isFavorite;
    private boolean isRecipe;

    public Food(String name, String brand, int calories, float servingSize, 
                float protein, float carbs, float fat, float fiber, float sugar) {
        this.name = name;
        this.brand = brand;
        this.calories = calories;
        this.servingSize = servingSize;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
        this.isFavorite = false;
        this.isRecipe = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public float getServingSize() {
        return servingSize;
    }

    public void setServingSize(float servingSize) {
        this.servingSize = servingSize;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public float getCarbs() {
        return carbs;
    }

    public void setCarbs(float carbs) {
        this.carbs = carbs;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public float getFiber() {
        return fiber;
    }

    public void setFiber(float fiber) {
        this.fiber = fiber;
    }

    public float getSugar() {
        return sugar;
    }

    public void setSugar(float sugar) {
        this.sugar = sugar;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isRecipe() {
        return isRecipe;
    }

    public void setRecipe(boolean recipe) {
        isRecipe = recipe;
    }
} 