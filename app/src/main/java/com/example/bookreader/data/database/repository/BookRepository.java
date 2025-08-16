package com.example.bookreader.data.database.repository;

import static com.example.bookreader.utility.ArraysHelper.partitionList;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.constants.Constants;
import com.example.bookreader.data.database.paganation.PaginationResultData;
import com.example.bookreader.data.database.dao.BookDao;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookRepository {
    private final BookDao bookDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final int MAX_SQL_VARS = 999;

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

    public long insert(Book book) {
       return bookDao.insert(book);
    }

    public CompletableFuture<List<String>> getAllPathsAsync(){
        return CompletableFuture.supplyAsync(bookDao::getAllPaths);
    }

    public CompletableFuture<List<Long>> insertAllAsync(List<Book> books) {
       return CompletableFuture.supplyAsync(() -> {
           List<Long> result = new ArrayList<>();
           List<List<Book>> partitions = partitionList(books,MAX_SQL_VARS);
           for (List<Book> bookList:partitions){
               result.addAll(bookDao.insertAll(bookList));
           }
           return result;
        });
    }

    public CompletableFuture<List<Integer>> getBooksByHashesAsync(List<Integer> hashes){
        return CompletableFuture.supplyAsync(() -> {
            List<Integer> results = new ArrayList<>();
            List<List<Integer>> partitions = partitionList(hashes, MAX_SQL_VARS);
            for (List<Integer> hashList:partitions){
                results.addAll(bookDao.getByHash(hashList));
            }
            return results;
        });
    }

    public CompletableFuture<Boolean> isBookExistByHashesAsync(Integer hash){
        return CompletableFuture.supplyAsync(() -> bookDao.existsByHash(hash));
    }

    public CompletableFuture<BookDto> getByIdAsync(Long bookId){
        return CompletableFuture.supplyAsync(()->bookDao.getById(bookId));
    }

    public CompletableFuture<BookDto> updateAndGetAsync(Book book) {
        return CompletableFuture.supplyAsync(() -> {
            bookDao.update(book);
            return bookDao.getById(book.id);
        });
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
        CompletableFuture<Long> booksCount = CompletableFuture.supplyAsync(()->bookDao.getUnsortedByCategoryIdCountAsync(categoryId));
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

    // getByTags

    public CompletableFuture<List<BookDto>> getByTagsListAsync(List<Long> tagsIds){
        return CompletableFuture.supplyAsync(() -> bookDao.getBooksByTagsList(tagsIds,tagsIds.size()));
    }
    public List<BookDto> getByTagsList(List<Long> tagsIds){
        return  bookDao.getBooksByTagsList(tagsIds,tagsIds.size());
    }

    public CompletableFuture<Long> getRangeByTagsListCountAsync(List<Long> tagsIds){
        return CompletableFuture.supplyAsync(() -> bookDao.getBooksByTagsListCount(tagsIds,tagsIds.size()));
    }
    public Long getRangeByTagsListCount(List<Long> tagsIds){
        return  bookDao.getBooksByTagsListCount(tagsIds,tagsIds.size());
    }



    public CompletableFuture<List<BookDto>> loadRowBooksAsync(Long mainCategoryId, Long rowCategoryId, int offset, int limit ) {
        if (Objects.equals(mainCategoryId, Constants.FAVORITE_CATEGORY_ID) || Objects.equals(rowCategoryId, Constants.FAVORITE_CATEGORY_ID)) {
            return getRangeFavoriteBooksAsync(offset, limit);
        }
        if (Objects.equals(mainCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
            if (Objects.equals(rowCategoryId,  Constants.ALL_BOOKS_CATEGORY_ID)) {
                return getRangeAllBooksAsync(offset, limit);
            }
            if (Objects.equals(rowCategoryId, Constants.UNSORTED_BOOKS_CATEGORY_ID)) {
                return getRangeUnsortedBooksAsync(offset, limit);
            }
            return getRangeAllBooksInCategoryIdAsync(rowCategoryId, offset, limit);
        }
        if (Objects.equals(rowCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
            return getRangeAllBooksInCategoryIdAsync(mainCategoryId, offset, limit);
        }
        if (Objects.equals(rowCategoryId, Constants.UNSORTED_BOOKS_CATEGORY_ID)) {
            return getRageUnsortedBooksByCategoryIdAsync(mainCategoryId, offset, limit);
        }
        return getRangeAllBooksInCategoryIdAsync(rowCategoryId, offset, limit);
    }

    public CompletableFuture<Long> getRowBooksCountAsync(Long mainCategoryId, Long rowCategoryId) {
        if (Objects.equals(mainCategoryId,Constants.FAVORITE_CATEGORY_ID) || Objects.equals(rowCategoryId,Constants.FAVORITE_CATEGORY_ID)) {
            return CompletableFuture.supplyAsync(bookDao::getFavoriteBooksCount);
        }
        if (Objects.equals(mainCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
            if (Objects.equals(rowCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
                return CompletableFuture.supplyAsync(bookDao::getAllCount);
            }
            if (Objects.equals(rowCategoryId, Constants.UNSORTED_BOOKS_CATEGORY_ID)) {
                return CompletableFuture.supplyAsync(bookDao::getUnsortedCount);
            }
            return CompletableFuture.supplyAsync(()->bookDao.getAllByCategoryIdCount(rowCategoryId));
        }

        if (Objects.equals(rowCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
            return  CompletableFuture.supplyAsync(()->bookDao.getAllByCategoryIdCount(mainCategoryId));
        }
        if (Objects.equals(rowCategoryId, Constants.UNSORTED_BOOKS_CATEGORY_ID)) {
            return CompletableFuture.supplyAsync(()->bookDao.getUnsortedByCategoryIdCountAsync(mainCategoryId));
        }
        return CompletableFuture.supplyAsync(()->bookDao.getAllByCategoryIdCount(rowCategoryId));
    }

    public Long getRowBooksCount(Long mainCategoryId, Long rowCategoryId) {
        if (Objects.equals(mainCategoryId,Constants.FAVORITE_CATEGORY_ID) || Objects.equals(rowCategoryId,Constants.FAVORITE_CATEGORY_ID)) {
            return bookDao.getFavoriteBooksCount();
        }
        if (Objects.equals(mainCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
            if (Objects.equals(rowCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
                return bookDao.getAllCount();
            }
            if (Objects.equals(rowCategoryId, Constants.UNSORTED_BOOKS_CATEGORY_ID)) {
                return bookDao.getUnsortedCount();
            }
            return bookDao.getAllByCategoryIdCount(rowCategoryId);
        }

        if (Objects.equals(rowCategoryId, Constants.ALL_BOOKS_CATEGORY_ID)) {
            return  bookDao.getAllByCategoryIdCount(mainCategoryId);
        }
        if (Objects.equals(rowCategoryId, Constants.UNSORTED_BOOKS_CATEGORY_ID)) {
            return bookDao.getUnsortedByCategoryIdCountAsync(mainCategoryId);
        }
        return bookDao.getAllByCategoryIdCount(rowCategoryId);
    }
}


