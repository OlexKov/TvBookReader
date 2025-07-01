package com.example.bookreader.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;

@Database(
        entities = {
                Category.class,
                Book.class
        },
        version = 5,
        exportSchema = false
)
public abstract class BookDb extends RoomDatabase{
    public abstract CategoryDao categoryDao();
    public abstract BookDao bookDao();
}
