package com.example.bookreader.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(
        tableName = "categories",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "parentId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parentId")}
)
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public Long parentId = null;
    public Integer iconId = null;
}
