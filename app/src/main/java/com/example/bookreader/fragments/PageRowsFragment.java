package com.example.bookreader.fragments;

import static com.example.bookreader.constants.Constants.INIT_ADAPTER_SIZE;
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
import androidx.leanback.widget.VerticalGridView;
import androidx.lifecycle.LifecycleOwner;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.constants.Constants;
import com.example.bookreader.extentions.ArrayBookAdapter;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.customclassses.RowItemData;
import com.example.bookreader.customclassses.RowUploadInfo;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.extentions.IconHeader;
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

    private ProgressBarManager progressBarManager;
    private ArrayObjectAdapter rowsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private  VerticalGridView gridView;

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = getVerticalGridView();
        rowsAdapter = new StableIdArrayObjectAdapter(new RowPresenterSelector());
        progressBarManager = new ProgressBarManager();
        if(getView() instanceof  ViewGroup root){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(0);
            progressBarManager.hide();
        }
        setupEventListeners();
        getPageRows();
    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    private CompletableFuture<List<ListRow>> getFavoriteRows(BookRepository bookRepo, BookPreviewPresenter itemPresenter){
        return bookRepo.getPageDataFavoriteBooksAsync(1, INIT_ADAPTER_SIZE).thenApply((booksData)->{
            List<ListRow> rows = new ArrayList<>();
            List<BookDto> books = booksData.getData();
            if(!books.isEmpty()){
                RowUploadInfo info = new RowUploadInfo(
                        booksData.getTotal(),
                        booksData.getData().size(),
                        Constants.FAVORITE_CATEGORY_ID,
                        Constants.FAVORITE_CATEGORY_ID);
                ArrayBookAdapter adapter = new ArrayBookAdapter(itemPresenter,info,gridView);
                adapter.addAll(0, books);
                rows.add(new ListRow(new IconHeader(Constants.FAVORITE_CATEGORY_ID,
                        getString(R.string.favorite),R.drawable.books_stack),
                        adapter));
            }
            return rows;
        });
    }

    private CompletableFuture<List<ListRow>> getSettingRows() {
        app.getGlobalEventListener().unSubscribe(GlobalEventType.ITEM_SELECTED_CHANGE, itemSelectChangeListenerHandler);
        return CompletableFuture.supplyAsync(() -> {
            ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(new TextIconPresenter());
            settingsAdapter.add(new TextIcon(ActionType.SETTING_1.getId(), R.drawable.settings, "Налаштування категорій"));
            settingsAdapter.add(new TextIcon(ActionType.SETTING_2.getId(), R.drawable.settings, "Додати книгу"));
            settingsAdapter.add(new TextIcon(ActionType.SETTING_3.getId(), R.drawable.settings, "Додати папку"));
            return List.of(new ListRow(new HeaderItem(Constants.SETTINGS_CATEGORY_ID, "Налаштування"), settingsAdapter));
        });
    }

    private CompletableFuture<List<ListRow>> getAllRowsPage(BookRepository bookRepo,BookPreviewPresenter itemPresenter, List<CategoryDto> categories){
        CompletableFuture<List<ListRow>> allBooks = bookRepo.getPageDataAllBooksAsync(1, INIT_ADAPTER_SIZE)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();
                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        RowUploadInfo info = new RowUploadInfo(
                                booksData.getTotal(),
                                booksData.getData().size(),
                                Constants.ALL_BOOKS_CATEGORY_ID,
                                Constants.ALL_BOOKS_CATEGORY_ID);
                        ArrayBookAdapter adapter = new ArrayBookAdapter(itemPresenter,info,gridView);
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID,
                                getString(R.string.all_category),R.drawable.books_stack),
                                adapter));
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> unsortedBooks = bookRepo.getPageDataUnsortedBooksAsync(1, INIT_ADAPTER_SIZE)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();

                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        RowUploadInfo info = new RowUploadInfo(
                                booksData.getTotal(),
                                booksData.getData().size(),
                                Constants.ALL_BOOKS_CATEGORY_ID,
                                Constants.UNSORTED_BOOKS_CATEGORY_ID);
                        ArrayBookAdapter adapter = new ArrayBookAdapter(itemPresenter,info,gridView);
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getString(R.string.unsorted_category)), adapter));
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> categoriesBooks = CompletableFuture.supplyAsync(()->{
            List<CompletableFuture<ListRow>> futures = new ArrayList<>();
            for (CategoryDto cat : categories) {
                if(cat.parentId == null){
                    CompletableFuture<ListRow> futureRow = bookRepo.getPageDataAllBooksInCategoryIdAsync(cat.id,1, INIT_ADAPTER_SIZE)
                            .thenApply(booksData -> {
                                List<BookDto> books = booksData.getData();
                                if (!books.isEmpty()) {
                                    RowUploadInfo info = new RowUploadInfo(
                                            booksData.getTotal(),
                                            booksData.getData().size(),
                                            Constants.ALL_BOOKS_CATEGORY_ID,
                                            cat.id);
                                    ArrayBookAdapter adapter = new ArrayBookAdapter(itemPresenter,info,gridView);
                                    adapter.addAll(0, books);
                                    return new ListRow(new HeaderItem(cat.id, cat.name), adapter);
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

    private CompletableFuture<List<ListRow>> getOtherPageRows(BookRepository bookRepo,
                                                              BookPreviewPresenter itemPresenter,String category,
                                                              List<CategoryDto> categories){
        Optional<CategoryDto> optionalCategory = categories.stream().filter(x -> x.name.equals(category)).findFirst();
        if (optionalCategory.isPresent()) {
            CategoryDto selectedCategory = optionalCategory.get();
            CompletableFuture<List<ListRow>> allCategoryBooks = bookRepo.getPageDataAllBooksInCategoryIdAsync(selectedCategory.id,1, INIT_ADAPTER_SIZE)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        if (books != null && !books.isEmpty()) {
                            RowUploadInfo info = new RowUploadInfo(
                                    booksData.getTotal(),
                                    booksData.getData().size(),
                                    selectedCategory.id,
                                    Constants.ALL_BOOKS_CATEGORY_ID);
                            ArrayBookAdapter adapter = new ArrayBookAdapter(itemPresenter,info,gridView);
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.ALL_BOOKS_CATEGORY_ID, getString(R.string.all_category)), adapter));
                        }
                        return rows;
                    });
            CompletableFuture<List<ListRow>> unsortedCategoryBooks =
                    bookRepo.getPageDataUnsortedBooksByCategoryIdAsync(selectedCategory.id,1, INIT_ADAPTER_SIZE)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        if (!books.isEmpty()) {
                            RowUploadInfo info = new RowUploadInfo(
                                    booksData.getTotal(),
                                    booksData.getData().size(),
                                    selectedCategory.id,
                                    Constants.UNSORTED_BOOKS_CATEGORY_ID);
                            ArrayBookAdapter adapter = new ArrayBookAdapter(itemPresenter,info,gridView);
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getString(R.string.unsorted_category)), adapter));
                        }
                        return rows;
                    });

            CompletableFuture<List<ListRow>> categoryBooks = CompletableFuture.supplyAsync(() -> {
                List<CategoryDto> subcategories = categories.stream()
                        .filter(c ->c.parentId != null && c.parentId == selectedCategory.id)
                        .collect(Collectors.toList());
                List<CompletableFuture<ListRow>> futures = new ArrayList<>();
                for (CategoryDto subCategory : subcategories) {

                        CompletableFuture<ListRow> futureRow = bookRepo.getPageDataAllBooksInCategoryIdAsync(subCategory.id,1, INIT_ADAPTER_SIZE)
                                .thenApply(booksData -> {
                                    List<BookDto> books = booksData.getData();
                                    if (!books.isEmpty()) {
                                        RowUploadInfo info = new RowUploadInfo(
                                                booksData.getTotal(),
                                                booksData.getData().size(),
                                                selectedCategory.id,
                                                subCategory.id);
                                        ArrayBookAdapter adapter = new ArrayBookAdapter(itemPresenter,info,gridView);
                                        adapter.addAll(0, books);
                                        return new ListRow(new HeaderItem(subCategory.id, subCategory.name), adapter);
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

    private void getPageRows() {
        String categoryName = getCurrentCategoryName();
        if (categoryName == null || categoryName.isEmpty()) return;
        BookRepository bookRepo = new BookRepository();
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();
        CompletableFuture<List<ListRow>> future ;


        if (getString(R.string.settings).equals(categoryName)) {
            future = getSettingRows();
        }
        else{
            progressBarManager.show();
            if(getString(R.string.favorite).equals(categoryName)){
                future = getFavoriteRows(bookRepo,itemPresenter);
            }
            else{
                List<CategoryDto> categories = app.getCategoriesCash().stream()
                        .filter(c->c.booksCount > 0).collect(Collectors.toList());

                if(categoryName.equals(getString(R.string.all_category))){
                    future = getAllRowsPage(bookRepo,itemPresenter,categories);
                }
                else{
                    future = getOtherPageRows(bookRepo,itemPresenter, categoryName,categories);
                }
            }
        }

        future.thenAccept(listRows -> {
            if (!isAdded() || isDetached()) return;
            requireActivity().runOnUiThread(() -> {
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
        LifecycleOwner  owner = getViewLifecycleOwner();
        app.getGlobalEventListener().subscribe(owner,GlobalEventType.BOOK_DELETED,bookDeletedHandler,RowItemData.class);
        app.getGlobalEventListener().subscribe(owner,GlobalEventType.ITEM_SELECTED_CHANGE,itemSelectChangeListenerHandler,RowItemData.class);
        app.getGlobalEventListener().subscribe(owner,GlobalEventType.BOOK_UPDATED,bookUpdatedHandler,BookDto.class);
        app.getGlobalEventListener().subscribe(owner,GlobalEventType.BOOK_FAVORITE_UPDATED, bookFavoriteUpdateHandler,BookDto.class);
    }

    private final Consumer<BookDto> bookFavoriteUpdateHandler = (book)->{
//        if(!(book instanceof BookDto updatedBook)) return;
//        Long selectedCategoryId = app.getSelectedParentCategoryHeader().getId();
//        if(selectedCategoryId == Constants.FAVORITE_CATEGORY_ID){
//            if(rowsAdapter.size() == 0) return;
//            Object row = rowsAdapter.get(0);
//            if (row instanceof ListRow listRow) {
//                RowUploadInfo info =  rowsUploadInfo.get(Constants.FAVORITE_CATEGORY_ID);
//                if(info != null && listRow.getAdapter() instanceof ArrayObjectAdapter adapter) {
//                    if (!updatedBook.isFavorite) {
//                        if(adapter.size() == 1){
//                            rowsUploadInfo.remove(Constants.FAVORITE_CATEGORY_ID);
//                            rowsAdapter.remove(row);
//                            app.getGlobalEventListener().sendEvent(GlobalEventType.NEED_DELETE_CATEGORY,Constants.FAVORITE_CATEGORY_ID);
//                            return;
//                        }
//                        adapter.remove(book);
//                        info.setMaxElements(info.getMaxElements() - 1);
//                        if(adapter.size() == INIT_ADAPTER_SIZE - 1 && adapter.size() < info.getMaxElements()) {
//                             loadRowBooks(Constants.FAVORITE_CATEGORY_ID,Constants.FAVORITE_CATEGORY_ID,(info.getStartUploadPage() + INIT_PAGES_COUNT -1)*UPLOAD_SIZE ,1)
//                                    .thenAccept((books)->{
//                                        requireActivity().runOnUiThread(()->{
//                                            adapter.add(rowsAdapter.size(),books.get(0));
//                                        });
//                                    });
//                        }
//                    }
//                    else {
//                        adapter.add(0,book);
//                        adapter.removeItems(adapter.size(),1);
//                        info.setMaxElements(info.getMaxElements() + 1);
//                    }
//
//                }
//            }
//        }


   };

    private final Consumer<BookDto> bookUpdatedHandler = (updatedBook)->{
        for (int i = 0; i < rowsAdapter.size(); i++){
            if(!(rowsAdapter.get(i) instanceof ListRow row)
                    || !(row.getAdapter() instanceof ArrayObjectAdapter adapter)) return;
            int index = adapter.indexOf(updatedBook);
            if(index > 0){
                adapter.replace(index,updatedBook);
                adapter.notifyArrayItemRangeChanged(index,1);
            }
        }
    };

    private final Consumer<RowItemData> bookDeletedHandler = (bookData)->{
        List<ListRow> rowsToDelete = new ArrayList<>();
        BookDto book = bookData.getBook();
        for (int i = 0; i < rowsAdapter.size(); i++) {
            if (!(rowsAdapter.get(i) instanceof ListRow row) || !(row.getAdapter() instanceof ArrayBookAdapter rowAdapter)) return;
            if(rowAdapter.indexOf(book) != -1){
                if (rowAdapter.size() == 1) {
                    rowsToDelete.add(row);
                }
                else {
                    rowAdapter.remove(book);
                }
            }
        }
        if (!rowsToDelete.isEmpty()) {
            for (ListRow row : rowsToDelete) {
                rowsAdapter.remove(row);
            }
            if (rowsAdapter.size() == 0) {
                app.updateCategoryCash();
            }
        }
    };

    private final Consumer<RowItemData> itemSelectChangeListenerHandler = (rowBookData)->{
         if( !(rowBookData.getRow().getAdapter() instanceof ArrayBookAdapter bookAdapter)) return;
         bookAdapter.paginateRow(rowBookData.getBook());
    };

    private  String getCurrentCategoryName(){
        return getArguments() != null ? getArguments().getString("category") : "";
    }
}
