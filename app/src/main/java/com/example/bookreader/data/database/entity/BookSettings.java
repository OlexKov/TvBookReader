package com.example.bookreader.data.database.entity;

import static com.example.bookreader.constants.Constants.READER_PAGE_DEFAULT_BRIGHTNESS;
import static com.example.bookreader.constants.Constants.READER_PAGE_DEFAULT_CONTRAST;
import static com.example.bookreader.constants.Constants.READER_PAGE_QUALITY_HIGH;
import static com.example.bookreader.constants.Constants.READER_PAGE_QUALITY_MEDIUM;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity(tableName = "books_settings",
        foreignKeys = @ForeignKey(
                entity = Book.class,
                parentColumns = "id",
                childColumns = "bookId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "bookId", unique = true)})
@NoArgsConstructor
@AllArgsConstructor
public class BookSettings {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long bookId;
    public float scale = 1.0f;
    public float quality = READER_PAGE_QUALITY_HIGH;
    public float contrast = READER_PAGE_DEFAULT_CONTRAST;
    public float brightness = READER_PAGE_DEFAULT_BRIGHTNESS;
    public int lastReadPageIndex = 0;
    public int pageOffset = 0;
    public boolean invert = true;
}
