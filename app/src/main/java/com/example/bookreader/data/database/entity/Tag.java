package com.example.bookreader.data.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity(tableName = "tags",
        indices = {@Index(value = "name", unique = true)})

@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Tag {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
}
