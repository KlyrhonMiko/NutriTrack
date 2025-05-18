package com.example.calorie_tracker.ui.food;

public class FoodItem {
    private long id;
    private String name;
    private String brand;
    private String category;
    private String serving;
    private int calories;
    private double protein;
    private double carbs;
    private double fat;
    private boolean favorite;
    private boolean userCreated;
    private boolean isRecipe;

    public FoodItem(String name, String brand, String category, String serving, int calories, 
                   double protein, double carbs, double fat, boolean favorite, boolean userCreated) {
        this.id = 0;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.serving = serving;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.favorite = favorite;
        this.userCreated = userCreated;
        this.isRecipe = false;
    }

    public FoodItem(String name, String brand, String category, String serving, int calories, 
                   double protein, double carbs, double fat, boolean favorite, boolean userCreated, boolean isRecipe) {
        this.id = 0;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.serving = serving;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.favorite = favorite;
        this.userCreated = userCreated;
        this.isRecipe = isRecipe;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getServing() {
        return serving;
    }

    public void setServing(String serving) {
        this.serving = serving;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isUserCreated() {
        return userCreated;
    }

    public void setUserCreated(boolean userCreated) {
        this.userCreated = userCreated;
    }

    public boolean isRecipe() {
        return isRecipe;
    }

    public void setRecipe(boolean recipe) {
        isRecipe = recipe;
    }
} 