package com.example.bookreader.customclassses;

import com.example.bookreader.data.database.dto.BookDto;

import java.io.File;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileData {
    public int hash;
    public File file;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData data = (FileData) o;
        return hash == data.hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}

