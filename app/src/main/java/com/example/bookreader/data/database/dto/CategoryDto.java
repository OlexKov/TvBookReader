package com.example.bookreader.data.database.dto;

import java.io.Serializable;

public class CategoryDto implements Serializable {
    public long id;
    public String name;
    public Long parentId = null; // NULL для основних категорій
    public int iconId;
    public Integer booksCount = null;


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryDto category = (CategoryDto) o;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
