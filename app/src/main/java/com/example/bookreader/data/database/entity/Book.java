package com.example.bookreader.data.database.entity;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity(tableName = "books",
        foreignKeys = @ForeignKey(
        entity = Subcategory.class,
        parentColumns = "id",
        childColumns = "subCategoryId",
        onDelete = ForeignKey.CASCADE
))


@NoArgsConstructor                // Потрібен Room для створення через рефлексію
@AllArgsConstructor              // Повний конструктор, якщо треба вручну створювати
@Builder                         // Додає патерн Builder
public class Book {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public long subCategoryId;
}
