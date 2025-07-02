package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.paganation.PaginationResultData;
import com.example.bookreader.data.database.paganation.QueryFilter;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;


import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    public void getBookByIdAsync(long bookId, Consumer<BookDto> callback) {
        executorService.execute(() -> {
            BookDto book = bookDao.getById(bookId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(book);
            });
        });
    }

    //getAllBooks
    public CompletableFuture<List<BookDto>> getAllBookAsyncCF() {
        return CompletableFuture.supplyAsync(bookDao::getAll);
    }

    public List<BookDto> getAllBook() {
        return bookDao.getAll();
    }

    public void getAllBookAsync(Consumer<List<BookDto>> callback) {
        executorService.execute(() -> {
            List<BookDto> books = bookDao.getAll();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public CompletableFuture<List<BookDto>> getBooksPageAsyncCF(int page, int size) {
        //Log.d("SqlLog",new QueryFilter().buildPagination(page,size).getSql());
        return CompletableFuture.supplyAsync(() -> bookDao.getBookPageWithFilter(new QueryFilter().buildPagination(page, size)));
    }

    public CompletableFuture<List<BookDto>> getBooksPageAsyncCF(int page, int size, QueryFilter filter) {
        //Log.d("SqlLog",filter.buildPagination(page,size).getSql());
        return CompletableFuture.supplyAsync(() -> bookDao.getBookPageWithFilter(filter.buildPagination(page, size)));
    }

    public CompletableFuture<Long> getBooksFilterCountAsyncCF(QueryFilter filter) {
        return CompletableFuture.supplyAsync(() -> bookDao.getBookPageCountWithFilter(filter.buildCount()));
    }

    public CompletableFuture<PaginationResultData<BookDto>> getBooksPageDataAsyncCF(int page, int size, QueryFilter filter) {
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(() -> bookDao.getBookPageCountWithFilter(filter.buildCount()));
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getBookPageWithFilter(filter.buildPagination(page, size)));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<PaginationResultData<BookDto>> getBooksPageDataAsyncCF(int page, int size) {
        QueryFilter emptyFilter = new QueryFilter();
        return getBooksPageDataAsyncCF(page,size,emptyFilter);
    }


    //getAllBooksByCategoryId
    public List<BookDto> getAllBooksByCategoryId(long categoryId) {
        return bookDao.getAllByCategoryId(categoryId);
    }

    public void getAllBooksByCategoryIdAsync(long categoryId, Consumer<List<BookDto>> callback) {
        executorService.execute(() -> {
            List<BookDto> books = bookDao.getAllByCategoryId(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public CompletableFuture<List<BookDto>> getAllBooksByCategoryIdAsyncCF(long categoryId) {
        return CompletableFuture.supplyAsync(() -> bookDao.getAllByCategoryId(categoryId));
    }


    //geAllUnsortedBooks
    public List<BookDto> getAllUnsortedBooks() {
        return bookDao.getUnsorted();
    }

    public void getAllUnsortedBooksAsync(Consumer<List<BookDto>> callback) {
        executorService.execute(() -> {
            List<BookDto> books = bookDao.getUnsorted();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public CompletableFuture<List<BookDto>> getAllUnsortedBooksAsyncCF() {
        return CompletableFuture.supplyAsync(bookDao::getUnsorted);
    }

    //deleteBook
    public CompletableFuture<Integer> deleteBookAsyncCF(Book book) {
        return CompletableFuture.supplyAsync(() -> bookDao.delete(book));
    }

    public CompletableFuture<Integer> deleteBookByIdAsyncCF(long bookId) {
        return CompletableFuture.supplyAsync(() -> bookDao.deleteById(bookId));
    }

    public void deleteBookAsync(Book book, Consumer<Integer> callback) {
        executorService.execute(() -> {
            int rows = bookDao.delete(book);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(rows);
            });
        });
    }

    public void deleteBookByIdAsync(long bookId, Consumer<Integer> callback) {
        executorService.execute(() -> {
            int rows = bookDao.deleteById(bookId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(rows);
            });
        });
    }


    //getBooksByCategoryId
    public List<BookDto> getBooksByCategoryId(long categoryId) {
        return bookDao.getByCategoryId(categoryId);
    }

    public void getBooksByCategoryIdAsync(long categoryId, Consumer<List<BookDto>> callback) {
        executorService.execute(() -> {
            List<BookDto> books = bookDao.getByCategoryId(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(books);
            });
        });
    }

    public CompletableFuture<List<BookDto>> getBooksByCategoryIdAsyncCF(long categoryId) {
        return CompletableFuture.supplyAsync(() -> bookDao.getByCategoryId(categoryId));
    }
}


