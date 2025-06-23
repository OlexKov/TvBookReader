package com.example.bookreader.data.database.repository;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.DatabaseClient;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.entity.Category;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CategoryRepository() {
        this.categoryDao = BookReaderApp.getInstance().getAppDatabase().categoryDao();
    }

    public void insert(Category category) {
        executorService.execute(() -> {
            categoryDao.insert(category);
        });
    }

    public void getCategoryByIdAsync(int categoryId, Consumer<Category> callback) {
        executorService.execute(() -> {
            Category category = categoryDao.getById(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(category);
            });
        });
    }
    public void getAllCategoriesAsync(Consumer<List<Category>> callback) {
        executorService.execute(() -> {
            List<Category> list = categoryDao.getAll();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(list);
            });
        });
    }
}
