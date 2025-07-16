package com.example.bookreader.customclassses;

import androidx.annotation.NonNull;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.presenters.browserpresenters.BrowserFilePresenter;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrowserFile{
    private File file;
    private boolean checked;

    public  BrowserFile(BrowserFile browserFile){
        this.file = browserFile.file;
        this.checked = browserFile.checked;;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrowserFile browserFile = (BrowserFile) o;
        return file.getAbsolutePath().equals(browserFile.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return file.getAbsolutePath().hashCode();
    }
}
