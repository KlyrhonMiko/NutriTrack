package com.example.calorie_tracker.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "meal_entries",
        foreignKeys = {
            @ForeignKey(entity = User.class,
                       parentColumns = "id",
                       childColumns = "userId",
                       onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Food.class,
                       parentColumns = "id",
                       childColumns = "foodId",
                       onDelete = ForeignKey.CASCADE)
        })
public class MealEntry {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long userId;
    private long foodId;
    private Date date;
    private String mealType; // "breakfast", "lunch", "dinner", "snack"
    private float servingsConsumed;
    private int caloriesConsumed;

    public MealEntry(long userId, long foodId, Date date, String mealType, float servingsConsumed, int caloriesConsumed) {
        this.userId = userId;
        this.foodId = foodId;
        this.date = date;
        this.mealType = mealType;
        this.servingsConsumed = servingsConsumed;
        this.caloriesConsumed = caloriesConsumed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public float getServingsConsumed() {
        return servingsConsumed;
    }

    public void setServingsConsumed(float servingsConsumed) {
        this.servingsConsumed = servingsConsumed;
    }

    public int getCaloriesConsumed() {
        return caloriesConsumed;
    }

    public void setCaloriesConsumed(int caloriesConsumed) {
        this.caloriesConsumed = caloriesConsumed;
    }
} 