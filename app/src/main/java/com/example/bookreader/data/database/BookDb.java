package com.example.bookreader.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.bookreader.data.database.converters.DateConverter;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.BookSettingsDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.dao.TagDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.BookSettings;
import com.example.bookreader.data.database.entity.BookTag;
import com.example.bookreader.data.database.entity.Tag;
import com.example.bookreader.data.database.entity.Category;

@Database(
        entities = {
                Category.class,
                Book.class,
                Tag.class,
                BookTag.class,
                BookSettings.class
        },
        version = 9,
        exportSchema = false
)

@TypeConverters({DateConverter.class})
public abstract class BookDb extends RoomDatabase{
    public abstract CategoryDao categoryDao();
    public abstract BookDao bookDao();
    public abstract TagDao tagDao();
    public abstract BookSettingsDao bookSettingsDao();
}
