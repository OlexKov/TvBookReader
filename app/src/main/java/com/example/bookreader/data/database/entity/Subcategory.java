package com.example.bookreader.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(
        tableName = "subcategories",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "parentCategoryId",
                onDelete = ForeignKey.CASCADE
        )
)
public class Subcategory {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public long parentCategoryId;

    public Subcategory(String name, long parentCategoryId) {
        this.name = name;
        this.parentCategoryId = parentCategoryId;
    }
}
