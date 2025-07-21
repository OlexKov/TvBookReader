package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.constants.Constants;
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

    public CompletableFuture<List<Integer>> getBooksByHashesAsync(List<Integer> hashes){
        return CompletableFuture.supplyAsync(() -> bookDao.getByHash(hashes));
    }


    ///  toggle favorites

    public CompletableFuture<PaginationResultData<BookDto>> getPageDataFavoriteBooksAsync(int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(bookDao::getFavoriteBooksCount);
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getFavoritesRange((page - 1) * size, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<List<BookDto>> getRangeFavoriteBooksAsync(int offset, int limit){
        return CompletableFuture.supplyAsync(() -> bookDao.getFavoritesRange(offset, limit));
    }

    public void getFavoriteBooksCount(Consumer<Long> callback) {
        executorService.execute(() -> {
            Long count =  bookDao.getFavoriteBooksCount();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(count);
            });
        });
    }

    public void addBookToFavorite(Long bookId , Consumer<Long> callback) {
        executorService.execute(() -> {
            bookDao.markBookAsFavorite(bookId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(null);
            });
        });
    }

    public void removeBookFromFavorite(Long bookId ,Consumer<Long> callback) {
        executorService.execute(() -> {
            bookDao.unmarkBookAsFavorite(bookId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(null);
            });
        });
    }



    //get All Books

    public CompletableFuture<PaginationResultData<BookDto>> getPageDataAllBooksAsync(int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(bookDao::getAllCount);
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getAllRange((page - 1) * size, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<List<BookDto>> getRangeAllBooksAsync(int offset, int limit){
         return CompletableFuture.supplyAsync(() -> bookDao.getAllRange(offset, limit));
    }

    //getAllBooksByCategoryId

    public CompletableFuture<PaginationResultData<BookDto>> getPageDataAllBooksInCategoryIdAsync(Long categoryId, int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(()->bookDao.getAllByCategoryIdCount(categoryId));
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getAllByCategoryIdRange(categoryId,(page - 1) * size, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }
    public CompletableFuture<List<BookDto>> getRangeAllBooksInCategoryIdAsync(Long categoryId, int offset, int limit){
        return  CompletableFuture.supplyAsync(() -> bookDao.getAllByCategoryIdRange(categoryId,offset, limit));
    }

    //get unsorted books
    public CompletableFuture<PaginationResultData<BookDto>> getPageDataUnsortedBooksAsync(int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(bookDao::getUnsortedCount);
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getUnsortedRange((page - 1) * size, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<List<BookDto>> getRangeUnsortedBooksAsync(int offset, int limit){
        return CompletableFuture.supplyAsync(() -> bookDao.getUnsortedRange(offset, limit));
    }



    public CompletableFuture<PaginationResultData<BookDto>> getPageDataUnsortedBooksByCategoryIdAsync(Long categoryId, int page, int size){
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(()->bookDao.getUnsortedByCategoryIdCount(categoryId));
        CompletableFuture<List<BookDto>> bookList = CompletableFuture.supplyAsync(() -> bookDao.getUnsortedByCategoryIdRange(categoryId,(page - 1) * size, size));
        return CompletableFuture.allOf(booksCount, bookList).thenApply(v -> {
            PaginationResultData<BookDto> result = new PaginationResultData<>();
            result.setData(bookList.join());
            result.setTotal(booksCount.join());
            return result;
        });
    }

    public CompletableFuture<List<BookDto>> getRageUnsortedBooksByCategoryIdAsync(Long categoryId, int offset, int limit){
        return CompletableFuture.supplyAsync(() -> bookDao.getUnsortedByCategoryIdRange(categoryId,offset, limit));
    }

    //deleteBook

    public CompletableFuture<Integer> deleteBookByIdAsyncCF(long bookId) {
        return CompletableFuture.supplyAsync(() -> bookDao.deleteById(bookId));
    }


    public CompletableFuture<List<BookDto>> loadRowBooks(Long mainCategoryId, Long rowCategoryId,int offset,int limit ) {
        if (mainCategoryId == Constants.FAVORITE_CATEGORY_ID || rowCategoryId == Constants.FAVORITE_CATEGORY_ID) {
            return getRangeFavoriteBooksAsync(offset, limit);
        } else if (mainCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
            if (rowCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return getRangeAllBooksAsync(offset, limit);
            } else if (rowCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return getRangeUnsortedBooksAsync(offset, limit);
            } else {
                return getRangeAllBooksInCategoryIdAsync(rowCategoryId, offset, limit);
            }
        } else {
            if (rowCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return getRangeAllBooksInCategoryIdAsync(mainCategoryId, offset, limit);
            } else if (rowCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return getRageUnsortedBooksByCategoryIdAsync(mainCategoryId, offset, limit);
            } else {
                return getRangeAllBooksInCategoryIdAsync(mainCategoryId, offset, limit);
            }
        }
    }
}


