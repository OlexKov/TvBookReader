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
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.data.database.dao.CategoryDao;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.extentions.CustomTitleView;
import com.example.bookreader.extentions.IconHeader;
import com.example.bookreader.data.database.repository.CategoryRepository;
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
         setTitle("Всі");
         //  prepareBackgroundManager();
        setupUIElements();
        setupCategoryRows();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        app.getGlobalEventListener().unSubscribe(GlobalEventType.CATEGORY_CASH_UPDATED,categoryCashUpdateHandler);
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
        if(supportFragment != null) {
            supportFragment.setOnHeaderViewSelectedListener(new HeaderViewSelectedListener(this));
        }

       View titleView  = getTitleView();
        if(titleView instanceof CustomTitleView){
            CustomTitleView customeView = (CustomTitleView) titleView;
            customeView.setOnButton1ClickListener((v)->Toast.makeText(getContext(),"Натиснута кнопка 1", Toast.LENGTH_SHORT).show());
            customeView.setOnButton2ClickListener((v)->Toast.makeText(getContext(),"Натиснута кнопка 2", Toast.LENGTH_SHORT).show());
            customeView.setOnButton3ClickListener((v)->Toast.makeText(getContext(),"Натиснута кнопка 3", Toast.LENGTH_SHORT).show());
            customeView.setButton1Icon(R.drawable.books_stack);
        }
        app.getGlobalEventListener().subscribe(GlobalEventType.CATEGORY_CASH_UPDATED,categoryCashUpdateHandler);
    }

    private final Consumer<Object> categoryCashUpdateHandler = (object)->{
        if(rowsAdapter.size() > 0){
            setupCategoryRows();
        }
    };

    private void setupCategoryRows() {
        rowsAdapter.clear();
        rowsAdapter.add(new PageRow( new IconHeader(123123123, "Всі",R.drawable.books_stack)));
        for (CategoryDto category:app.getCategoriesCash()){
            if(category.booksCount > 0 && category.parentId == null){
                IconHeader header = new IconHeader(category.id, category.name,category.iconId);
                rowsAdapter.add(new PageRow(header));
            }
        }
        rowsAdapter.add(new DividerRow());
        rowsAdapter.add(new PageRow(new IconHeader(100000, "Налаштування",R.drawable.settings)));
        requireActivity().runOnUiThread(() -> { setAdapter(rowsAdapter);});
    }
}