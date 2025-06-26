package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bookreader.data.database.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Query("SELECT * " +
            "FROM categories " +
            "WHERE parentId IS NULL")
    List<Category> getAllParent();

    @Query("SELECT * " +
            "FROM categories " +
            "WHERE parentId = :categoryId")
    List<Category> getAllSubcategoryByCategoryId(long categoryId);

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    Category getById(long categoryId);
}
