package com.example.bookreader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.customclassses.paganation.QueryFilter;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.entity.Category;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.extentions.RowPresenterSelector;
import com.example.bookreader.extentions.StableIdArrayObjectAdapter;
import com.example.bookreader.listeners.ItemViewClickedListener;
import com.example.bookreader.listeners.RowItemSelectedListener;
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
    private final int UPLOAD_THRESHOLD = 5;
    private final int PAGE_SIZE = 30;
    private final int UPLOAD_SIZE = 10;
    ProgressBarManager progressBarManager;
    ArrayObjectAdapter rowsAdapter;
    BookReaderApp app = BookReaderApp.getInstance();

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rowsAdapter = new StableIdArrayObjectAdapter(new RowPresenterSelector());
        progressBarManager = new ProgressBarManager();
        ViewGroup root = (ViewGroup) getView();
        if(root != null){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(0);
            progressBarManager.hide();
        }
        setupEventListeners();
        loadCategoryRows();
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        app.getGlobalEventListener().unSubscribe(GlobalEventType.ITEM_SELECTED_CHANGE,itemSelectChangeListenerHandler);
        app.getGlobalEventListener().unSubscribe(GlobalEventType.BOOK_DELETED,bookDeletedHandler);
    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    private void removeBookFromCurrentCategory(ArrayObjectAdapter rowsAdapter, BookDto bookToRemove) {
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

    private CompletableFuture<List<ListRow>> getSettingRows(){
        return CompletableFuture.supplyAsync(() -> {
            ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(new TextIconPresenter());
            settingsAdapter.add(new TextIcon(ActionType.SETTING_1.getId(), R.drawable.settings, "Налаштування категорій"));
            settingsAdapter.add(new TextIcon(ActionType.SETTING_2.getId(), R.drawable.settings, "Додати книгу"));
            settingsAdapter.add(new TextIcon(ActionType.SETTING_3.getId(), R.drawable.settings, "Додати папку"));
            return List.of(new ListRow(new HeaderItem(10101, "Налаштування"), settingsAdapter));
        });
    }


    private CompletableFuture<List<ListRow>> getAllRowsPage(BookRepository bookRepo,BookPreviewPresenter itemPresenter,List<CategoryDto> categories){
        CompletableFuture<List<ListRow>> allBooks = bookRepo.getBooksPageAsyncCF(1,PAGE_SIZE)
                .thenApply(books ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    if(books != null && !books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new HeaderItem(111111111111111L, "Всі"), adapter));
                    }
                    return rows;
                });

        QueryFilter filter  = new QueryFilter();
        filter.setCategoryId(-1L);
        CompletableFuture<List<ListRow>> unsortedBooks = bookRepo.getBooksPageAsyncCF(1,PAGE_SIZE,filter)
                .thenApply(books ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    if(books != null && !books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new HeaderItem(111111111111111L + 1, "Не сортовані"), adapter));
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> categoriesBooks = CompletableFuture.supplyAsync(()->{
            List<CompletableFuture<ListRow>> futures = new ArrayList<>();
            for (CategoryDto cat : categories) {
                if(cat.parentId == null){
                    QueryFilter bookFilter  = new QueryFilter();
                    bookFilter.setCategoryId(cat.id);
                    bookFilter.setInParentCategoryId(cat.id);
                    CompletableFuture<ListRow> futureRow = bookRepo.getBooksPageAsyncCF(1, PAGE_SIZE, bookFilter)
                            .thenApply(books -> {
                                if (books != null && !books.isEmpty()) {
                                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                    adapter.addAll(0, books);
                                    return new ListRow(new HeaderItem(111111111111111L + cat.id, cat.name), adapter);
                                }
                                return null;
                            });
                    futures.add(futureRow);
                }
            }

            // Дочекатися завершення всіх ф’ючерсів
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)   // отримати ListRow з кожного future
                            .filter(Objects::nonNull)       // відфільтрувати null-значення
                            .collect(Collectors.toList())
                    )
                    .join();  // повертаємо List<ListRow> у supplyAsync
        });
        // Об’єднуємо всі три CompletableFuture у один, що поверне загальний список ListRow
        return CompletableFuture.allOf(allBooks, unsortedBooks, categoriesBooks)
                .thenApply(v -> {
                    List<ListRow> combinedRows = new ArrayList<>();
                    combinedRows.addAll(allBooks.join());
                    combinedRows.addAll(unsortedBooks.join());
                    combinedRows.addAll(categoriesBooks.join());
                    return combinedRows;
                });
    }


    private CompletableFuture<List<ListRow>> getOtherPageRows(BookRepository bookRepo,BookPreviewPresenter itemPresenter,String category,List<CategoryDto> categories){
        Optional<CategoryDto> optionalCategory = categories.stream().filter(x -> x.name.equals(category)).findFirst();
        if (optionalCategory.isPresent()) {
            CategoryDto selectedCategory = optionalCategory.get();

            QueryFilter filter = new QueryFilter();
            filter.setCategoryId(selectedCategory.id);
            filter.setInParentCategoryId(selectedCategory.id);
            CompletableFuture<List<ListRow>> allCategoryBooks = bookRepo.getBooksPageAsyncCF(1, PAGE_SIZE, filter)
                    .thenApply(books -> {
                        List<ListRow> rows = new ArrayList<>();
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        if (books != null && !books.isEmpty()) {
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(111111111111111L, "Всі"), adapter));
                        }
                        return rows;
                    });


            QueryFilter unsortedFilter = new QueryFilter();
            unsortedFilter.setCategoryId(selectedCategory.id);
            unsortedFilter.setInParentCategoryId(-1L);
            CompletableFuture<List<ListRow>> unsortedCategoryBooks = bookRepo.getBooksPageAsyncCF(1, PAGE_SIZE, unsortedFilter)
                    .thenApply(books -> {
                        List<ListRow> rows = new ArrayList<>();
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        if (books != null && !books.isEmpty()) {
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(111111111111111L + 1, "Не сортовані"), adapter));
                        }
                        return rows;
                    });

            CompletableFuture<List<ListRow>> categoryBooks = CompletableFuture.supplyAsync(() -> {
                List<CategoryDto> subcategories = categories.stream()
                        .filter(c ->c.parentId != null && c.parentId == selectedCategory.id)
                        .collect(Collectors.toList());
                List<CompletableFuture<ListRow>> futures = new ArrayList<>();
                for (CategoryDto subCategory : subcategories) {
                        QueryFilter bookFilter = new QueryFilter();
                        bookFilter.setCategoryId(subCategory.id);
                        bookFilter.setInParentCategoryId(subCategory.id);
                        CompletableFuture<ListRow> futureRow = bookRepo.getBooksPageAsyncCF(1, PAGE_SIZE, bookFilter)
                                .thenApply(books -> {
                                    if (books != null && !books.isEmpty()) {
                                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                        adapter.addAll(0, books);
                                        return new ListRow(new HeaderItem(111111111111111L + subCategory.id, subCategory.name), adapter);
                                    }
                                    return null;
                                });
                        futures.add(futureRow);
                }

                // Дочекатися завершення всіх ф’ючерсів
                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream()
                                .map(CompletableFuture::join)   // отримати ListRow з кожного future
                                .filter(Objects::nonNull)       // відфільтрувати null-значення
                                .collect(Collectors.toList())
                        )
                        .join();  // повертаємо List<ListRow> у supplyAsync
            });
           // Об’єднуємо всі три CompletableFuture у один, що поверне загальний список ListRow
            return CompletableFuture.allOf(allCategoryBooks, unsortedCategoryBooks, categoryBooks)
                    .thenApply(v -> {
                        List<ListRow> combinedRows = new ArrayList<>();
                        combinedRows.addAll(allCategoryBooks.join());
                        combinedRows.addAll(unsortedCategoryBooks.join());
                        combinedRows.addAll(categoryBooks.join());
                        return combinedRows;
                    });
        }
        else{
            return CompletableFuture.completedFuture(List.of());
        }
    }


    private void loadCategoryRows() {
        String category = getCurrentCategoryName();
        if (category == null || category.isEmpty()) return;
        BookRepository bookRepo = new BookRepository();
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();
        CompletableFuture<List<ListRow>> future ;

        if ("Налаштування".equals(category)) {
            future = getSettingRows();
        }
        else{
            progressBarManager.show();
            List<CategoryDto> categories = app.getCategoriesCash().stream()
                    .filter(c->c.booksCount > 0).collect(Collectors.toList());

            if(category.equals("Всі")){
               future = getAllRowsPage(bookRepo,itemPresenter,categories);
            }
            else{
                future = getOtherPageRows(bookRepo,itemPresenter,category,categories);
            }
        }

        future.thenAccept(listRows -> {
            if (!isAdded() || isDetached()) return;
            requireActivity().runOnUiThread(() -> {
                Log.d("DEBUG", "UI thread update");
                rowsAdapter.addAll(0, listRows);
                setAdapter(rowsAdapter);
                progressBarManager.hide();
            });
        }).exceptionally(ex -> {
            Log.e("ERROR", "Exception in future: " + ex.getMessage(), ex);
            requireActivity().runOnUiThread(() -> progressBarManager.hide());
            return null;
        });
    }

    private void setupEventListeners(){
        setOnItemViewClickedListener(new ItemViewClickedListener(this));
        setOnItemViewSelectedListener(new RowItemSelectedListener());
        app.getGlobalEventListener().subscribe(GlobalEventType.BOOK_DELETED,bookDeletedHandler);
        app.getGlobalEventListener().subscribe(GlobalEventType.ITEM_SELECTED_CHANGE,itemSelectChangeListenerHandler);

    }

    private final Consumer<Object> bookDeletedHandler = (book)->{
        if(book instanceof BookDto){
            removeBookFromCurrentCategory(rowsAdapter,(BookDto)book);
        }
    };
    private final Consumer<Object> itemSelectChangeListenerHandler = (book)->{
        Log.d("UploadLog","ITEM_SELECTED_CHANGEd " );
        if(book instanceof BookDto){
            BookDto selectedBook = (BookDto) book;
            ListRow selectedRow = app.getSelectedRow();
            if(selectedRow != null){
                ObjectAdapter objectAdapter = selectedRow.getAdapter();
                if(objectAdapter instanceof ArrayObjectAdapter){
                    ArrayObjectAdapter adapter = (ArrayObjectAdapter)objectAdapter;
                    boolean needUpload = false;

                    for (int i = 0; i < adapter.size(); i++) {
                        if(adapter.get(i) == selectedBook){
                            needUpload = (adapter.size() - i + 1) <= UPLOAD_THRESHOLD;
                            break;
                        }
                    }
                    if(needUpload){
                        Log.d("UploadLog","need upload " );
                    }
                }
            }
        }
    };

    private  String getCurrentCategoryName(){
        return getArguments() != null ? getArguments().getString("category") : "";
    }


}
