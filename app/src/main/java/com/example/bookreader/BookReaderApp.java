package com.example.bookreader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.leanback.widget.ListRow;
import androidx.room.Room;

import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.extentions.IconHeader;
import com.example.bookreader.utility.GlobalEventListener;
import com.example.bookreader.data.database.BookDb;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;

public class BookReaderApp  extends Application {
    // Глобальний доступ до інстансу MyApp
    @Getter
    private static BookReaderApp instance;

    // Глобальний доступ до бази даних
    @Getter
    private BookDb appDatabase;

    public boolean isDataBaseInit(){
        return prefs.getBoolean("db_seeded", false);
    }

    @Getter
    @Setter
    private boolean isMenuOpen = true;

    @Getter
    @Setter
    private IconHeader selectedParentCategoryHeader = new IconHeader(123123123, "Всі",R.drawable.books_stack);

    @Getter
    @Setter
    private ListRow selectedRow = null;

    @Getter
    @Setter
    private BookDto selectedItem = null;

    @Getter
    private GlobalEventListener globalEventListener;

    private SharedPreferences prefs;

    @Getter
    private List<CategoryDto> categoriesCash ;

    public void updateCategoryCash(){
        Executors.newSingleThreadExecutor().execute(() -> {
            categoriesCash = appDatabase.categoryDao().getAllParentWithBookCount();
            globalEventListener.sendEvent(GlobalEventType.CATEGORY_CASH_UPDATED,null);
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalEventListener = new GlobalEventListener();
        // Зберігаємо інстанс класу для глобального доступу
        instance = this;
        prefs = getApplicationContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
       // prefs.edit().putBoolean("db_seeded", false).apply();
       // getApplicationContext().deleteDatabase("book-database");
        appDatabase = Room.databaseBuilder(getApplicationContext(), BookDb.class, "book-database")
                .fallbackToDestructiveMigration() // видалить базу при несумісності схем
                .build();
    }

    public void DataBaseInit(){
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
                        .iconId(R.drawable.settings)
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
                        .iconId(R.drawable.books_stack)
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

                categoryId =  сdao.insert(Category.builder()
                        .name( "Категорія 3")
                        .iconId(R.drawable.books_stack)
                        .build());

                 сdao.insert(Category.builder()
                        .name("Субкатегорія 1")
                        .parentId(categoryId)
                        .build());

                prefs.edit().putBoolean("db_seeded", true).apply();
                categoriesCash = appDatabase.categoryDao().getAllParentWithBookCount();
                globalEventListener.sendEvent(GlobalEventType.DATABASE_DONE,null);
            });
        }
}

