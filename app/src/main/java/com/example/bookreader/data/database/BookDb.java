package com.example.bookreader.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.dao.SubcategoryDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;
import com.example.bookreader.data.database.entity.Subcategory;

@Database(
        entities = {
                Category.class,
                Subcategory.class,
                Book.class
        },
        version = 2,
        exportSchema = false
)
public abstract class BookDb extends RoomDatabase{
    public abstract CategoryDao categoryDao();
    public abstract SubcategoryDao subcategoryDao();
    public abstract BookDao bookDao();
}
