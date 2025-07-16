package com.example.bookreader.customclassses;

import androidx.annotation.NonNull;

import com.example.bookreader.data.database.dto.BookDto;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrowserFile implements Cloneable{
    private File file;
    private boolean checked;

    @NonNull
    @Override
    public BrowserFile clone() {
        try {
            return (BrowserFile) super.clone(); // поверхневе клонування
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
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
