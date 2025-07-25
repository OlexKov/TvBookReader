package com.example.bookreader.utility.bookutils;

import android.graphics.Bitmap;

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
    public long fileSize;

    public int getHash(){
        return HashHelper.getStringHash(author + String.valueOf(pageCount)+description+year+String.valueOf(fileSize));
    }
}
