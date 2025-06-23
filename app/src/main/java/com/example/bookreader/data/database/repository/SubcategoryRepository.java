package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.SubcategoryDao;
import com.example.bookreader.data.database.entity.Category;
import com.example.bookreader.data.database.entity.Subcategory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubcategoryRepository {
    private final SubcategoryDao subcategoryDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SubcategoryRepository() {
        this.subcategoryDao = BookReaderApp.getInstance().getAppDatabase().subcategoryDao();
    }

    public void insert(Subcategory subcategory, Consumer<Long> callback) {
        executorService.execute(() -> {
            long id = subcategoryDao.insert(subcategory);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(id);
            });
        });
    }

    public void getSubcategoriesForCategory(long categoryId, Consumer<List<Subcategory>> callback) {
        executorService.execute(() -> {
            List<Subcategory> subCategories = subcategoryDao.getByCategoryId(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(subCategories);
            });
        });
    }


}
