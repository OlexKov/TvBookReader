package com.example.bookreader;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.customclassses.GlobalEventListener;
import com.example.bookreader.data.database.BookDb;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

public class BookReaderApp  extends Application {
    // Глобальний доступ до інстансу MyApp
    @Getter
    private static BookReaderApp instance;

    // Глобальний доступ до бази даних
    @Getter
    private BookDb appDatabase;

    @Getter
    @Setter
    private boolean isMenuOpen = true;

    @Getter
    private GlobalEventListener globalEventListener;


//    @Setter
//    private boolean isRowsChanget = false;
//
//    public boolean getIsRowsChanget(){
//        if(isRowsChanget){
//            isRowsChanget = false;
//            return true;
//        }
//        return false;
//    }
//
//
//    @Setter
//    private Book bookToDelete = null;
//
//    public Book getBookToDelete(){
//        if(bookToDelete != null){
//            Book temp = bookToDelete;
//            bookToDelete = null;
//            return temp;
//        }
//        return null;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalEventListener = new GlobalEventListener();
        // Зберігаємо інстанс класу для глобального доступу
        instance = this;
        //getApplicationContext().deleteDatabase("book-database");
        // Ініціалізуємо базу даних один раз
        appDatabase = Room.databaseBuilder(getApplicationContext(), BookDb.class, "book-database")
                .fallbackToDestructiveMigration() // видалить базу при несумісності схем
                .addCallback(seedData)  // додали callback
                .build();

    }


    private final RoomDatabase.Callback seedData = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d("Info","Data init");
            Executors.newSingleThreadExecutor().execute(() -> {
                CategoryDao сdao = appDatabase.categoryDao();
                BookDao bdao = appDatabase.bookDao();
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .name("книга - " + i)
                            .categoryId(null)
                            .build());
                }
                long categoryId =  сdao.insert(Category.builder()
                        .name( "Категорія 1")
                        .build());

                long subcategoryId  = сdao.insert(Category.builder()
                        .name("Субкатегорія 1")
                        .parentId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .name("Категорія 1 - Субкатегорія 1 - книга - " + i)
                            .categoryId(subcategoryId)
                            .build());
                }

                subcategoryId  = сdao.insert(Category.builder()
                        .name("Субкатегорія 2")
                        .parentId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .name("Категорія 1 - Субкатегорія 2 - книга - " + i)
                            .categoryId(subcategoryId)
                            .build());
                }

                for (int i = 0; i < 5;i++){
                    bdao.insert( Book.builder()
                            .name("Категорія 1 - книга - " + i)
                            .categoryId(categoryId)
                            .build());
                }




                categoryId =  сdao.insert(Category.builder()
                        .name( "Категорія 2")
                        .build());

                subcategoryId  = сdao.insert(Category.builder()
                        .name("Субкатегорія 1")
                        .parentId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .name("Категорія 2 - Субкатегорія 1 - книга - " + i)
                            .categoryId(subcategoryId)
                            .build());
                }
                subcategoryId  = сdao.insert(Category.builder()
                        .name("Субкатегорія 2")
                        .parentId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .name("Категорія 2 - Субкатегорія 2 - книга - " + i)
                            .categoryId(subcategoryId)
                            .build());
                }
                for (int i = 0; i < 5;i++){
                    bdao.insert( Book.builder()
                            .name("Категорія 2 - книга - " + i)
                            .categoryId(categoryId)
                            .build());
                }
            });
        }
    };
}
