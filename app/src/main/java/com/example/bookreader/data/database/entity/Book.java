package com.example.bookreader.data.database.entity;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "books",
        foreignKeys = @ForeignKey(
        entity = Subcategory.class,
        parentColumns = "id",
        childColumns = "subCategoryId",
        onDelete = ForeignKey.CASCADE
))
public class Book {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public long subCategoryId;

    public Book(String name,long subCategoryId) {
        this.name = name;
        this.subCategoryId = subCategoryId;
    }
}
