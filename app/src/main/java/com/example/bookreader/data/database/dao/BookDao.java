package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bookreader.data.database.entity.Book;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    long insert(Book book);

    @Query("SELECT * FROM books")
    List<Book> getAll();

    @Query("SELECT * FROM books WHERE id = :bookId")
    Book getById(long bookId);

    @Query("SELECT * " +
            "FROM books  " +
            "WHERE categoryId = :categoryId")
    List<Book> getByCategoryId(long categoryId);

    @Query("SELECT * " +
            "FROM books  " +
            "WHERE categoryId IS NULL")
    List<Book> getUnsorted();

    @Query( "SELECT * " +
            "FROM books "+
            "WHERE categoryId = :parentCategoryId "+
            "OR categoryId IN " +
              "( SELECT id " +
              "FROM categories "+
              "WHERE parentId = :parentCategoryId) "
    )
    List<Book> getAllByCategoryId(long parentCategoryId);
}
