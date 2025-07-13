package com.example.bookreader.fragments;

import static com.example.bookreader.constants.Constants.INIT_ADAPTER_SIZE;
import static com.example.bookreader.constants.Constants.UPLOAD_SIZE;
import static com.example.bookreader.constants.Constants.UPLOAD_THRESHOLD;

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

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.constants.Constants;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.customclassses.RowItemData;
import com.example.bookreader.customclassses.RowUploadInfo;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.data.database.paganation.QueryFilter;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PageRowsFragment extends RowsSupportFragment {

    private ProgressBarManager progressBarManager;
    private ArrayObjectAdapter rowsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private final ConcurrentHashMap<Long, RowUploadInfo> rowsUploadInfo = new ConcurrentHashMap<>();
    private final BookRepository bookRepository = new BookRepository();

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    public void onDestroyView() {
        super.onDestroyView();
        app.getGlobalEventListener().unSubscribe(GlobalEventType.ITEM_SELECTED_CHANGE, itemSelectChangeListenerHandler);
        app.getGlobalEventListener().unSubscribe(GlobalEventType.BOOK_DELETED, bookDeletedHandler);
        app.getGlobalEventListener().unSubscribe(GlobalEventType.BOOK_UPDATED,bookUpdatedHandler);
        app.getGlobalEventListener().unSubscribe(GlobalEventType.BOOK_FAVORITE_UPDATED, bookFavoriteUpdateHandler);
    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    private CompletableFuture<List<ListRow>> getFavoriteRows(BookRepository bookRepo,
                                                             ConcurrentHashMap<Long, RowUploadInfo> rowsUploadInfo,
                                                             BookPreviewPresenter itemPresenter){
        return bookRepo.getPageDataFavoriteBooksAsync(1, INIT_ADAPTER_SIZE).thenApply((booksData)->{
            List<ListRow> rows = new ArrayList<>();
            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
            List<BookDto> books = booksData.getData();
            if(!books.isEmpty()){
                adapter.addAll(0, books);
                rows.add(new ListRow(new IconHeader(Constants.FAVORITE_CATEGORY_ID,
                        getString(R.string.favorite),R.drawable.books_stack),
                        adapter));
                RowUploadInfo info = new RowUploadInfo(
                        booksData.getTotal(),
                        booksData.getData().size(),
                        Constants.FAVORITE_CATEGORY_ID,
                        Constants.FAVORITE_CATEGORY_ID);
                rowsUploadInfo.put(Constants.FAVORITE_CATEGORY_ID,info);
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

    private CompletableFuture<List<ListRow>> getAllRowsPage(BookRepository bookRepo,
                                                            ConcurrentHashMap<Long, RowUploadInfo> rowsUploadInfo,
                                                            BookPreviewPresenter itemPresenter, List<CategoryDto> categories){
        CompletableFuture<List<ListRow>> allBooks = bookRepo.getPageDataAllBooksAsync(1, INIT_ADAPTER_SIZE)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID,
                                getString(R.string.all_category),R.drawable.books_stack),
                                adapter));
                        RowUploadInfo info = new RowUploadInfo(
                                booksData.getTotal(),
                                booksData.getData().size(),
                                Constants.ALL_BOOKS_CATEGORY_ID,
                                Constants.ALL_BOOKS_CATEGORY_ID);
                        rowsUploadInfo.put(Constants.ALL_BOOKS_CATEGORY_ID,info);
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> unsortedBooks = bookRepo.getPageDataUnsortedBooksAsync(1, INIT_ADAPTER_SIZE)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getString(R.string.unsorted_category)), adapter));
                        RowUploadInfo info = new RowUploadInfo(
                                booksData.getTotal(),
                                booksData.getData().size(),
                                Constants.ALL_BOOKS_CATEGORY_ID,
                                Constants.UNSORTED_BOOKS_CATEGORY_ID);
                        rowsUploadInfo.put(Constants.UNSORTED_BOOKS_CATEGORY_ID,info);
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> categoriesBooks = CompletableFuture.supplyAsync(()->{
            List<CompletableFuture<ListRow>> futures = new ArrayList<>();
            for (CategoryDto cat : categories) {
                if(cat.parentId == null){
                    QueryFilter bookFilter  = new QueryFilter();
                    CompletableFuture<ListRow> futureRow = bookRepo.getPageDataAllBooksInCategoryIdAsync(cat.id,1, INIT_ADAPTER_SIZE)
                            .thenApply(booksData -> {
                                List<BookDto> books = booksData.getData();
                                if (!books.isEmpty()) {
                                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                    adapter.addAll(0, books);
                                    RowUploadInfo info = new RowUploadInfo(
                                            booksData.getTotal(),
                                            booksData.getData().size(),
                                            Constants.ALL_BOOKS_CATEGORY_ID,
                                            cat.id);
                                    rowsUploadInfo.put( cat.id,info);
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
                                                              ConcurrentHashMap<Long, RowUploadInfo> rowsUploadInfo,
                                                              BookPreviewPresenter itemPresenter,String category,
                                                              List<CategoryDto> categories){
        Optional<CategoryDto> optionalCategory = categories.stream().filter(x -> x.name.equals(category)).findFirst();
        if (optionalCategory.isPresent()) {
            CategoryDto selectedCategory = optionalCategory.get();
            CompletableFuture<List<ListRow>> allCategoryBooks = bookRepo.getPageDataAllBooksInCategoryIdAsync(selectedCategory.id,1, INIT_ADAPTER_SIZE)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        if (books != null && !books.isEmpty()) {
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.ALL_BOOKS_CATEGORY_ID, getString(R.string.all_category)), adapter));
                            RowUploadInfo info = new RowUploadInfo(
                                    booksData.getTotal(),
                                    booksData.getData().size(),
                                    selectedCategory.id,
                                    Constants.ALL_BOOKS_CATEGORY_ID);
                            rowsUploadInfo.put( Constants.ALL_BOOKS_CATEGORY_ID,info);
                        }
                        return rows;
                    });
            CompletableFuture<List<ListRow>> unsortedCategoryBooks =
                    bookRepo.getPageDataUnsortedBooksByCategoryIdAsync(selectedCategory.id,1, INIT_ADAPTER_SIZE)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        if (!books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getString(R.string.unsorted_category)), adapter));
                            RowUploadInfo info = new RowUploadInfo(
                                    booksData.getTotal(),
                                    booksData.getData().size(),
                                    selectedCategory.id,
                                    Constants.UNSORTED_BOOKS_CATEGORY_ID);
                            rowsUploadInfo.put(Constants.UNSORTED_BOOKS_CATEGORY_ID,info);
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
                                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                        adapter.addAll(0, books);
                                        RowUploadInfo info = new RowUploadInfo(
                                                booksData.getTotal(),
                                                booksData.getData().size(),
                                                selectedCategory.id,
                                                subCategory.id);
                                        rowsUploadInfo.put( subCategory.id,info);
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
                future = getFavoriteRows(bookRepo,rowsUploadInfo,itemPresenter);
            }
            else{
                List<CategoryDto> categories = app.getCategoriesCash().stream()
                        .filter(c->c.booksCount > 0).collect(Collectors.toList());

                if(categoryName.equals(getString(R.string.all_category))){
                    future = getAllRowsPage(bookRepo,rowsUploadInfo,itemPresenter,categories);
                }
                else{
                    future = getOtherPageRows(bookRepo,rowsUploadInfo,itemPresenter, categoryName,categories);
                }
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
        app.getGlobalEventListener().subscribe(GlobalEventType.BOOK_UPDATED,bookUpdatedHandler);
        app.getGlobalEventListener().subscribe(GlobalEventType.BOOK_FAVORITE_UPDATED, bookFavoriteUpdateHandler);
    }

    private final Consumer<Object> bookFavoriteUpdateHandler = (book)->{
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

    private final Consumer<Object> bookUpdatedHandler = (book)->{
        if(!(book instanceof BookDto updatedBook)) return;
        for (int i = 0; i < rowsAdapter.size(); i++){
            if(!(rowsAdapter.get(i) instanceof ListRow row)
                    || !(row.getAdapter() instanceof ArrayObjectAdapter adapter)) return;
            int index = adapter.indexOf(book);
            if(index > 0){
                adapter.replace(index,updatedBook);
                adapter.notifyArrayItemRangeChanged(index,1);
            }
        }
    };

    private final Consumer<Object> bookDeletedHandler = (rowBookData)->{
        if(!(rowBookData instanceof RowItemData bookData)) return;
        List<ListRow> rowsToDelete = new ArrayList<>();
        BookDto book = bookData.getBook();
        for (int i = 0; i < rowsAdapter.size(); i++) {
            if (!(rowsAdapter.get(i) instanceof ListRow row) || !(row.getAdapter() instanceof ArrayObjectAdapter rowAdapter)) return;
            if(rowAdapter.indexOf(book) != -1){
                if (rowAdapter.size() == 1) {
                    rowsToDelete.add(row);
                }
                else {
                    RowUploadInfo info = rowsUploadInfo.get(row.getId());
                    if (info != null) {
                        if(rowAdapter.remove(book)){
                            info.setMaxElements(info.getMaxElements() - 1);
                            info.setLastUploadedElementDbIndex(info.getLastUploadedElementDbIndex() - 1);
                            normalizeAdapterSize(info,rowAdapter);
                        }
                    }
                }
            }
        }
        if (!rowsToDelete.isEmpty()) {
            for (ListRow row : rowsToDelete) {
                rowsAdapter.remove(row);
                rowsUploadInfo.remove(row.getId());
            }
            if (rowsAdapter.size() == 0) {
                app.updateCategoryCash();
            }
        }
    };

    private final Consumer<Object> itemSelectChangeListenerHandler = (rowBookData)->{
         if(!(rowBookData instanceof RowItemData data)) return;
         paginateRow( data.getRow(),data.getBook(),rowsUploadInfo);
    };

    private void normalizeAdapterSize(RowUploadInfo info,ArrayObjectAdapter adapter) {
        if(INIT_ADAPTER_SIZE <= info.getMaxElements() && adapter.size() < INIT_ADAPTER_SIZE ){
            boolean next = info.getLastUploadedElementDbIndex() < info.getMaxElements();
            int firstUploadedElementDbIndex =  info.getLastUploadedElementDbIndex() - INIT_ADAPTER_SIZE;
            int offset = next ? info.getLastUploadedElementDbIndex() : Math.max(0,firstUploadedElementDbIndex );
            updateAdapter(adapter, info, next, offset, 1);
        }
    }

    private void paginateRow(ListRow selectedRow, BookDto selectedBook,ConcurrentHashMap<Long, RowUploadInfo> rowsUploadInfo){
        if(selectedRow != null && selectedRow.getAdapter() instanceof ArrayObjectAdapter adapter){
            RowUploadInfo info = rowsUploadInfo.get(selectedRow.getId());
            int currentFocusPosition = adapter.indexOf(selectedBook);
            boolean nextThreshold = currentFocusPosition + UPLOAD_THRESHOLD >= INIT_ADAPTER_SIZE - 1;
            if(info == null || info.getMaxElements() <= INIT_ADAPTER_SIZE || info.isLoading()
                    || (!nextThreshold  && currentFocusPosition - UPLOAD_THRESHOLD > 0 )) return;
            int firstUploadedElementDbIndex =  info.getLastUploadedElementDbIndex() - INIT_ADAPTER_SIZE;
            int upload_size = nextThreshold ? UPLOAD_SIZE : Math.min(UPLOAD_SIZE, firstUploadedElementDbIndex);
            boolean needUpload = nextThreshold ? info.getLastUploadedElementDbIndex() < info.getMaxElements() : firstUploadedElementDbIndex >= 1;
            if(needUpload){
                int offset = nextThreshold ?  info.getLastUploadedElementDbIndex() : Math.max(0, firstUploadedElementDbIndex - UPLOAD_SIZE);
                info.setLoading(true);
                updateAdapter( adapter, info, nextThreshold,  offset, upload_size);
            }
        }
    }

    private void updateAdapter(ArrayObjectAdapter adapter,RowUploadInfo info, boolean next ,int offset, int upload_size){
        loadRowBooks(info.getMainCategoryId(),info.getRowCategoryId(),offset,upload_size).thenAccept(books->{
            if(!books.isEmpty()){
                requireActivity().runOnUiThread(() -> {
                    int booksCount = books.size();
                    if(next){
                        adapter.addAll(adapter.size(), books);
                        if(adapter.size() > INIT_ADAPTER_SIZE){
                            adapter.removeItems(0,booksCount);
                        }
                        info.setLastUploadedElementDbIndex(info.getLastUploadedElementDbIndex() + booksCount);
                    }
                    else{
                        adapter.addAll(0, books);
                        if(adapter.size() > INIT_ADAPTER_SIZE){
                            adapter.removeItems(adapter.size() - booksCount, booksCount);
                            info.setLastUploadedElementDbIndex(info.getLastUploadedElementDbIndex() - booksCount);
                        }
                    }
                    getView().post(()->{
                        info.setLoading(false);
                    });
                });
            }
        }).exceptionally(ex->{
            Log.e("ERROR", "Exception in future: " + ex.getMessage(), ex);
            return null;
        });
    }

    private CompletableFuture<List<BookDto>> loadRowBooks(Long mainCategoryId,Long rowCategoryId,int offset, int size) {
        if(mainCategoryId == Constants.FAVORITE_CATEGORY_ID || rowCategoryId == Constants.FAVORITE_CATEGORY_ID ){
            return bookRepository.getRangeFavoriteBooksAsync(offset,size);
        }
        else if (mainCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
            if (rowCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return bookRepository.getRangeAllBooksAsync(offset,size);
            } else if (rowCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return bookRepository.getRangeUnsortedBooksAsync(offset,size);
            } else {
                return bookRepository.getRangeAllBooksInCategoryIdAsync( rowCategoryId,offset,size);
            }
        }
        else {
            if (rowCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return bookRepository.getRangeAllBooksInCategoryIdAsync( mainCategoryId,offset,size);
            } else if (rowCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return bookRepository.getRageUnsortedBooksByCategoryIdAsync(mainCategoryId,offset,size);
            } else {
                return bookRepository.getRangeAllBooksInCategoryIdAsync( mainCategoryId,offset,size);
            }
        }
    }

    private  String getCurrentCategoryName(){
        return getArguments() != null ? getArguments().getString("category") : "";
    }
}
