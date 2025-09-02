package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.bookreader.data.database.dto.BookSettingsDto;
import com.example.bookreader.data.database.entity.BookSettings;

import java.util.List;

@Dao
public interface BookSettingsDao {

    @Insert
    long insert(BookSettings bookSettings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<BookSettings> booksSettings);

    @Update
    void update(BookSettings bookSettings);

    @Query("SELECT * FROM books_settings WHERE bookId = :bookId")
    BookSettingsDto getByBookId(long bookId);
}
