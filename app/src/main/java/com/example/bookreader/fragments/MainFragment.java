package com.example.bookreader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DividerPresenter;
import androidx.leanback.widget.DividerRow;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.VerticalGridView;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.Constants;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.extentions.CustomTitleView;
import com.example.bookreader.extentions.IconHeader;
import com.example.bookreader.extentions.RowPresenterSelector;
import com.example.bookreader.extentions.StableIdArrayObjectAdapter;
import com.example.bookreader.listeners.BrowserTransitionListener;
import com.example.bookreader.listeners.HeaderViewSelectedListener;
import com.example.bookreader.presenters.IconCategoryItemPresenter;

public class MainFragment extends BrowseSupportFragment {
   // private BackgroundManager mBackgroundManager;
  //  private Drawable mDefaultBackground;
  //  private DisplayMetrics mMetrics;
    private ArrayObjectAdapter rowsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rowsAdapter = new StableIdArrayObjectAdapter(new RowPresenterSelector());
        setupEventListeners();
        setTitle(getString(R.string.all_category));
         //  prepareBackgroundManager();
        setupUIElements();
        setupCategoryRows();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        app.getGlobalEventListener().unSubscribe(GlobalEventType.CATEGORY_CASH_UPDATED,categoryCashUpdateHandler);
        app.getGlobalEventListener().unSubscribe(GlobalEventType.BOOK_UPDATED,bookFavoriteUpdatedHandler);
        app.getGlobalEventListener().subscribe(GlobalEventType.NEED_DELETE_CATEGORY,categoryDeleteHandler);
    }

    private void setupUIElements(){
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                if (o instanceof PageRow) {
                    return new IconCategoryItemPresenter();
                }
                else  {
                    return new DividerPresenter();
                }
            }
        });
    }

//    private void prepareBackgroundManager() {
//
//        mBackgroundManager = BackgroundManager.getInstance(Objects.requireNonNull(getActivity()));
//        mBackgroundManager.attach(getActivity().getWindow());
//
//        mDefaultBackground = ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.default_background);
//        mMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
//    }


    private void setupEventListeners(){
        setBrowseTransitionListener(new BrowserTransitionListener());
        //Зміна заголовку відповідно до обраної категорії
        HeadersSupportFragment supportFragment = getHeadersSupportFragment();
        if( supportFragment != null) {
            supportFragment.setOnHeaderViewSelectedListener(new HeaderViewSelectedListener(this));
        }

        if(getTitleView() instanceof CustomTitleView customsView){
            customsView.setOnButton1ClickListener((v)->Toast.makeText(getContext(),"Натиснута кнопка 1", Toast.LENGTH_SHORT).show());
            customsView.setOnButton2ClickListener((v)->Toast.makeText(getContext(),"Натиснута кнопка 2", Toast.LENGTH_SHORT).show());
            customsView.setOnButton3ClickListener((v)->Toast.makeText(getContext(),"Натиснута кнопка 3", Toast.LENGTH_SHORT).show());
            customsView.setButton1Icon(R.drawable.books_stack);
        }
        app.getGlobalEventListener().subscribe(GlobalEventType.CATEGORY_CASH_UPDATED,categoryCashUpdateHandler);
        app.getGlobalEventListener().subscribe(GlobalEventType.BOOK_FAVORITE_UPDATED,bookFavoriteUpdatedHandler);
        app.getGlobalEventListener().subscribe(GlobalEventType.NEED_DELETE_CATEGORY,categoryDeleteHandler);
    }

    private final Consumer<Object> categoryDeleteHandler = (categoryId)->{
        if(!(categoryId instanceof  Long categoryToDeleteId)) return;
        for (int i = 0; i < rowsAdapter.size(); i++) {
            if(rowsAdapter.get(i) instanceof PageRow pageRow){
                if(pageRow.getId() == categoryToDeleteId)
                {
                    rowsAdapter.remove(pageRow);
                    if (!app.isMenuOpen()){
                        startHeadersTransition(true);
                    }
                    return;
                }
            }
        }
    };

    private final Consumer<Object> bookFavoriteUpdatedHandler = (book)->{
       if(!(book instanceof  BookDto favoriteBook)) return;
       for (int i = 0; i < rowsAdapter.size(); i++) {
            if(rowsAdapter.get(i) instanceof PageRow pageRow){
                if(pageRow.getId() == Constants.FAVORITE_CATEGORY_ID) return;
            }
        }
       if(favoriteBook.isFavorite){
          rowsAdapter.add(0,new PageRow( new IconHeader(Constants.FAVORITE_CATEGORY_ID,
                   getString(R.string.favorite),
                   R.drawable.books_stack)));
       }
    };


    private final Consumer<Object> categoryCashUpdateHandler = (object)->{
        if(rowsAdapter.size() > 0){
            setupCategoryRows();
        }
    };

    private void setupCategoryRows() {
        rowsAdapter.clear();
        BookRepository bookRepository = new BookRepository();
        bookRepository.getFavoriteBooksCount((count)->{
            if(count != null && count > 0){
                rowsAdapter.add(0,new PageRow( new IconHeader(Constants.FAVORITE_CATEGORY_ID,
                        getString(R.string.favorite),
                        R.drawable.books_stack)));
            }
        });
        rowsAdapter.add(new PageRow( new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID, getString(R.string.all_category),R.drawable.books_stack)));
        for (CategoryDto category:app.getCategoriesCash()){
            if(category.booksCount > 0 && category.parentId == null){
                IconHeader header = new IconHeader(category.id, category.name,category.iconId);
                rowsAdapter.add(new PageRow(header));
            }
        }
        rowsAdapter.add(new DividerRow());
        rowsAdapter.add(new PageRow(new IconHeader(Constants.SETTINGS_CATEGORY_ID, getString(R.string.settings),R.drawable.settings)));
        requireActivity().runOnUiThread(() -> {
            setAdapter(rowsAdapter);
            if (!app.isMenuOpen()){
                startHeadersTransition(true);
            }
        });
    }
}