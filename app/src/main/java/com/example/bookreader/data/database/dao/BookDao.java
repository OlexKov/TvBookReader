package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    long insert(Book book);

    @Update
    void update(Book book);

    @Insert(onConflict = OnConflictStrategy.REPLACE) // або інша стратегія
    List<Long> insertAll(List<Book> books);

    @Query("DELETE FROM books WHERE id = :bookId")
    int deleteById(long bookId);

    @Query("SELECT fileHash FROM books WHERE fileHash IN (:hashes)")
    List<Integer> getByHash(List<Integer> hashes);

    @Query("SELECT filePath FROM books")
    List<String> getAllPaths();

    @Query("SELECT EXISTS (SELECT 1 FROM books WHERE fileHash = :hash)")
    boolean existsByHash(Integer hash);

    @Query("SELECT * FROM books")
    List<BookDto> getAllRange();

    @Query("SELECT * FROM books WHERE id = :bookId")
    BookDto getById(long bookId);

    /// ------------- Favorite ------------

    @Query("UPDATE books SET isFavorite = 1 WHERE id = :bookId")
    void markBookAsFavorite(long bookId);

    @Query("UPDATE books SET isFavorite = 0 WHERE id = :bookId")
    void unmarkBookAsFavorite(long bookId);

    @Query("SELECT * FROM books WHERE isFavorite = 1 " +
            "ORDER BY creationDate DESC " +
            "LIMIT :size OFFSET (:page -1) * :size" )
    List<BookDto> getFavoriteBooks(int page,int size);


    /// --------------Count query -----------------------------

    @Query("SELECT COUNT(*) FROM books WHERE isFavorite = 1 ")
    Long getFavoriteBooksCount();

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
            "LIMIT :limit OFFSET :offset")
    List<BookDto> getFavoritesRange(int offset, int limit);

    @Query("SELECT * FROM books ORDER BY creationDate DESC LIMIT :limit OFFSET :offset")
    List<BookDto> getAllRange(int offset, int limit);

    @Query("SELECT * FROM books WHERE categoryId IS NULL " +
            "ORDER BY creationDate DESC LIMIT :limit OFFSET :offset")
    List<BookDto> getUnsortedRange(int offset, int limit);

    @Query("SELECT * FROM books  " +
            "WHERE categoryId = :categoryId "+
            "AND categoryId IN ( SELECT id FROM categories WHERE parentId IS NULL) "+
            "ORDER BY creationDate DESC LIMIT :limit OFFSET :offset")
    List<BookDto> getUnsortedByCategoryIdRange(Long categoryId, int offset, int limit);

    @Query( "SELECT * FROM books "+
            "WHERE categoryId = :categoryId "+
            "OR categoryId IN " +
               "( SELECT id FROM categories "+
               "WHERE parentId = :categoryId) "+
            "ORDER BY creationDate DESC LIMIT :limit OFFSET :offset")
    List<BookDto> getAllByCategoryIdRange(long categoryId, int offset, int limit);



    //-------------------------------------------------------------------------

    @RawQuery(observedEntities = Book.class)
    List<BookDto> getBookPageWithFilter(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Book.class)
    long getBookPageCountWithFilter(SupportSQLiteQuery query);
}
