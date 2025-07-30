package com.example.bookreader.utility.bookutils;

import android.graphics.Bitmap;

import com.example.bookreader.customclassses.BrowserFile;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.utility.HashHelper;

public  class BookInfo extends BookDto {
    public Bitmap preview;
    public Book getBook(){
        Book book = new Book();
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
        return book;
    }
}
