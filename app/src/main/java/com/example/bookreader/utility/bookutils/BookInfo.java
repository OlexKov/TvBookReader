package com.example.bookreader.utility.bookutils;

import android.graphics.Bitmap;

import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.utility.HashHelper;

public  class BookInfo {
    public String author;
    public int pageCount;
    public String title;
    public String description;
    public String year;
    public Bitmap preview;
    public int hash;
    public String filePath;
    public String previewPath;
    public long fileSize;
    public Book getBook(){
        Book book = new Book();
        book.author = author;
        book.pageCount = pageCount;
        book.title = title;
        book.description = description;
        book.year = year;
        book.previewPath = previewPath;
        book.filePath = filePath;
        book.fileHash = hash;
        book.fileSize = fileSize;
        return book;
    }
}
