package com.example.bookreader.data.database.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.dto.CategoryDto;
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
    public void getCategoryByIdAsync(long categoryId, Consumer<CategoryDto> callback) {
        executorService.execute(() -> {
            CategoryDto category = categoryDao.getById(categoryId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(category);
            });
        });
    }

    public CompletableFuture<CategoryDto> getCategoryByIdAsyncCF(long categoryId) {
        return CompletableFuture.supplyAsync(()->categoryDao.getById(categoryId));
    }


   //getAllSubcategoriesByParentId
    public List<CategoryDto> getAllSubcategoriesByParentId(long parentId){
        return categoryDao.getAllSubcategoryByCategoryId(parentId);
    }

    public CompletableFuture<List<CategoryDto>> getAllSubcategoriesByParentIdCF(long parentId) {
        return CompletableFuture.supplyAsync(()->categoryDao.getAllSubcategoryByCategoryId(parentId));
    }


    //getAllSubcategoriesByParentId
    public void getAllSubcategoriesByParentIdAsync(long parentId,Consumer<List<CategoryDto>> callback) {
        executorService.execute(() -> {
            List<CategoryDto> list = categoryDao.getAllSubcategoryByCategoryId(parentId);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(list);
            });
        });
    }

    public CompletableFuture<List<CategoryDto>> getAllSubcategoriesByParentIdAsyncCF(long parentId) {
        return CompletableFuture.supplyAsync(()->categoryDao.getAllSubcategoryByCategoryId(parentId));
    }


   //getAllParentCategories
    public List<CategoryDto> getAllParentCategories(){
        return categoryDao.getAllParent();
    }

    public CompletableFuture<List<CategoryDto>> getAllParentCategoriesAsyncCF() {
        return CompletableFuture.supplyAsync(categoryDao::getAllParent);
    }

    public void getAllParentCategoriesAsync(Consumer<List<CategoryDto>> callback) {
        executorService.execute(() -> {
            List<CategoryDto> list = categoryDao.getAllParent();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(list);
            });
        });
    }
}
