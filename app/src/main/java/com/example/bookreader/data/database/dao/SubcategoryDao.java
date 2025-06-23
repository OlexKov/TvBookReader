package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bookreader.data.database.entity.Subcategory;

import java.util.List;

@Dao
public interface SubcategoryDao {
    @Insert
    void insert(Subcategory subcategory);

    @Query("SELECT * FROM subcategories WHERE parentCategoryId = :categoryId")
    List<Subcategory> getByCategoryId(int categoryId);
}