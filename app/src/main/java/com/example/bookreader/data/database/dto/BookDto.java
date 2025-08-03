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
        return fileHash != 0 ? fileHash == book.fileHash : id == book.id;
    }

    @Override
    public int hashCode() {
        return fileHash != 0 ? fileHash : Long.hashCode(id);
    }

    public Book getBook(){
        Book book = new Book();
        book.id = id;
        book.author = author;
        book.pageCount = pageCount;
        book.title = title;
        book.description = description;
        book.year = year;
        book.previewPath = previewPath;
        book.filePath = filePath;
        book.fileHash = fileHash;
        book.fileSize = fileSize;
        book.isFavorite = isFavorite;
        book.categoryId = categoryId;
        book.creationDate = creationDate;
        return book;
    }
}
