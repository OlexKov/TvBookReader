package com.example.bookreader.data.database.dto;

import static com.example.bookreader.constants.Constants.READER_PAGE_DEFAULT_BRIGHTNESS;
import static com.example.bookreader.constants.Constants.READER_PAGE_DEFAULT_CONTRAST;
import static com.example.bookreader.constants.Constants.READER_PAGE_QUALITY_HIGH;
import com.example.bookreader.data.database.entity.BookSettings;

import java.io.Serializable;

public class BookSettingsDto implements Serializable {
    public long id;
    public long bookId;
    public float scale = 1.0f;
    public float quality = READER_PAGE_QUALITY_HIGH;
    public float contrast = READER_PAGE_DEFAULT_CONTRAST;
    public float brightness = READER_PAGE_DEFAULT_BRIGHTNESS;
    public int lastReadPageIndex = 0;
    public int pageOffset = 0;
    public boolean invert = true;

    public BookSettings getBookSetting(){
        return new BookSettings(id,bookId,scale,quality,contrast,brightness,lastReadPageIndex,pageOffset,invert);
    }
}
