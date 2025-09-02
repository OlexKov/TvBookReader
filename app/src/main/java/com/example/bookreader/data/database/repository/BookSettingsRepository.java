package com.example.bookreader.data.database.repository;
import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.BookSettingsDao;
import com.example.bookreader.data.database.dto.BookSettingsDto;
import com.example.bookreader.data.database.entity.BookSettings;

import java.util.concurrent.CompletableFuture;

public class BookSettingsRepository {
    private final BookSettingsDao bookSettingsDao;

    public BookSettingsRepository() {
        this.bookSettingsDao = BookReaderApp.getInstance().getAppDatabase().bookSettingsDao();
    }

    public CompletableFuture<Long> insertAsync(BookSettings bookSettings) {
        return CompletableFuture.supplyAsync(() -> bookSettingsDao.insert(bookSettings));
    }

    public long insert(BookSettings bookSettings) {
        return  bookSettingsDao.insert(bookSettings);
    }

    public CompletableFuture<Void> updateAsync(BookSettings bookSettings) {
        return CompletableFuture.runAsync(() -> bookSettingsDao.update(bookSettings));
    }

    public void update(BookSettings bookSettings) {
         bookSettingsDao.update(bookSettings);
    }

    public CompletableFuture<BookSettingsDto> getByBookIdAsync(Long bookId){
        return CompletableFuture.supplyAsync(()->bookSettingsDao.getByBookId(bookId));
    }

    public BookSettingsDto getByBookId(Long bookId){
        return bookSettingsDao.getByBookId(bookId);
    }
}
