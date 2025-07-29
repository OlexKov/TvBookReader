package com.example.bookreader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.leanback.widget.ListRow;
import androidx.room.Room;

import com.example.bookreader.customclassses.MainCategoryInfo;
import com.example.bookreader.customclassses.MainStorage;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.utility.eventlistener.GlobalEventListener;
import com.example.bookreader.data.database.BookDb;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;
//import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;

public class BookReaderApp  extends Application {
    private  SharedPreferences prefs;
    private final List<CategoryDto> categoriesCash = new ArrayList<>();

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
    @Setter
    private MainCategoryInfo selectedMainCategoryInfo ;

    @Getter
    @Setter
    private ListRow selectedRow = null;

    @Getter
    @Setter
    private BookDto selectedItem = null;

    @Getter
    @Setter
    private MainStorage selectedFolder = null;

    @Getter
    private GlobalEventListener globalEventListener;

    public boolean isDataBaseInit(){
        return prefs.getBoolean("db_seeded", false);
    }

    public List<CategoryDto> getCategoriesCash(){
        return Collections.unmodifiableList(categoriesCash);
    }

    public void updateCategoryCash(){
        Executors.newSingleThreadExecutor().execute(() -> {
            categoriesCash.clear();
            categoriesCash.addAll(appDatabase.categoryDao().getAllParentWithBookCount());
            globalEventListener.sendEvent(GlobalEventType.CATEGORY_CASH_UPDATED,null);
        });
    }

    public String getLocalLanguage(){
        return prefs.getString("localization","uk");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        System.setProperty("zip.encoding", "UTF-8");
       // PDFBoxResourceLoader.init(getApplicationContext());
        globalEventListener = new GlobalEventListener();

        setPrefs();
        //getApplicationContext().deleteDatabase("book-database");
        appDatabase = Room.databaseBuilder(getApplicationContext(), BookDb.class, "book-database")
                .fallbackToDestructiveMigration() // видалить базу при несумісності схем
                .build();
    }

    public void DataBaseInit(){
            Log.d("Info","Data init");
            Executors.newSingleThreadExecutor().execute(() -> {
                CategoryDao сdao = appDatabase.categoryDao();
                BookDao bdao = appDatabase.bookDao();
                Random random = new Random();

                for (int i = 0; i < 10;i++){
                     bdao.insert( Book.builder()
                            .title("книга - " + i)
                            .isFavorite(i%2==0)
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
                            .title("Категорія 1 - Субкатегорія 1 - книга - " + i)
                            .isFavorite(i%2==0)
                            .categoryId(subcategoryId)
                            .build());
                }

                subcategoryId  = сdao.insert(Category.builder()
                        .name("Субкатегорія 2")
                        .parentId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .title("Категорія 1 - Субкатегорія 2 - книга - " + i)
                            .categoryId(subcategoryId)
                            .isFavorite(i%2==0)
                            .build());
                }

                for (int i = 0; i < 7;i++){
                    bdao.insert( Book.builder()
                            .title("Категорія 1 - книга - " + i)
                            .categoryId(categoryId)
                            .isFavorite(i%2==0)
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
                            .title("Категорія 2 - Субкатегорія 1 - книга - " + i)
                            .categoryId(subcategoryId)
                            .build());
                }
                subcategoryId  = сdao.insert(Category.builder()
                        .name("Субкатегорія 2")
                        .parentId(categoryId)
                        .build());
                for (int i = 0; i < 10;i++){
                    bdao.insert( Book.builder()
                            .title("Категорія 2 - Субкатегорія 2 - книга - " + i)
                            .categoryId(subcategoryId)
                            .build());
                }
                for (int i = 0; i < 7;i++){
                    bdao.insert( Book.builder()
                            .title("Категорія 2 - книга - " + i)
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
                categoriesCash.addAll(appDatabase.categoryDao().getAllParentWithBookCount());
                globalEventListener.sendEvent(GlobalEventType.DATABASE_DONE,null);
            });
        }

    private void setPrefs(){
        prefs = getApplicationContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        if(!prefs.contains("db_seeded")){
            prefs.edit().putBoolean("db_seeded", false).apply();
        }
        if(!prefs.contains("localization")){
            prefs.edit().putString("localization", "uk").apply();
        }
    }
}

