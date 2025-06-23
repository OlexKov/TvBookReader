package com.example.bookreader.data.database.repository;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.SubcategoryDao;
import com.example.bookreader.data.database.entity.Subcategory;

import java.util.List;

public class SubcategoryRepository {
    private final SubcategoryDao subcategoryDao;

    public SubcategoryRepository() {
        this.subcategoryDao = BookReaderApp.getInstance().getAppDatabase().subcategoryDao();
    }

    public void insert(Subcategory subcategory) {
        subcategoryDao.insert(subcategory);
    }

    public List<Subcategory> getSubcategoriesForCategory(int categoryId) {
        return subcategoryDao.getByCategoryId(categoryId);
    }
}
