package com.example.bookreader.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.extentions.RowPresenterSelector;
import com.example.bookreader.extentions.StableIdArrayObjectAdapter;
import com.example.bookreader.listeners.ItemViewClickedListener;
import com.example.bookreader.presenters.BookPreviewPresenter;
import com.example.bookreader.presenters.TextIconPresenter;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PageRowsFragment extends RowsSupportFragment {

    ProgressBarManager progressBarManager;
    ArrayObjectAdapter rowsAdapter;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rowsAdapter = new StableIdArrayObjectAdapter(new RowPresenterSelector());
        rowsAdapter.setHasStableIds(true);
        setupEventListeners();
        loadCategoryRows();
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBarManager = new ProgressBarManager();
        ViewGroup root = (ViewGroup) getView();
        if(root != null){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(0);
        }

    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    private void removeBookFromCurrentCategory(ArrayObjectAdapter rowsAdapter, Book bookToRemove) {
        for (int i = 0; i < rowsAdapter.size(); i++) {
            Object item = rowsAdapter.get(i);
            if (item instanceof ListRow) {
                ListRow row = (ListRow) item;
                ArrayObjectAdapter rowAdapter = (ArrayObjectAdapter) row.getAdapter();
                if (rowAdapter != null) {
                   rowAdapter.remove(bookToRemove);
                }
            }
        }
    }

    private void loadCategoryRows() {
         String category = getCurrentCategoryName();
        if (category != null && category.isEmpty()) return;
        progressBarManager.show();
        BookRepository bookRepo = new BookRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();

        categoryRepo.getAllParentCategoriesAsyncCF()
                .thenCompose(categories -> {
                    List<CompletableFuture<ListRow>> futures = new ArrayList<>();

                    if ("Всі".equals(category)) {

                        futures.add(bookRepo.getAllBookAsyncCF()
                                .thenApply(books -> {
                                    if(books != null && !books.isEmpty()){
                                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                        adapter.addAll(0, books);
                                        return new ListRow(new HeaderItem(111111111111111L, "Всі"), adapter);
                                    }
                                    else {
                                        return null;
                                    }
                                }));

                        futures.add(bookRepo.getAllUnsortedBooksAsyncCF()
                                .thenApply(books -> {
                                    if(books != null && !books.isEmpty()){
                                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                        adapter.addAll(0, books);
                                        return new ListRow(new HeaderItem(111111111111111L + 1, "Не сортовані"), adapter);
                                    }
                                    else{
                                        return null;
                                    }
                                }));

                        for (Category cat : categories) {
                            futures.add(bookRepo.getAllBooksByCategoryIdAsyncCF(cat.id)
                                    .thenApply(books -> {
                                        if(books != null && !books.isEmpty()){
                                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                            adapter.addAll(0, books);
                                            return new ListRow(new HeaderItem(cat.id + 222222222222222L, cat.name), adapter);
                                        }
                                        else{
                                            return null;
                                        }
                                    }));
                        }
                        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                .thenApply(v -> futures.stream()
                                        .map(CompletableFuture::join)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList()));
                    } else if ("Налаштування".equals(category)) {
                        ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(new TextIconPresenter());
                        settingsAdapter.add(new TextIcon(ActionType.SETTING_1.getId(), R.drawable.settings, "Налаштування категорій"));
                        settingsAdapter.add(new TextIcon(ActionType.SETTING_2.getId(), R.drawable.settings, "Додати книгу"));
                        settingsAdapter.add(new TextIcon(ActionType.SETTING_3.getId(), R.drawable.settings, "Додати папку"));
                        return CompletableFuture.completedFuture(List.of(new ListRow(new HeaderItem(10101, "Налаштування"), settingsAdapter)));
                    } else {

                        Optional<Category> optionalCategory = categories.stream().filter(x -> x.name.equals(category)).findFirst();
                        if (optionalCategory.isPresent()) {
                            Category selectedCategory = optionalCategory.get();
                            futures.add(bookRepo.getAllBooksByCategoryIdAsyncCF(selectedCategory.id)
                                    .thenApply(books -> {
                                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                        adapter.addAll(0, books);
                                        return new ListRow(new HeaderItem(1233454456, "Всі"), adapter);
                                    }));

                            futures.add(bookRepo.getBooksByCategoryIdAsyncCF(selectedCategory.id)
                                    .thenApply(books -> {
                                        if(books != null && !books.isEmpty()){
                                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                            adapter.addAll(0, books);
                                            return new ListRow(new HeaderItem(12389456, "Не сортовані"), adapter);
                                        }
                                        else{
                                            return null;
                                        }
                                    }));

                            categoryRepo.getAllSubcategoriesByParentIdAsyncCF(selectedCategory.id)
                                    .thenAccept(subcategories->{
                                        for (Category subCategory: subcategories){
                                            futures.add(bookRepo.getBooksByCategoryIdAsyncCF(subCategory.id)
                                                    .thenApply(books -> {
                                                        if(books != null && !books.isEmpty()){
                                                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                                            adapter.addAll(0, books);
                                                            return new ListRow(new HeaderItem(subCategory.id, subCategory.name), adapter);
                                                        }
                                                        else{
                                                            return null;
                                                        }
                                                    }));
                                        }
                                    });
                            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                    .thenApply(v -> futures.stream()
                                            .map(CompletableFuture::join)
                                            .collect(Collectors.toList()));


                        }else {
                            return CompletableFuture.completedFuture(List.of());
                        }
                    }
                }).thenAccept(listRows -> {
                        requireActivity().runOnUiThread(() -> {
                        rowsAdapter.addAll(0, listRows);
                        if (isAdded()) setAdapter(rowsAdapter);
                        progressBarManager.hide();
                    });
                });
    }

    private void setupEventListeners(){
        setOnItemViewClickedListener(new ItemViewClickedListener(this));
        BookReaderApp.getInstance().getGlobalEventListener().subscribe(GlobalEventType.BOOK_DELETED,(Object book)->{
            if(book instanceof Book){
                removeBookFromCurrentCategory(rowsAdapter,(Book)book);
            }
        });
    }

    private  String getCurrentCategoryName(){
        return getArguments() != null ? getArguments().getString("category") : "";
    }


}
