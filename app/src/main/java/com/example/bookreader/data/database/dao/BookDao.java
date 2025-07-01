package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    long insert(Book book);

    @Delete
    int delete(Book book);

    @Query("DELETE FROM books WHERE id = :bookId")
    int deleteById(long bookId);

    @Query("SELECT * FROM books")
    List<BookDto> getAll();

    @Query("SELECT * FROM books WHERE id = :bookId")
    BookDto getById(long bookId);

    @Query("SELECT * " +
            "FROM books  " +
            "WHERE categoryId = :categoryId")
    List<BookDto> getByCategoryId(long categoryId);

    @Query("SELECT * " +
            "FROM books  " +
            "WHERE categoryId IS NULL")
    List<BookDto> getUnsorted();

    @Query( "SELECT * " +
            "FROM books "+
            "WHERE categoryId = :parentCategoryId "+
            "OR categoryId IN " +
              "( SELECT id " +
              "FROM categories "+
              "WHERE parentId = :parentCategoryId) "
    )
    List<BookDto> getAllByCategoryId(long parentCategoryId);
}
