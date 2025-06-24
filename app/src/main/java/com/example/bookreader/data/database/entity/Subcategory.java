package com.example.bookreader.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity(
        tableName = "subcategories",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "parentCategoryId",
                onDelete = ForeignKey.CASCADE
        )
)
@NoArgsConstructor                // Потрібен Room для створення через рефлексію
@AllArgsConstructor              // Повний конструктор, якщо треба вручну створювати
@Builder                         // Додає патерн Builder
public class Subcategory {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public long parentCategoryId;
}
