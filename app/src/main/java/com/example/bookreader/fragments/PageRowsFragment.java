package com.example.bookreader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.data.database.entity.Category;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.listeners.ItemViewClickedListener;
import com.example.bookreader.presenters.BookPreviewPresenter;
import com.example.bookreader.presenters.TextIconPresenter;

import java.util.Optional;

public class PageRowsFragment extends RowsSupportFragment {


    CategoryRepository categoryRepo = new CategoryRepository();
    BookRepository bookRepo = new BookRepository();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupEventListeners();
        loadCategoryRows();

    }

    @Override
    public void onResume(){
        super.onResume();
        if(BookReaderApp.getInstance().getIsRowsChanget()){
            loadCategoryRows();
        }
    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    public void loadCategoryRows() {
        String category = getArguments() != null ? getArguments().getString("category") : "";
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();

        categoryRepo.getAllParentCategoriesAsync(categories->{
            if ("Всі".equals(category)) {
                bookRepo.getAllBookAsync(books->{
                    if (books != null && !books.isEmpty()) {
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        HeaderItem header = new HeaderItem(1234456, "Всі");
                        adapter.addAll(0,books);
                        rowsAdapter.add(new ListRow(header, adapter));
                    }
                });


                bookRepo.geAllUnsortedBooksAsync(books -> {
                    if (books != null && !books.isEmpty()) {
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        adapter.addAll(0,books);
                        HeaderItem header = new HeaderItem(12334456, "Не сортовані");
                        rowsAdapter.add(new ListRow(header, adapter));
                    }
                });

                categories.forEach(cat->{
                    bookRepo.geAllBooksByCategoryIdAsync(cat.id, books ->{
                        if (books != null && !books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            HeaderItem header = new HeaderItem(cat.id, cat.name);
                            adapter.addAll(0,books);
                            rowsAdapter.add(new ListRow(header, adapter));
                        }
                    });

                });
            }

            else if("Налаштування".equals(category)){
                ArrayObjectAdapter adapter = new ArrayObjectAdapter(new TextIconPresenter());
                adapter.add(new TextIcon(ActionType.SETTING_1.getId(),R.drawable.settings,"Налаштування категорій"));
                adapter.add(new TextIcon(ActionType.SETTING_2.getId(),R.drawable.settings,"Додати книгу"));
                adapter.add(new TextIcon(ActionType.SETTING_3.getId(), R.drawable.settings,"Додати папку"));
                HeaderItem header = new HeaderItem(10101, "Налаштування");
                rowsAdapter.add(new ListRow(header, adapter));
            }
            else {
                Optional<Category> cat = categories.stream().filter(x -> x.name.equals(category)).findFirst();
                cat.ifPresent(selectedCategory -> {
                    bookRepo.geAllBooksByCategoryIdAsync(selectedCategory.id, books ->{
                        if (books != null && !books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            HeaderItem header = new HeaderItem(1234456, "Всі");
                            adapter.addAll(0,books);
                            rowsAdapter.add(new ListRow(header, adapter));
                        }
                    });

                    bookRepo.getBooksByCategoryIdAsync(selectedCategory.id,books ->{
                        if (books != null && !books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            HeaderItem header = new HeaderItem(12334456, "Не сортовані");
                            adapter.addAll(0,books);
                            rowsAdapter.add(new ListRow(header, adapter));
                        }
                    });


                    categoryRepo.getAllSubcategoriesByParentIdAsync(selectedCategory.id,subcategories->{
                        subcategories.forEach(subcategory -> {
                            bookRepo.getBooksByCategoryIdAsync(subcategory.id, books ->{
                                if (books != null && !books.isEmpty()) {
                                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                    HeaderItem header = new HeaderItem(subcategory.id, subcategory.name);
                                    adapter.addAll(0,books);
                                    rowsAdapter.add(new ListRow(header, adapter));
                                }
                            });

                        });
                    });

                });

            }
        });

        // Можеш додати інші категорії так само...

        setAdapter(rowsAdapter);
    }

    private void setupEventListeners(){
        setOnItemViewClickedListener(new ItemViewClickedListener(this));
    }

}
