package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.entity.Book;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookRepository {
    private final BookDao bookDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BookRepository() {
        this.bookDao = BookReaderApp.getInstance().getAppDatabase().bookDao();
    }

    public void insertAsync(Book book, Consumer<Long> callback) {
        executorService.execute(() -> {
            long id = bookDao.insert(book);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(id);
            });
        });
    }

    public void geBookByIdAsync(long bookId, Consumer<Book> callback) {
        executorService.execute(() -> {
            Book book = bookDao.getById(bookId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(book);
            });
        });
    }

    public void geBooksBySubcategoryIdAsync(long subcategoryId, Consumer<List<Book>> callback) {
        executorService.execute(() -> {
            List<Book> books = bookDao.getBySubcategoryId(subcategoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public void geBooksByCategoryIdAsync(long categoryId, Consumer<List<Book>> callback) {
        executorService.execute(() -> {
            List<Book> books = bookDao.getByCategoryId(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

}
