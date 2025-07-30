package com.example.bookreader.data.database.dto;

import androidx.leanback.widget.DiffCallback;

import com.example.bookreader.data.database.entity.Book;

import java.io.Serializable;
import java.util.Date;

public class BookDto implements Serializable {
    public long id;
    public int fileHash;
    public String author;
    public int pageCount;
    public String filePath;
    public String zipPath;
    public String previewPath;
    public String title;
    public String description;
    public String year;
    public Long categoryId = null;
    public long fileSize;
    public boolean isFavorite = false;
    public Date creationDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDto book = (BookDto) o;
        return fileHash == book.fileHash;
    }

    @Override
    public int hashCode() {
        return fileHash;
    }
}
