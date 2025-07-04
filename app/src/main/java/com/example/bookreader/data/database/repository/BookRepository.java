package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.paganation.PaginationResultData;
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


    //get All Books

    public CompletableFuture<PaginationResultData<BookDto>> getPageDataAllBooksAsync(int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(bookDao::getAllCount);
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getAll(page, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<List<BookDto>> getPageAllBooksAsync(int page,int size){
         return CompletableFuture.supplyAsync(() -> bookDao.getAll(page, size));
    }

    //getAllBooksByCategoryId

    public CompletableFuture<PaginationResultData<BookDto>> getPageDataAllBooksInCategoryIdAsync(Long categoryId, int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(()->bookDao.getAllByCategoryIdCount(categoryId));
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getAllByCategoryId(categoryId,page, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }
    public CompletableFuture<List<BookDto>> getPageAllBooksInCategoryIdAsync(Long categoryId, int page, int size){
        return  CompletableFuture.supplyAsync(() -> bookDao.getAllByCategoryId(categoryId,page, size));
    }

    //get unsorted books
    public CompletableFuture<PaginationResultData<BookDto>> getPageDataUnsortedBooksAsync(int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(bookDao::getUnsortedCount);
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getUnsorted(page, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<List<BookDto>> getPageUnsortedBooksAsync(int page, int size){
        return CompletableFuture.supplyAsync(() -> bookDao.getUnsorted(page, size));
    }



    public CompletableFuture<PaginationResultData<BookDto>> getPageDataUnsortedBooksBuCategoryIdAsync(Long categoryId, int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(()->bookDao.getUnsortedByCategoryIdCount(categoryId));
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getUnsortedByCategoryId(categoryId,page, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<List<BookDto>> getPageUnsortedBooksBuCategoryIdAsync(Long categoryId, int page, int size){
        return CompletableFuture.supplyAsync(() -> bookDao.getUnsortedByCategoryId(categoryId,page, size));
    }

    //deleteBook

    public CompletableFuture<Integer> deleteBookByIdAsyncCF(long bookId) {
        return CompletableFuture.supplyAsync(() -> bookDao.deleteById(bookId));
    }


    //getBooksByCategoryId

}


