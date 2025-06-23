package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bookreader.data.database.entity.Book;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    long insert(Book category);

    @Query("SELECT * FROM books")
    List<Book> getAll();

    @Query("SELECT * FROM books WHERE id = :bookId")
    Book getById(long bookId);

    @Query("SELECT * FROM books WHERE subCategoryId = :subcategoryId")
    List<Book> getBySubcategoryId(long subcategoryId);

    @Query("SELECT * FROM books WHERE subcategoryId IN (SELECT id FROM subcategories WHERE parentCategoryId = :categoryId)")
    List<Book> getByCategoryId(long categoryId);
}
