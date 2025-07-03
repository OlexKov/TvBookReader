package com.example.bookreader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
import com.example.bookreader.constants.GlobalEventType;
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
    private final int INIT_PAGE_SIZE = UPLOAD_SIZE*INIT_PAGES_COUNT;
    private final int UPLOAD_THRESHOLD = UPLOAD_SIZE;
    private ProgressBarManager progressBarManager;
    private ArrayObjectAdapter rowsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private final ConcurrentHashMap<Long, RowUploadInfo> rowsUploadInfo = new ConcurrentHashMap<>();
    private final BookRepository bookRepository = new BookRepository();
    private boolean updatedNow = false;

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

    private void removeBookFromCurrentCategory(BookDto bookToRemove) {
        boolean hasDeleted = false;
        List<ListRow> rowsToDelete = new ArrayList<>();
        for (int i = 0; i < rowsAdapter.size(); i++) {
            Object item = rowsAdapter.get(i);
            if (item instanceof ListRow) {
                ListRow row = (ListRow) item;
                ArrayObjectAdapter rowAdapter = (ArrayObjectAdapter) row.getAdapter();
                if (rowAdapter != null) {
                   if(rowAdapter.remove(bookToRemove)){
                       if(rowAdapter.size() == 0){
                           rowsToDelete.add(row);
                       }
                       else{
                           RowUploadInfo info = rowsUploadInfo.get(row.getId());
                           if(info != null){
                               info.setMaxElements(info.getMaxElements()-1);
                           }
                       }
                       hasDeleted = true;
                   }
                }
            }
        }
        if(hasDeleted){
            if(!rowsToDelete.isEmpty()){
                for (ListRow row:rowsToDelete){
                    rowsAdapter.remove(row);
                    rowsUploadInfo.remove(row.getId());
                }
                if(rowsAdapter.size()==0){
                     app.updateCategoryCash();
                }
            }
        }
    }

    private CompletableFuture<List<ListRow>> getSettingRows(){
        app.getGlobalEventListener().unSubscribe(GlobalEventType.ITEM_SELECTED_CHANGE,itemSelectChangeListenerHandler);
        return CompletableFuture.supplyAsync(() -> {
            ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(new TextIconPresenter());
            settingsAdapter.add(new TextIcon(ActionType.SETTING_1.getId(), R.drawable.settings, "Налаштування категорій"));
            settingsAdapter.add(new TextIcon(ActionType.SETTING_2.getId(), R.drawable.settings, "Додати книгу"));
            settingsAdapter.add(new TextIcon(ActionType.SETTING_3.getId(), R.drawable.settings, "Додати папку"));
            return List.of(new ListRow(new HeaderItem(Constants.SETTINGS_CATEGORY_ID, "Налаштування"), settingsAdapter));
        });
    }


    private CompletableFuture<List<BookDto>> loadRowBooks(int page,int size,QueryFilter filter){
        Pair<Long,Long> ids = correctIds(filter.getCategoryId(), filter.getInParentCategoryId());
        filter.setCategoryId(ids.first);
        filter.setInParentCategoryId(ids.second);
        return bookRepository.getBooksPageAsyncCF(page,size,filter);
    }

    private Pair<Long,Long> correctIds(Long parentCategoryId,Long categoryId){

        if(parentCategoryId == Constants.ALL_BOOKS_CATEGORY_ID){
            if(categoryId == Constants.ALL_BOOKS_CATEGORY_ID){
                return new Pair<>(null,null);
            }
            else if(categoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID){
                return new Pair<>(-1L,null);
            }
            else{
                return new Pair<>(categoryId,categoryId);
            }
        }
        else{
            if(categoryId == Constants.ALL_BOOKS_CATEGORY_ID){
                return new Pair<>(categoryId,categoryId);
            }
            else if(categoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID){
                return new Pair<>(categoryId,-1L);
            }
            else{
                return new Pair<>(categoryId,categoryId);
            }
        }
    }



    private CompletableFuture<List<ListRow>> getAllRowsPage(BookRepository bookRepo, BookPreviewPresenter itemPresenter, List<CategoryDto> categories){
        CompletableFuture<List<ListRow>> allBooks = bookRepo.getBooksPageDataAsyncCF(1, INIT_PAGE_SIZE)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID,
                                getContext().getString(R.string.all_category),R.drawable.books_stack),
                                adapter));
                        RowUploadInfo info = new RowUploadInfo();
                        info.setMaxElements(booksData.getTotal());
                        rowsUploadInfo.put(Constants.ALL_BOOKS_CATEGORY_ID,info);
                    }
                    return rows;
                });

        QueryFilter filter  = new QueryFilter();
        filter.setCategoryId(-1L);
        CompletableFuture<List<ListRow>> unsortedBooks = bookRepo.getBooksPageDataAsyncCF(1, INIT_PAGE_SIZE,filter)
                .thenApply(booksData ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                    List<BookDto> books = booksData.getData();
                    if(!books.isEmpty()){
                        adapter.addAll(0, books);
                        rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getContext().getString(R.string.unsorted_category)), adapter));
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
                    bookFilter.setCategoryId(cat.id);
                    bookFilter.setInParentCategoryId(cat.id);
                    CompletableFuture<ListRow> futureRow = bookRepo.getBooksPageDataAsyncCF(1, INIT_PAGE_SIZE, bookFilter)
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

            QueryFilter filter = new QueryFilter();
            filter.setCategoryId(selectedCategory.id);
            filter.setInParentCategoryId(selectedCategory.id);
            CompletableFuture<List<ListRow>> allCategoryBooks = bookRepo.getBooksPageDataAsyncCF(1, INIT_PAGE_SIZE, filter)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                        if (books != null && !books.isEmpty()) {
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.ALL_BOOKS_CATEGORY_ID, getContext().getString(R.string.all_category)), adapter));
                            RowUploadInfo info = new RowUploadInfo();
                            info.setMaxElements(booksData.getTotal());
                            rowsUploadInfo.put( Constants.ALL_BOOKS_CATEGORY_ID,info);
                        }
                        return rows;
                    });


            QueryFilter unsortedFilter = new QueryFilter();
            unsortedFilter.setCategoryId(selectedCategory.id);
            unsortedFilter.setInParentCategoryId(-1L);
            CompletableFuture<List<ListRow>> unsortedCategoryBooks = bookRepo.getBooksPageDataAsyncCF(1, INIT_PAGE_SIZE, unsortedFilter)
                    .thenApply(booksData -> {
                        List<ListRow> rows = new ArrayList<>();
                        List<BookDto> books = booksData.getData();
                        if (!books.isEmpty()) {
                            ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
                            adapter.addAll(0, books);
                            rows.add(new ListRow(new HeaderItem(Constants.UNSORTED_BOOKS_CATEGORY_ID, getContext().getString(R.string.unsorted_category)), adapter));
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
                        QueryFilter bookFilter = new QueryFilter();
                        bookFilter.setCategoryId(subCategory.id);
                        bookFilter.setInParentCategoryId(subCategory.id);
                        CompletableFuture<ListRow> futureRow = bookRepo.getBooksPageDataAsyncCF(1, INIT_PAGE_SIZE, bookFilter)
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
            removeBookFromCurrentCategory((BookDto)book);
        }
    };

    private final Consumer<Object> itemSelectChangeListenerHandler = (book)->{
        if(book instanceof BookDto && !updatedNow){
            BookDto selectedBook = (BookDto) book;
            ListRow selectedRow = app.getSelectedRow();
            if(selectedRow != null){
                RowUploadInfo info = rowsUploadInfo.get(selectedRow.getId());
                ObjectAdapter objectAdapter = selectedRow.getAdapter();
                if(objectAdapter instanceof ArrayObjectAdapter){
                    ArrayObjectAdapter adapter = (ArrayObjectAdapter)objectAdapter;
                    if(info != null && info.getMaxElements() > adapter.size()){
                        Log.d("AdapterLog",String.valueOf(adapter.size()));
                        boolean needUploadNext = false;
                        boolean needUploadPrev = false;
                        for (int i = 0; i < adapter.size(); i++) {
                            BookDto tempBook = (BookDto)adapter.get(i);
                            if(tempBook != null && tempBook.id == selectedBook.id){
                                int rightThresholdPosition = i+1 + UPLOAD_THRESHOLD;
                                int leftThresholdPosition = i+1 - UPLOAD_THRESHOLD;
                                long lastUploadedPage = (info.getStartUploadPage() + INIT_PAGES_COUNT - 1);
                                Log.d("UploadLog","max elements - " + String.valueOf(info.getMaxElements()));
                                Log.d("UploadLog","adapter count - " + String.valueOf(adapter.size()));

                                boolean canUploadNext = (lastUploadedPage * UPLOAD_SIZE) < info.getMaxElements();
                                needUploadNext = (rightThresholdPosition > adapter.size() && canUploadNext);
                                needUploadPrev = leftThresholdPosition < 0 && info.getStartUploadPage() != 1;
                                break;
                            }
                        }
                        if(needUploadNext  || needUploadPrev){

                            String categoryName = selectedRow.getHeaderItem().getName();
                            String parentCategoryName = app.getSelectedParentCategoryHeader().getName();
                            int pageSize = UPLOAD_SIZE + INIT_PAGE_SIZE - adapter.size();
                            int uploadPage = needUploadNext ? info.getStartUploadPage() + INIT_PAGES_COUNT: info.getStartUploadPage() - 1;
                            boolean finalNeedUploadNext = needUploadNext;
                            if(categoryName.equals("Всі")){
                                if(parentCategoryName.equals("Всі")){
                                    updatedNow = true;
                                    bookRepository.getBooksPageAsyncCF(uploadPage,pageSize).thenAccept(books->{
                                        requireActivity().runOnUiThread(() -> {
                                            updateRow(adapter,books,info,finalNeedUploadNext);
                                            updatedNow = false;
                                        });
                                    });
                                }
                                else{

                                }

                            }
                            else if(categoryName.equals("Не сортовані")){
                                if(parentCategoryName.equals("Всі")){

                                }
                                else{

                                }
                            }
                            else{

                            }
                        }
                    }
                }
            }
        }
    };


    private void updateRow(ArrayObjectAdapter adapter,List<BookDto> books,RowUploadInfo info,boolean next){
        if(next){
            adapter.addAll(adapter.size(), books);
            adapter.removeItems(0,UPLOAD_SIZE);
            info.setStartUploadPage(info.getStartUploadPage() + 1);
        }
        else{
            adapter.addAll(0, books);
            adapter.removeItems(adapter.size()-UPLOAD_SIZE,UPLOAD_SIZE);
            info.setStartUploadPage(info.getStartUploadPage() - 1);
        }
    }

    private  String getCurrentCategoryName(){
        return getArguments() != null ? getArguments().getString("category") : "";
    }
}
