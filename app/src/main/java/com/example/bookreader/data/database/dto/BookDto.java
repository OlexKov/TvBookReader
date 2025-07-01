package com.example.bookreader.data.database.dto;

import com.example.bookreader.data.database.entity.Book;

import java.io.Serializable;

public class BookDto implements Serializable {
    public long id;

    public String name;

    public Long categoryId = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDto book = (BookDto) o;
        return id == book.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
