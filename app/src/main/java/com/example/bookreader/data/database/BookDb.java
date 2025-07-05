package com.example.bookreader.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.bookreader.data.database.converters.DateConverter;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;

@Database(
        entities = {
                Category.class,
                Book.class
        },
        version = 7,
        exportSchema = false
)

@TypeConverters({DateConverter.class})
public abstract class BookDb extends RoomDatabase{
    public abstract CategoryDao categoryDao();
    public abstract BookDao bookDao();
}
