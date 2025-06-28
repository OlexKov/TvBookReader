package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.entity.Book;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

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

    public void getBookByIdAsync(long bookId, Consumer<Book> callback) {
        executorService.execute(() -> {
            Book book = bookDao.getById(bookId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(book);
            });
        });
    }

    //getAllBooks
    public CompletableFuture<List<Book>> getAllBookAsyncCF() {
        return CompletableFuture.supplyAsync(bookDao::getAll);
    }

    public  List<Book> getAllBook(){
        return bookDao.getAll();
    }

    public void getAllBookAsync(Consumer<List<Book>> callback) {
        executorService.execute(() -> {
            List<Book> books = bookDao.getAll();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }


    //getAllBooksByCategoryId
    public List<Book> getAllBooksByCategoryId(long categoryId){
        return bookDao.getAllByCategoryId(categoryId);
    }

    public void getAllBooksByCategoryIdAsync(long categoryId, Consumer<List<Book>> callback) {
        executorService.execute(() -> {
            List<Book> books = bookDao.getAllByCategoryId(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public CompletableFuture<List<Book>> getAllBooksByCategoryIdAsyncCF(long categoryId) {
        return CompletableFuture.supplyAsync(()-> bookDao.getAllByCategoryId(categoryId));
    }


    //geAllUnsortedBooks
    public List<Book>  getAllUnsortedBooks( ){
        return bookDao.getUnsorted();
    }

    public void getAllUnsortedBooksAsync( Consumer<List<Book>> callback) {
        executorService.execute(() -> {
            List<Book> books = bookDao.getUnsorted();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public CompletableFuture<List<Book>> getAllUnsortedBooksAsyncCF() {
        return CompletableFuture.supplyAsync(bookDao::getUnsorted);
    }

    //deleteBook
    public void deleteBook(Book book, Consumer<Integer> callback){
        executorService.execute(() -> {
         int rows = bookDao.delete(book);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(rows);
            });
        });
    }


    //getBooksByCategoryId
    public List<Book> getBooksByCategoryId(long categoryId){
        return bookDao.getByCategoryId(categoryId);
    }

    public void getBooksByCategoryIdAsync(long categoryId, Consumer<List<Book>> callback) {
        executorService.execute(() -> {
            List<Book> books = bookDao.getByCategoryId(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public CompletableFuture<List<Book>> getBooksByCategoryIdAsyncCF(long categoryId) {
        return CompletableFuture.supplyAsync(()->bookDao.getByCategoryId(categoryId));
    }

}
