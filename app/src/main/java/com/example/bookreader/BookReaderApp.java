package com.example.bookreader;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.bookreader.data.database.BookDb;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.dao.SubcategoryDao;
import com.example.bookreader.data.database.entity.Category;
import com.example.bookreader.data.database.entity.Subcategory;

import java.util.concurrent.Executors;

public class BookReaderApp  extends Application {
    private static BookReaderApp instance;
    private BookDb appDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        // Зберігаємо інстанс класу для глобального доступу
        instance = this;

        // Ініціалізуємо базу даних один раз
        appDatabase = Room.databaseBuilder(getApplicationContext(), BookDb.class, "book-database")
                .fallbackToDestructiveMigration() // видалить базу при несумісності схем
                .addCallback(seedData)  // додали callback
                .build();
    }

    // Глобальний доступ до інстансу MyApp
    public static BookReaderApp getInstance() {
        return instance;
    }

    // Глобальний доступ до бази даних
    public BookDb getAppDatabase() {
        return appDatabase;
    }

    private final RoomDatabase.Callback seedData = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d("Info","Data init");
            Executors.newSingleThreadExecutor().execute(() -> {
                CategoryDao dao = appDatabase.categoryDao();
                SubcategoryDao sdao = appDatabase.subcategoryDao();
                long categoryId = dao.insert(new Category( "Всі"));
                sdao.insert(new Subcategory("Всі",categoryId));


            });
        }
    };
}
