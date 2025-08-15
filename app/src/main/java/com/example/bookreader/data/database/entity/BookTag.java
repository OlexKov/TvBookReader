package com.example.bookreader.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "books_tags",
        foreignKeys = {
                @ForeignKey(entity = Book.class,
                        parentColumns = "id",
                        childColumns = "bookId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Tag.class,
                        parentColumns = "id",
                        childColumns = "tagId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("bookId"), @Index("tagId")})
public class BookTag {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long bookId;
    public long tagId;
}
