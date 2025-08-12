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

    @Query("SELECT * FROM categories ")
    List<CategoryDto> getAll();

    @Query("SELECT * " +
            "FROM categories " +
            "WHERE parentId IS NULL")
    List<CategoryDto> getAllParent();

    @Query("SELECT DISTINCT cat.* FROM categories AS cat " +
            "JOIN books AS bks ON bks.categoryId = cat.id " +
            "WHERE bks.id IN (:bookIds)")
    List<CategoryDto> getByBookIds(List<Long> bookIds);

    @Query("SELECT cat.id, cat.name, cat.parentId, cat.iconId, " +
            "(SELECT COUNT(*) FROM books bks WHERE bks.categoryId = cat.id OR bks.categoryId IN " +
            "(SELECT sub.id FROM categories sub WHERE sub.parentId = cat.id)) AS booksCount " +
            "FROM categories AS cat ")
    List<CategoryDto> getAllParentWithBookCount();

    @Query("SELECT * " +
            "FROM categories " +
            "WHERE parentId = :categoryId")
    List<CategoryDto> getAllSubcategoryByCategoryId(long categoryId);

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    CategoryDto getById(long categoryId);
}
