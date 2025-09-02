package com.example.bookreader.data.database.dto;

import com.example.bookreader.data.database.entity.BookSettings;

public class BookSettingsDto {
    public long id;
    public long bookId;
    public float scale = 1.0f;
    public float quality = 0.25f;
    public int lastReadPageIndex = 0;
    public int pageOffset = 0;
    public boolean invert = true;

    public BookSettings getBookSetting(){
        return new BookSettings(id,bookId,scale,quality,lastReadPageIndex,pageOffset,invert);
    }
}
