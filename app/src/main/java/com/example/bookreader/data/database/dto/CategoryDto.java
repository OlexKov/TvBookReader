package com.example.bookreader.data.database.dto;

import com.example.bookreader.data.database.entity.Category;

import java.io.Serializable;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CategoryDto implements Serializable {
    public long id;
    public String name;
    public Long parentId = null;
    public int iconId;
    public Integer booksCount = null;

    public CategoryDto(){}
    public CategoryDto(CategoryDto category){
        id = category.id;
        name = category.name;
        parentId = category.parentId;
        iconId = category.iconId;
        booksCount = category.booksCount;
    }

    public Category getCategory(){
        return new Category(id,name,parentId,iconId);
    }

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
