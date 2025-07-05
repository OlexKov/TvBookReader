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
import androidx.leanback.widget.ObjectAdapter;

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
    private final int INIT_PAGES_COUNT = 3;
    private final int UPLOAD_SIZE = 5;
    private final int INIT_ADATTER_SIZE = UPLOAD_SIZE * INIT_PAGES_COUNT;
    private final int UPLOAD_THRESHOLD = UPLOAD_SIZE;
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
    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
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

    private CompletableFuture<List<ListRow>> getAllRowsPage(BookRepository bookRepo, BookPreviewPresenter itemPresenter, List<CategoryDto> categories){
        CompletableFuture<List<ListRow>> allBooks = bookRepo.getPageDataAllBooksAsync(1, INIT_ADATTER_SIZE)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID,
                                getString(R.string.all_category),R.drawable.books_stack),
                                adapter));
                        RowUploadInfo info = new RowUploadInfo();
                        info.setMaxElements(booksData.getTotal());
                        rowsUploadInfo.put(Constants.ALL_BOOKS_CATEGORY_ID,info);
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> unsortedBooks = bookRepo.getPageDataUnsortedBooksAsync(1, INIT_ADATTER_SIZE)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getString(R.string.unsorted_category)), adapter));
                        RowUploadInfo info = new RowUploadInfo();
                        info.setMaxElements(booksData.getTotal());
                        rowsUploadInfo.put(Constants.UNSORTED_BOOKS_CATEGORY_ID,info);
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> categoriesBooks = CompletableFuture.supplyAsync(()->{
            List<CompletableFuture<ListRow>> futures = new ArrayList<>();
            for (CategoryDto cat : categories) {
                if(cat.parentId == null){
                    QueryFilter bookFilter  = new QueryFilter();
                    CompletableFuture<ListRow> futureRow = bookRepo.getPageDataAllBooksInCategoryIdAsync(cat.id,1, INIT_ADATTER_SIZE)
                            .thenApply(booksData -> {
                                List<BookDto> books = booksData.getData();
                                if (!books.isEmpty()) {
                                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                    adapter.addAll(0, books);
                                    RowUploadInfo info = new RowUploadInfo();
                                    info.setMaxElements(booksData.getTotal());
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

    private CompletableFuture<List<ListRow>> getOtherPageRows(BookRepository bookRepo,BookPreviewPresenter itemPresenter,String category,List<CategoryDto> categories){
        Optional<CategoryDto> optionalCategory = categories.stream().filter(x -> x.name.equals(category)).findFirst();
        if (optionalCategory.isPresent()) {
            CategoryDto selectedCategory = optionalCategory.get();
            CompletableFuture<List<ListRow>> allCategoryBooks = bookRepo.getPageDataAllBooksInCategoryIdAsync(selectedCategory.id,1, INIT_ADATTER_SIZE)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        if (books != null && !books.isEmpty()) {
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.ALL_BOOKS_CATEGORY_ID, getString(R.string.all_category)), adapter));
                            RowUploadInfo info = new RowUploadInfo();
                            info.setMaxElements(booksData.getTotal());
                            rowsUploadInfo.put( Constants.ALL_BOOKS_CATEGORY_ID,info);
                        }
                        return rows;
                    });
            CompletableFuture<List<ListRow>> unsortedCategoryBooks =
                    bookRepo.getPageDataUnsortedBooksByCategoryIdAsync(selectedCategory.id,1, INIT_ADATTER_SIZE)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        if (!books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getString(R.string.unsorted_category)), adapter));
                            RowUploadInfo info = new RowUploadInfo();
                            info.setMaxElements(booksData.getTotal());
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

                        CompletableFuture<ListRow> futureRow = bookRepo.getPageDataAllBooksInCategoryIdAsync(subCategory.id,1, INIT_ADATTER_SIZE)
                                .thenApply(booksData -> {
                                    List<BookDto> books = booksData.getData();
                                    if (!books.isEmpty()) {
                                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                                        adapter.addAll(0, books);
                                        RowUploadInfo info = new RowUploadInfo();
                                        info.setMaxElements(booksData.getTotal());
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
        String category = getCurrentCategoryName();
        if (category == null || category.isEmpty()) return;
        BookRepository bookRepo = new BookRepository();
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();
        CompletableFuture<List<ListRow>> future ;

        if (getString(R.string.settings).equals(category)) {
            future = getSettingRows();
        }
        else{
            progressBarManager.show();
            List<CategoryDto> categories = app.getCategoriesCash().stream()
                    .filter(c->c.booksCount > 0).collect(Collectors.toList());

            if(category.equals(getString(R.string.all_category))){
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
        app.getGlobalEventListener().subscribe(GlobalEventType.BOOK_UPDATED,bookUpdatedHandler);
    }

    private final Consumer<Object> bookUpdatedHandler = (book)->{
        if(!(book instanceof BookDto updatedBook)) return;
        for (int i = 0; i < rowsAdapter.size(); i++){
            ListRow row = (ListRow) rowsAdapter.get(i);
            if(row != null){
                ArrayObjectAdapter adapter = (ArrayObjectAdapter) row.getAdapter();
                if(adapter != null){
                    for (int j = 0; j < adapter.size(); j++) {
                        BookDto currentBook = (BookDto) adapter.get(j);
                        if(currentBook != null && currentBook.id == updatedBook.id){
                            adapter.replace(j,updatedBook);
                            adapter.notifyArrayItemRangeChanged(j,1);
                            break;
                        }
                    }
                }
            }
        }
    };

    private final Consumer<Object> bookDeletedHandler = (rowBookData)->{
        if(!(rowBookData instanceof RowItemData bookData)) return;
        List<ListRow> rowsToDelete = new ArrayList<>();
        for (int i = 0; i < rowsAdapter.size(); i++) {
            Object item = rowsAdapter.get(i);
            if (!(rowsAdapter.get(i) instanceof ListRow row) || !(row.getAdapter() instanceof ArrayObjectAdapter rowAdapter)) return;
            if(rowAdapter.indexOf(bookData.getBook())!= -1){
                if (rowAdapter.size() == 1) {
                    rowsToDelete.add(row);
                } else {
                    RowUploadInfo info = rowsUploadInfo.get(row.getId());
                    if (info != null) {
                        if(rowAdapter.remove(bookData.getBook())){
                            info.setMaxElements(info.getMaxElements() - 1);
                            if(rowAdapter.size() == INIT_ADATTER_SIZE - 1 && rowAdapter.size() < info.getMaxElements()) {
                                Long categoryId = row.getHeaderItem().getId();
                                Long parentCategoryId = app.getSelectedParentCategoryHeader().getId();
                                loadRowBooks(parentCategoryId,categoryId,(info.getStartUploadPage() + INIT_PAGES_COUNT -1)*UPLOAD_SIZE ,1)
                                        .thenAccept((books)->{
                                            requireActivity().runOnUiThread(()->{
                                                rowAdapter.add(rowAdapter.size(),books.get(0));
                                            });
                                        });
                            }
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
         paginateRow( data.getRow(),data.getBook());
    };

    private void paginateRow(ListRow selectedRow, BookDto selectedBook){
        if(selectedRow != null){
            RowUploadInfo info = rowsUploadInfo.get(selectedRow.getId());
            if(info != null && !info.isLoading()){
                ObjectAdapter objectAdapter = selectedRow.getAdapter();
                if(!(selectedRow.getAdapter() instanceof ArrayObjectAdapter adapter)) return;
                int currentFocusPosition = adapter.indexOf(selectedBook);
                int adapterSize = adapter.size();
                if(currentFocusPosition == info.getLastFocusPosition()) return;
                boolean isNext = currentFocusPosition > info.getLastFocusPosition();

                if(info.getMaxElements() > adapterSize){
                    boolean needUpload = false;
                    int lastUploadedPage =  info.getStartUploadPage() + INIT_PAGES_COUNT-1;
                    if(isNext){
                        boolean canUploadNext;
                        int rightThresholdPosition = adapterSize - UPLOAD_THRESHOLD;
                        canUploadNext = ((long) lastUploadedPage * UPLOAD_SIZE) < info.getMaxElements();
                        needUpload = (currentFocusPosition >= rightThresholdPosition) && canUploadNext;
                    }
                    else{
                        needUpload = UPLOAD_THRESHOLD >= currentFocusPosition && info.getStartUploadPage() != 1;
                    }

                    if(needUpload){
                        Long categoryId = selectedRow.getHeaderItem().getId();
                        Long parentCategoryId = app.getSelectedParentCategoryHeader().getId();
                        int uploadPage = isNext ?  lastUploadedPage + 1: info.getStartUploadPage() - 1;
                        info.setLoading(true);
                        QueryFilter filter = new QueryFilter();
                        filter.setCategoryId(parentCategoryId);
                        filter.setInParentCategoryId(categoryId);
                        loadRowBooks(categoryId,parentCategoryId,uploadPage,UPLOAD_SIZE).thenAccept(books->{
                            if(!books.isEmpty()){
                                requireActivity().runOnUiThread(() -> {
                                    updateRow(adapter,books,info, isNext);
                                    info.setLoading(false);
                                    info.setLastFocusPosition(adapter.indexOf(selectedBook));
                                });
                            }
                        }).exceptionally(ex->{
                            Log.e("ERROR", "Exception in future: " + ex.getMessage(), ex);
                            return null;
                        });
                    }
                    info.setLastFocusPosition(currentFocusPosition);
                }
            }
        }
    }

    private CompletableFuture<List<BookDto>> loadRowBooks(Long categoryId,Long parentCategoryId,int page, int size) {
        if (categoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
            if (parentCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return bookRepository.getPageAllBooksAsync(page,size);
            } else if (parentCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return bookRepository.getPageUnsortedBooksAsync(page,size);
            } else {
                return bookRepository.getPageAllBooksInCategoryIdAsync( parentCategoryId,page,size);
            }
        }
        else {
            if (parentCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return bookRepository.getPageAllBooksInCategoryIdAsync( categoryId,page,size);
            } else if (parentCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return bookRepository.getPageUnsortedBooksBuCategoryIdAsync(categoryId,page,size);
            } else {
                return bookRepository.getPageAllBooksInCategoryIdAsync( categoryId,page,size);
            }
        }

    }

    private void updateRow(ArrayObjectAdapter adapter,List<BookDto> books,RowUploadInfo info,boolean next){
        if(next){
            adapter.addAll(adapter.size(), books);
            if(adapter.size() > INIT_ADATTER_SIZE){
                adapter.removeItems(0,UPLOAD_SIZE);
                info.setStartUploadPage(info.getStartUploadPage() + 1);
            }
        }
        else{
            adapter.addAll(0, books);
            if(adapter.size() > INIT_ADATTER_SIZE){
                adapter.removeItems(adapter.size()-UPLOAD_SIZE,UPLOAD_SIZE);
                info.setStartUploadPage(info.getStartUploadPage() - 1);
            }
        }
    }

    private  String getCurrentCategoryName(){
        return getArguments() != null ? getArguments().getString("category") : "";
    }
}
