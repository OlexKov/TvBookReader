package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CategoryRepository() {
        this.categoryDao = BookReaderApp.getInstance().getAppDatabase().categoryDao();
    }

    public void insert(Category category, Consumer<Long> callback) {
        executorService.execute(() -> {
            long id = categoryDao.insert(category);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(id);
            });
        });
    }


    //getCategoryByIdAsync
    public void getCategoryByIdAsync(long categoryId, Consumer<Category> callback) {
        executorService.execute(() -> {
            Category category = categoryDao.getById(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(category);
            });
        });
    }

    public CompletableFuture<Category> getCategoryByIdAsyncCF(long categoryId) {
        return CompletableFuture.supplyAsync(()->categoryDao.getById(categoryId));
    }


   //getAllSubcategoriesByParentId
    public List<Category> getAllSubcategoriesByParentId(long parentId){
        return categoryDao.getAllSubcategoryByCategoryId(parentId);
    }

    public CompletableFuture<List<Category>> getAllSubcategoriesByParentIdCF(long parentId) {
        return CompletableFuture.supplyAsync(()->categoryDao.getAllSubcategoryByCategoryId(parentId));
    }


    //getAllSubcategoriesByParentId
    public void getAllSubcategoriesByParentIdAsync(long parentId,Consumer<List<Category>> callback) {
        executorService.execute(() -> {
            List<Category> list = categoryDao.getAllSubcategoryByCategoryId(parentId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(list);
            });
        });
    }

    public CompletableFuture<List<Category>> getAllSubcategoriesByParentIdAsyncCF(long parentId) {
        return CompletableFuture.supplyAsync(()->categoryDao.getAllSubcategoryByCategoryId(parentId));
    }


   //getAllParentCategories
    public List<Category> getAllParentCategories(){
        return categoryDao.getAllParent();
    }
    public CompletableFuture<List<Category>> getAllParentCategoriesAsyncCF() {
        return CompletableFuture.supplyAsync(categoryDao::getAllParent);
    }

    public void getAllParentCategoriesAsync(Consumer<List<Category>> callback) {
        executorService.execute(() -> {
            List<Category> list = categoryDao.getAllParent();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(list);
            });
        });
    }
}
