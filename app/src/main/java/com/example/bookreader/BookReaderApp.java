package com.example.bookreader;

import android.app.Application;

import com.example.bookreader.data.database.BookDb;
import com.example.bookreader.data.database.DatabaseClient;

public class BookReaderApp  extends Application {
    private static BookReaderApp instance;
    private BookDb appDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        // Зберігаємо інстанс класу для глобального доступу
        instance = this;

        // Ініціалізуємо базу даних один раз
        appDatabase = DatabaseClient.getInstance(this).getAppDatabase();
    }

    // Глобальний доступ до інстансу MyApp
    public static BookReaderApp getInstance() {
        return instance;
    }

    // Глобальний доступ до бази даних
    public BookDb getAppDatabase() {
        return appDatabase;
    }
}
