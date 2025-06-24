package com.example.bookreader.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity(tableName = "categories")
@NoArgsConstructor                // Потрібен Room для створення через рефлексію
@AllArgsConstructor              // Повний конструктор, якщо треба вручну створювати
@Builder                         // Додає патерн Builder
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
}
