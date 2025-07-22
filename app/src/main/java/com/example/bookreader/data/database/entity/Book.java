package com.example.bookreader.data.database.entity;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity(tableName = "books",
        foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "categoryId",
        onDelete = ForeignKey.SET_NULL
))


@NoArgsConstructor                // Потрібен Room для створення через рефлексію
@AllArgsConstructor              // Повний конструктор, якщо треба вручну створювати
@Builder                         // Додає патерн Builder
public class Book {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String author;
    public int pageCount;
    public String filePath;
    public String zipPath;
    public String previewPath;
    public String title;
    public String description;
    public String year;
    public int fileHash;
    public Long categoryId = null;
    public boolean isFavorite = false;
    public Date creationDate = new Date();
}
