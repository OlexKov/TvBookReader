package com.example.bookreader;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.bookreader.data.database.BookDb;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.dao.SubcategoryDao;
import com.example.bookreader.data.database.entity.Book;
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
              //  .fallbackToDestructiveMigration() // видалить базу при несумісності схем
                .addCallback(seedData)  // додали callback
                .build();
        //getApplicationContext().deleteDatabase("book-database");
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
                BookDao bdao = appDatabase.bookDao();
                dao.insert(Category.builder().name( "Всі").build());

                long categoryId =  dao.insert(Category.builder()
                        .name( "Категорія 1")
                        .build());
                sdao.insert(Subcategory.builder()
                        .name("Всі")
                        .parentCategoryId(categoryId)
                        .build());
                long subcategoryId  = sdao.insert(Subcategory.builder()
                        .name("Субкатегорія 1")
                        .parentCategoryId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .name("Категорія 1 - Субкатегорія 1 - книга - " + i)
                            .subCategoryId(subcategoryId)
                            .build());
                }
                subcategoryId  = sdao.insert(Subcategory.builder()
                        .name("Субкатегорія 2")
                        .parentCategoryId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert(Book.builder()
                            .name("Категорія 1 - Субкатегорія 2 - книга - " + i)
                            .subCategoryId(subcategoryId)
                            .build());
                }

                categoryId =  dao.insert(Category.builder()
                        .name( "Категорія 2")
                        .build());
                sdao.insert(Subcategory.builder()
                        .name("Всі")
                        .parentCategoryId(categoryId)
                        .build());
                subcategoryId  = sdao.insert( Subcategory.builder()
                        .name("Субкатегорія 1")
                        .parentCategoryId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert(Book.builder()
                            .name("Категорія 2 - Субкатегорія 1 - книга - " + i)
                            .subCategoryId(subcategoryId)
                            .build());
                }
                subcategoryId  = sdao.insert(Subcategory.builder()
                        .name("Субкатегорія 2")
                        .parentCategoryId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert(Book.builder()
                            .name("Категорія 2 - Субкатегорія 2 - книга - " + i)
                            .subCategoryId(subcategoryId)
                            .build());
                }
            });
        }
    };
}
