package com.example.calorie_tracker.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "weight_entries",
        foreignKeys = @ForeignKey(entity = User.class,
                   parentColumns = "id",
                   childColumns = "userId",
                   onDelete = ForeignKey.CASCADE))
public class WeightEntry {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long userId;
    private float weight; // in kg
    private Date date;
    private String notes;

    public WeightEntry(long userId, float weight, Date date) {
        this.userId = userId;
        this.weight = weight;
        this.date = date;
        this.notes = "";
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

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 