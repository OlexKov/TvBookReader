package com.example.bookreader.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;

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
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    private void loadCategoryRows() {
        String category = getArguments() != null ? getArguments().getString("category") : "";
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();

        categoryRepo.getAllParentCategoriesAsync(categories->{
            if ("Всі".equals(category)) {

                ArrayObjectAdapter alladapter = new ArrayObjectAdapter(itemPresenter);
                HeaderItem allheader = new HeaderItem(1234456, "Всі");
                bookRepo.getAllBookAsync(books->books.forEach(alladapter::add));
                rowsAdapter.add(new ListRow(allheader,alladapter));

                ArrayObjectAdapter unsortedAdapter = new ArrayObjectAdapter(itemPresenter);
                HeaderItem unsorteheader = new HeaderItem(12334456, "Не сортовані");
                bookRepo.geAllUnsortedBooksAsync(books->books.forEach( unsortedAdapter::add));
                rowsAdapter.add(new ListRow(unsorteheader,unsortedAdapter));


                categories.forEach(cat->{
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    HeaderItem header = new HeaderItem(cat.id, cat.name);
                    bookRepo.geAllBooksByCategoryIdAsync(cat.id, books -> books.forEach(adapter::add));
                    rowsAdapter.add(new ListRow(header, adapter));
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
                    ArrayObjectAdapter alladapter = new ArrayObjectAdapter(itemPresenter);
                    HeaderItem allheader = new HeaderItem(1234456, "Всі");
                    bookRepo.geAllBooksByCategoryIdAsync(selectedCategory.id, books -> books.forEach(alladapter::add));
                    rowsAdapter.add(new ListRow(allheader, alladapter));

                    ArrayObjectAdapter unsortedAdapter = new ArrayObjectAdapter(itemPresenter);
                    HeaderItem unsorteheader = new HeaderItem(12334456, "Не сортовані");
                    bookRepo.getBooksByCategoryIdAsync(selectedCategory.id,books -> books.forEach(unsortedAdapter::add));
                    rowsAdapter.add(new ListRow(unsorteheader, unsortedAdapter));

                    categoryRepo.getAllSubcategoriesByParentIdAsync(selectedCategory.id,subcategories->{
                        subcategories.forEach(subcategory -> {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            HeaderItem header = new HeaderItem(subcategory.id, subcategory.name);
                            bookRepo.getBooksByCategoryIdAsync(subcategory.id, books -> books.forEach(adapter::add));
                            rowsAdapter.add(new ListRow(header, adapter));
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
