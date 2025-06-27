package com.example.bookreader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.VerticalGridView;

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

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PageRowsFragment extends RowsSupportFragment {

    ProgressBarManager progressBarManager;
    CategoryRepository categoryRepo = new CategoryRepository();
    BookRepository bookRepo = new BookRepository();


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupEventListeners();
        loadCategoryRows();
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBarManager = new ProgressBarManager();
        ViewGroup root = (ViewGroup) getView();
        if(root != null){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(60);
        }

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
        AtomicInteger pendingCallbacks = new AtomicInteger(0);
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        Runnable tryHideProgress = () -> {
            if (pendingCallbacks.decrementAndGet() == 0) {
                requireActivity().runOnUiThread(() -> {
                   // setAdapter(rowsAdapter);
                    progressBarManager.hide();
                });
            }
        };
        Bundle arguments =  getArguments();
        String category = arguments != null ? arguments.getString("category") : "";
        if(category != null && category.isEmpty()) return;
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();

        categoryRepo.getAllParentCategoriesAsync(categories->{

            if ("Всі".equals(category)) {
                progressBarManager.show();
                pendingCallbacks.set(2 + categories.size());
                bookRepo.getAllBookAsync(books->{
                    if (books != null && !books.isEmpty()) {
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        HeaderItem header = new HeaderItem(1234456, "Всі");
                        adapter.addAll(0,books);
                        rowsAdapter.add(new ListRow(header, adapter));
                    }
                    tryHideProgress.run();
                });

                bookRepo.geAllUnsortedBooksAsync(books -> {
                    if (books != null && !books.isEmpty()) {
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        adapter.addAll(0,books);
                        HeaderItem header = new HeaderItem(12334456, "Не сортовані");
                        rowsAdapter.add(new ListRow(header, adapter));
                    }
                    tryHideProgress.run();
                });

                for (Category cat : categories){
                    bookRepo.geAllBooksByCategoryIdAsync(cat.id, books ->{
                        if (books != null && !books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            HeaderItem header = new HeaderItem(cat.id, cat.name);
                            adapter.addAll(0,books);
                            rowsAdapter.add(new ListRow(header, adapter));
                        }
                        tryHideProgress.run();
                    });
                };
            }

            else if("Налаштування".equals(category)){
                setSettingsElements(rowsAdapter);
//               requireActivity().runOnUiThread(() -> {
//                     setAdapter(rowsAdapter);
//                });
            }
            else {
                progressBarManager.show();
                Optional<Category> cat = categories.stream().filter(x -> x.name.equals(category)).findFirst();
                cat.ifPresent(selectedCategory -> {

                    pendingCallbacks.set(3);
                    bookRepo.geAllBooksByCategoryIdAsync(selectedCategory.id, books ->{
                        if (books != null && !books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            HeaderItem header = new HeaderItem(1234456, "Всі");
                            adapter.addAll(0,books);
                            rowsAdapter.add(new ListRow(header, adapter));
                            tryHideProgress.run();
                        }
                    });

                    bookRepo.getBooksByCategoryIdAsync(selectedCategory.id,books ->{
                        if (books != null && !books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            HeaderItem header = new HeaderItem(12334456, "Не сортовані");
                            adapter.addAll(0,books);
                            rowsAdapter.add(new ListRow(header, adapter));
                        }
                        tryHideProgress.run();
                    });

                    categoryRepo.getAllSubcategoriesByParentIdAsync(selectedCategory.id,subcategories->{
                        if (subcategories.isEmpty()) {
                            tryHideProgress.run();  // Якщо підкатегорій нема — зменшуємо лічильник
                            return;
                        }
                        // Збільшуємо лічильник на кількість підкатегорій
                        pendingCallbacks.addAndGet(subcategories.size() - 1);
                        for (Category subcategory : subcategories) {

                            bookRepo.getBooksByCategoryIdAsync(subcategory.id, books ->{
                                if (books != null && !books.isEmpty()) {
                                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                    HeaderItem header = new HeaderItem(subcategory.id, subcategory.name);
                                    adapter.addAll(0,books);
                                    rowsAdapter.add(new ListRow(header, adapter));
                                }
                                tryHideProgress.run();
                            });

                        };
                        tryHideProgress.run();
                    });

                });
            }
        });
        setAdapter(rowsAdapter);
    }

    private void setupEventListeners(){
        setOnItemViewClickedListener(new ItemViewClickedListener(this));
    }

    private void setSettingsElements(ArrayObjectAdapter rowsAdapter){
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new TextIconPresenter());
        adapter.add(new TextIcon(ActionType.SETTING_1.getId(),R.drawable.settings,"Налаштування категорій"));
        adapter.add(new TextIcon(ActionType.SETTING_2.getId(),R.drawable.settings,"Додати книгу"));
        adapter.add(new TextIcon(ActionType.SETTING_3.getId(), R.drawable.settings,"Додати папку"));
        HeaderItem header = new HeaderItem(10101, "Налаштування");
        rowsAdapter.add(new ListRow(header, adapter));
    }

}
