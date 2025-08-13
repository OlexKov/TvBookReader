package com.example.bookreader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.VerticalGridView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.constants.Constants;
import com.example.bookreader.customclassses.NewCategoryList;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.extentions.ArrayBookAdapter;
import com.example.bookreader.presenters.RowCategoryItemPresenter;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.customclassses.RowItemData;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.extentions.IconHeader;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PageRowsFragment extends RowsSupportFragment {

    private ProgressBarManager progressBarManager;
    private ArrayObjectAdapter rowsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final BookRepository bookRepository = new BookRepository();
    private VerticalGridView gridView;

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = getVerticalGridView();
        var listRowPresenter = new ListRowPresenter();
        listRowPresenter.setHeaderPresenter(new RowCategoryItemPresenter());
        rowsAdapter = new StableIdArrayObjectAdapter(listRowPresenter);
        progressBarManager = new ProgressBarManager();
        if(getView() instanceof  ViewGroup root){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(100);
            progressBarManager.hide();
        }
        setupEventListeners();
        setPageRows();
    }

    @Override
    public void setExpand(boolean expand) {
        // Завжди передаємо true, щоб розгорнути заголовки
        super.setExpand(true);
    }

    @Override
    public void onStop(){
        super.onStop();
        progressBarManager.hide();
    }

    private CompletableFuture<List<ListRow>> getFavoriteRow(BookPreviewPresenter itemPresenter){
        return  CompletableFuture.supplyAsync(()->{
            List<ListRow> rows = new ArrayList<>();
            if(bookRepository.getRowBooksCount(Constants.FAVORITE_CATEGORY_ID,null) > 0){
                ArrayBookAdapter adapter = new ArrayBookAdapter(
                        itemPresenter,
                        Constants.FAVORITE_CATEGORY_ID,
                        Constants.FAVORITE_CATEGORY_ID,
                        gridView);
                rows.add(new ListRow(new IconHeader(Constants.FAVORITE_CATEGORY_ID,
                        getString(R.string.favorite),R.drawable.favorite),
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

    private CompletableFuture<List<ListRow>> getAllRowsPage(BookPreviewPresenter itemPresenter, List<CategoryDto> categories){
        CompletableFuture<List<ListRow>> allBooks = CompletableFuture
                .supplyAsync(() ->{
                    List<ListRow> rows = new ArrayList<>();
                    ArrayBookAdapter adapter = new ArrayBookAdapter(
                            itemPresenter,
                            Constants.ALL_BOOKS_CATEGORY_ID,
                            Constants.ALL_BOOKS_CATEGORY_ID,
                            gridView);
                    rows.add(new ListRow(new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID,
                            getString(R.string.all_category),R.drawable.books_stack),
                            adapter));
                    return rows;
                });

        CompletableFuture<List<ListRow>> unsortedBooks = CompletableFuture
                .supplyAsync(() ->{
                    List<ListRow> rows = new ArrayList<>();
                    if(new BookRepository().getRowBooksCount(Constants.ALL_BOOKS_CATEGORY_ID,Constants.UNSORTED_BOOKS_CATEGORY_ID) > 0) {
                        ArrayBookAdapter adapter = new ArrayBookAdapter(
                                itemPresenter,
                                Constants.ALL_BOOKS_CATEGORY_ID,
                                Constants.UNSORTED_BOOKS_CATEGORY_ID,
                                gridView);
                        rows.add(new ListRow(new IconHeader(Constants.UNSORTED_BOOKS_CATEGORY_ID, getString(R.string.unsorted_category),R.drawable.unsorted_book), adapter));
                    }
                    return rows;
                });

        CompletableFuture<List<ListRow>> categoriesBooks = CompletableFuture.supplyAsync(()->{
            List<CompletableFuture<ListRow>> futures = new ArrayList<>();
            for (CategoryDto cat : categories) {
                if(cat.parentId == null){
                    CompletableFuture<ListRow> futureRow = CompletableFuture
                            .supplyAsync(() -> {
                                ArrayBookAdapter adapter = new ArrayBookAdapter(
                                            itemPresenter,
                                            Constants.ALL_BOOKS_CATEGORY_ID,
                                            cat.id,
                                            gridView);
                                    return new ListRow(new HeaderItem(cat.id, cat.name), adapter);
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

    private CompletableFuture<List<ListRow>> getOtherPageRows(BookPreviewPresenter itemPresenter,String category,
                                                              List<CategoryDto> categories){
        Optional<CategoryDto> optionalCategory = categories.stream().filter(x -> x.name.equals(category)).findFirst();
        if (optionalCategory.isPresent()) {
            CategoryDto selectedCategory = optionalCategory.get();
            CompletableFuture<List<ListRow>> allCategoryBooks = CompletableFuture
                    .supplyAsync(() -> {
                        List<ListRow> rows = new ArrayList<>();
                        ArrayBookAdapter adapter = new ArrayBookAdapter(
                                itemPresenter,
                                selectedCategory.id,
                                Constants.ALL_BOOKS_CATEGORY_ID,
                                gridView);
                        rows.add(new ListRow(new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID, getString(R.string.all_category),R.drawable.books_stack), adapter));
                        return rows;
                    });
            CompletableFuture<List<ListRow>> unsortedCategoryBooks = CompletableFuture
                    .supplyAsync(() -> {
                        List<ListRow> rows = new ArrayList<>();
                        if(new BookRepository().getRowBooksCount(selectedCategory.id,Constants.UNSORTED_BOOKS_CATEGORY_ID) > 0) {
                            ArrayBookAdapter adapter = new ArrayBookAdapter(
                                    itemPresenter,
                                    selectedCategory.id,
                                    Constants.UNSORTED_BOOKS_CATEGORY_ID,
                                    gridView);
                            rows.add(new ListRow(new IconHeader(
                                    Constants.UNSORTED_BOOKS_CATEGORY_ID,
                                    getString(R.string.unsorted_category),
                                    R.drawable.unsorted_book),
                                    adapter));
                        }
                        return rows;
                    });

            CompletableFuture<List<ListRow>> categoryBooks = CompletableFuture.supplyAsync(() -> {
                List<CategoryDto> subcategories = categories.stream()
                        .filter(c ->c.parentId != null && c.parentId == selectedCategory.id)
                        .collect(Collectors.toList());
                List<CompletableFuture<ListRow>> futures = new ArrayList<>();
                for (CategoryDto subCategory : subcategories) {
                    CompletableFuture<ListRow> futureRow = CompletableFuture
                            .supplyAsync(() -> {
                                ArrayBookAdapter adapter = new ArrayBookAdapter(
                                        itemPresenter,
                                        selectedCategory.id,
                                        subCategory.id,
                                        gridView);
                                return new ListRow(new IconHeader(
                                        subCategory.id,
                                        subCategory.name,
                                        subCategory.iconId != 0
                                                ? subCategory.iconId
                                                : R.drawable.unsorted), adapter);
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

    private void setPageRows() {
        String categoryName = getCurrentCategoryName();
        if (categoryName == null || categoryName.isEmpty()) return;
        BookPreviewPresenter itemPresenter = new BookPreviewPresenter();
        CompletableFuture<List<ListRow>> future ;


        if (getString(R.string.settings).equals(categoryName)) {
            future = getSettingRows();
        }
        else{
            progressBarManager.show();
            if(getString(R.string.favorite).equals(categoryName)){
                future = getFavoriteRow(itemPresenter);
            }
            else{

                List<CategoryDto> categories = categoryRepository.getAllParentWithBookCountAsync().join()
                        .stream()
                        .filter(c->c.booksCount > 0).collect(Collectors.toList());

                if(categoryName.equals(getString(R.string.all_category))){
                    future = getAllRowsPage(itemPresenter,categories);
                }
                else{
                    future = getOtherPageRows(itemPresenter, categoryName,categories);
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
        app.getGlobalEventListener().subscribe(owner,GlobalEventType.BOOK_CATEGORY_CHANGED,bookCategoryChangedHandler,BookDto.class);
        app.getGlobalEventListener().subscribe(owner,GlobalEventType.BOOK_FAVORITE_UPDATED, bookFavoriteUpdateHandler,BookDto.class);
        app.getGlobalEventListener().subscribe(owner,GlobalEventType.UPDATE_CATEGORIES_COMMAND, categoriesUpdateHandler, NewCategoryList.class);
    }

    private void tryAddOrReinitCategoryRow(CategoryDto category){
        String categoryName;
        long categoryId;
        Long selectedMainCategory = app.getSelectedMainCategoryInfo().getId();
        if(selectedMainCategory == Constants.FAVORITE_CATEGORY_ID) return;
        boolean isUnsortedCategory = category == null  || Objects.equals(category.id , selectedMainCategory);

        for (int i = 0; i < rowsAdapter.size(); i++){
            if(rowsAdapter.get(i) instanceof ListRow row
                    && (isUnsortedCategory ? row.getId() == Constants.UNSORTED_BOOKS_CATEGORY_ID  : row.getId() == category.id)){
                reinitRowAdapter(row);
                return;
            }
        }

        int position = rowsAdapter.size();
        if(isUnsortedCategory){
            categoryId = Constants.UNSORTED_BOOKS_CATEGORY_ID;
            position = 1;
            categoryName = getString(R.string.unsorted_category);
        }
        else {
            categoryName = category.name;
            categoryId = category.id;
            if (selectedMainCategory == Constants.ALL_BOOKS_CATEGORY_ID) {
                if (category.parentId != null) {
                    var parentCategory = categoryRepository.getCategoryByIdAsyncCF(category.parentId).join();
                    if (parentCategory != null) {
                        categoryId = parentCategory.id;
                        categoryName = parentCategory.name;
                    }
                }
            }
            app.getGlobalEventListener().sendEvent(
                    GlobalEventType.TRY_ADD_MAIN_CATEGORY_COMMAND,
                    category.parentId != null
                            ? category.parentId
                            : category.id);
        }
        ArrayBookAdapter adapter = new ArrayBookAdapter(
                new BookPreviewPresenter(),
                selectedMainCategory,
                categoryId,
                gridView);
        rowsAdapter.add(position,new ListRow(new IconHeader(categoryId, categoryName,R.drawable.unsorted_book), adapter));
    }

    private  String getCurrentCategoryName(){
        return getArguments() != null ? getArguments().getString("category") : "";
    }

    private void updateRowCountLabel(ListRow listRow,long newCount){
        int index = rowsAdapter.indexOf(listRow);
        if(app.getSelectedRow().getId() == listRow.getId()){
            VerticalGridView headerGridView = getVerticalGridView();
            RecyclerView.ViewHolder headerVH = headerGridView.findViewHolderForAdapterPosition(index);
            if (headerVH != null) {
                View headerView = headerVH.itemView;
                TextView countView = headerView.findViewById(R.id.header_book_count);
                if (countView != null) {
                    countView.setText(String.valueOf(newCount));
                }
            }
        }
        else {
            rowsAdapter.notifyItemRangeChanged(index,1);
        }

    }

    private void removeBook(Object row,BookDto book,boolean favoriteCheck){
        if(row instanceof ListRow listRow && listRow.getAdapter() instanceof ArrayBookAdapter adapter){
            if(adapter.indexOf(book) != -1){
                if (adapter.size() == 1 ) {
                    if(rowsAdapter.size() == 1 && app.getSelectedMainCategoryInfo().getId() != Constants.ALL_BOOKS_CATEGORY_ID){
                        app.getGlobalEventListener().sendEvent(GlobalEventType.DELETE_MAIN_CATEGORY_COMMAND,app.getSelectedMainCategoryInfo().getId());
                    }
                    else{
                        rowsAdapter.remove(listRow);
                        if(app.getSelectedMainCategoryInfo().getId() == Constants.ALL_BOOKS_CATEGORY_ID){
                            app.getGlobalEventListener().sendEvent(GlobalEventType.REMOVE_IS_EMPTY_MAIN_CATEGORY_COMMAND,listRow.getId());
                        }
                    }
                }
                else {
                    if(adapter.remove(book)){
                        if(favoriteCheck){
                            app.getGlobalEventListener().sendEvent(GlobalEventType.REMOVE_IS_EMPTY_MAIN_CATEGORY_COMMAND,Constants.FAVORITE_CATEGORY_ID);
                        }
                        updateRowCountLabel( listRow,adapter.getDbElementsCount());
                    }
                }
            }
        }
    }

    private void updateBook(ArrayBookAdapter adapter,BookDto updatedBook){
        int index = adapter.indexOf(updatedBook);
        if(index >= 0){
            adapter.replace(index,updatedBook);
            adapter.notifyArrayItemRangeChanged(index,1);
        }
    }

    private void reinitRowAdapter(ListRow row){
        if(row.getAdapter() instanceof ArrayBookAdapter adapter){
            adapter.reinit();
            updateRowCountLabel(row,adapter.getDbElementsCount());
        }
    }

    private final Consumer<BookDto> bookCategoryChangedHandler = (updatedBook)->{
        boolean isSameMainCategory;
        CategoryDto newCategory;
        Long currentMainCategory = app.getSelectedMainCategoryInfo().getId();
        if(currentMainCategory == Constants.FAVORITE_CATEGORY_ID) return;
        if(updatedBook.categoryId != null){
            newCategory = categoryRepository.getCategoryByIdAsyncCF(updatedBook.categoryId).join();
            if(newCategory != null) {
                isSameMainCategory = currentMainCategory == Constants.ALL_BOOKS_CATEGORY_ID || newCategory.id == currentMainCategory || Objects.equals(newCategory.parentId, currentMainCategory );
            } else {
                isSameMainCategory = false;
            }
        }
        else{
            newCategory = null;
            isSameMainCategory = currentMainCategory == Constants.ALL_BOOKS_CATEGORY_ID;
        }

        new ArrayList<>(rowsAdapter.unmodifiableList()).forEach(rowList-> {
            if (rowList instanceof ListRow row && row.getAdapter() instanceof ArrayBookAdapter adapter) {
                var oldBook = adapter.unmodifiableList().stream()
                        .filter(book -> book instanceof BookDto bk && bk.id == updatedBook.id)
                        .findFirst().orElse(null);
                if(oldBook != null){
                    if (!isSameMainCategory || row.getId() != Constants.ALL_BOOKS_CATEGORY_ID) {
                        if (oldBook instanceof BookDto bookDto) {
                           removeBook(row, bookDto, false);
                        }
                    }
                    else {
                        updateBook(adapter,updatedBook);
                    }
                }
            }
        });
        tryAddOrReinitCategoryRow(newCategory);
    };

    private final Consumer<NewCategoryList> categoriesUpdateHandler = (categoriesList)->{
        var currentMainCategory = app.getSelectedMainCategoryInfo().getId();
        if(currentMainCategory != Constants.FAVORITE_CATEGORY_ID || currentMainCategory != Constants.SETTINGS_CATEGORY_ID ){
            if(rowsAdapter.get(0) instanceof  ListRow row){
                reinitRowAdapter(row);
            }
        }
        if(!categoriesList.categories.isEmpty()){
            categoriesList.categories.forEach(this::tryAddOrReinitCategoryRow);
        }
        else{
            tryAddOrReinitCategoryRow(null);
        }
    };

    private final Consumer<BookDto> bookFavoriteUpdateHandler = (book)->{
        var favoriteRow = rowsAdapter.unmodifiableList().stream()
                .filter(listRow->listRow instanceof ListRow row && row.getId() == Constants.FAVORITE_CATEGORY_ID)
                .findFirst().orElse(null);
        if(favoriteRow != null){
            if(((ListRow)favoriteRow).getAdapter() instanceof ArrayBookAdapter adapter){
                if(adapter.size() == 1 && !book.isFavorite){
                    app.getGlobalEventListener().sendEvent(GlobalEventType.DELETE_MAIN_CATEGORY_COMMAND,Constants.FAVORITE_CATEGORY_ID);
                }
                else if(app.getSelectedMainCategoryInfo().getId() == Constants.FAVORITE_CATEGORY_ID) {
                    reinitRowAdapter((ListRow) favoriteRow);
                }
            }
        }
        else{
            app.getGlobalEventListener().sendEvent(GlobalEventType.TRY_ADD_MAIN_CATEGORY_COMMAND,Constants.FAVORITE_CATEGORY_ID);
        }
    };

    private final Consumer<BookDto> bookUpdatedHandler = (updatedBook)->{
       for (int i = 0; i < rowsAdapter.size(); i++){
            if(!(rowsAdapter.get(i) instanceof ListRow row) || !(row.getAdapter() instanceof ArrayBookAdapter adapter)) return;
            updateBook(adapter,updatedBook);
        }
    };

    private final Consumer<RowItemData> bookDeletedHandler = (bookData)->{
        BookDto book = bookData.getBook();
        removeBook(rowsAdapter.get(0),book,true);
        rowsAdapter.unmodifiableList().stream().filter(row->
                        row instanceof ListRow listRow
                        && listRow.getAdapter() instanceof ArrayBookAdapter adapter
                        && adapter.unmodifiableList().contains(book)
        ).findFirst().ifPresent(row->removeBook(row,book,true));
    };

    private final Consumer<RowItemData> itemSelectChangeListenerHandler = (rowBookData)->{
         if( !(rowBookData.getRow().getAdapter() instanceof ArrayBookAdapter bookAdapter)) return;
         bookAdapter.tryPaginateRow(rowBookData.getBook());
    };
}
