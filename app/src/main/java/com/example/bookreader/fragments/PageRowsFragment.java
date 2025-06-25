package com.example.bookreader.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;

import com.example.bookreader.activities.BookDetailsActivity;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.data.database.repository.SubcategoryRepository;
import com.example.bookreader.presenters.BookPreviewPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PageRowsFragment extends RowsSupportFragment {


    CategoryRepository categoryRepo = new CategoryRepository();
    BookRepository bookRepo = new BookRepository();
    SubcategoryRepository subcatRepo = new SubcategoryRepository();


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupEventListeners();
        loadCategoryRows();

    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    private void loadCategoryRows() {
        String category = getArguments() != null ? getArguments().getString("category") : "";

        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();

        categoryRepo.getAllCategoriesAsync(categories->{
            if ("Всі".equals(category)) {
                categories.forEach(cat->{
                    if(!cat.name.equals("Всі")) {
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        HeaderItem header = new HeaderItem(cat.id, cat.name);
                        bookRepo.geBooksByCategoryIdAsync(cat.id, books -> books.forEach(adapter::add));
                        rowsAdapter.add(new ListRow(header, adapter));
                    }
                });
            }
            else  {
                Optional<Category> cat = categories.stream().filter(x->x.name.equals(category)).findFirst();
                cat.ifPresent(value -> subcatRepo.getSubcategoriesForCategory(value.id,subcategories -> {
                    subcategories.forEach(subcategory -> {
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        HeaderItem header = new HeaderItem(subcategory.id, subcategory.name);
                        if(subcategory.name.equals("Всі")){
                            bookRepo.geBooksByCategoryIdAsync(cat.get().id, books -> books.forEach(adapter::add));
                        }
                        else{
                            bookRepo.geBooksBySubcategoryIdAsync(subcategory.id, books -> books.forEach(adapter::add));
                        }
                        rowsAdapter.add(new ListRow(header, adapter));
                    });
                }));
            }
        });

        // Можеш додати інші категорії так само...

        setAdapter(rowsAdapter);
    }

    private void setupEventListeners(){
        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof Book) {
                Book book = (Book) item;
                Toast.makeText(getContext(), "Натиснуто: " + book.name, Toast.LENGTH_SHORT).show();

                // Наприклад: відкриття нової Activity
                 Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
                 intent.putExtra("BOOK", book);
                 startActivity(intent);
            }
        });
    }

}
