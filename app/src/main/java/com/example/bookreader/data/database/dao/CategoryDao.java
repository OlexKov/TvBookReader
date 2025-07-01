package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Query("SELECT * " +
            "FROM categories " +
            "WHERE parentId IS NULL")
    List<CategoryDto> getAllParent();

    @Query("SELECT * " +
            "FROM categories " +
            "WHERE parentId = :categoryId")
    List<CategoryDto> getAllSubcategoryByCategoryId(long categoryId);

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    CategoryDto getById(long categoryId);
}
