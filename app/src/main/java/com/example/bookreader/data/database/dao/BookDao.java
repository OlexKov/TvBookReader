package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    long insert(Book book);

    @Query("DELETE FROM books WHERE id = :bookId")
    int deleteById(long bookId);

    @Query("SELECT * FROM books")
    List<BookDto> getAll();

    @Query("SELECT * FROM books WHERE id = :bookId")
    BookDto getById(long bookId);

    @Query("UPDATE books SET isFavorite = 1 WHERE id = :bookId")
    void markBookAsFavorite(long bookId);

    @Query("UPDATE books SET isFavorite = 0 WHERE id = :bookId")
    void unmarkBookAsFavorite(long bookId);

    /// --------------Count query -----------------------------

    @Query("SELECT COUNT(*) FROM books")
    Long getAllCount();

    @Query("SELECT COUNT(*) FROM books WHERE categoryId IS NULL ")
    Long getUnsortedCount();

    @Query("SELECT COUNT(*) FROM books  " +
            "WHERE categoryId = :categoryId "+
            "OR categoryId IN ( SELECT id FROM categories WHERE parentId IS NULL) ")
    Long getUnsortedByCategoryIdCount(Long categoryId);

    @Query( "SELECT COUNT(*) FROM books "+
            "WHERE categoryId = :categoryId "+
            "OR categoryId IN " +
            "( SELECT id FROM categories "+
            "WHERE parentId = :categoryId) ")
    Long getAllByCategoryIdCount(long categoryId);

    /// -------------------------------------------------------

    ///--------------- Pagination --------------------------------

    @Query("SELECT * FROM books " +
            "WHERE isFavorite  ORDER BY creationDate DESC " +
            "LIMIT :size OFFSET (:page -1) * :size")
    List<BookDto> getFavorites(int page,int size);

    @Query("SELECT * FROM books ORDER BY creationDate DESC LIMIT :size OFFSET (:page -1) * :size")
    List<BookDto> getAll(int page,int size);

    @Query("SELECT * FROM books WHERE categoryId IS NULL " +
            "ORDER BY creationDate DESC LIMIT :size OFFSET (:page -1) * :size")
    List<BookDto> getUnsorted(int page,int size);

    @Query("SELECT * FROM books  " +
            "WHERE categoryId = :categoryId "+
            "AND categoryId IN ( SELECT id FROM categories WHERE parentId IS NULL) "+
            "ORDER BY creationDate DESC LIMIT :size OFFSET (:page -1) * :size")
    List<BookDto> getUnsortedByCategoryId(Long categoryId,int page,int size);

    @Query( "SELECT * FROM books "+
            "WHERE categoryId = :categoryId "+
            "OR categoryId IN " +
               "( SELECT id FROM categories "+
               "WHERE parentId = :categoryId) "+
            "ORDER BY creationDate DESC LIMIT :size OFFSET (:page -1) * :size")
    List<BookDto> getAllByCategoryId(long categoryId,int page,int size);



    //-------------------------------------------------------------------------

    @RawQuery(observedEntities = Book.class)
    List<BookDto> getBookPageWithFilter(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Book.class)
    long getBookPageCountWithFilter(SupportSQLiteQuery query);
}
