package com.example.bookreader.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRowPresenter;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;

import com.example.bookreader.R;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.presenters.IconCategoryItemPresenter;

import java.util.Objects;

public class MainFragment extends BrowseSupportFragment {
   // private BackgroundManager mBackgroundManager;
  //  private Drawable mDefaultBackground;
  //  private DisplayMetrics mMetrics;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

      //  prepareBackgroundManager();
        setupUIElements();
        setupRows();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());
        setupEventListeners();
    }

    private void setupUIElements(){
        CategoryRepository repo = new CategoryRepository();
        repo.getCategoryByIdAsync(1,category ->{
            if(category != null){
                setTitle(category.name);
            }
        });
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(requireContext(), R.color.default_background));
        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconCategoryItemPresenter();
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
        //Зміна заголовку відповідно до обраної категорії
        HeadersSupportFragment supportFragment = getHeadersSupportFragment();
        if(supportFragment != null)
        {
            supportFragment.setOnHeaderViewSelectedListener(new HeadersSupportFragment.OnHeaderViewSelectedListener() {
                @Override
                public void onHeaderSelected(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
                    if (row != null && row.getHeaderItem() != null) {
                        String title = row.getHeaderItem().getName();
                        setTitle(title);
                        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getAdapter();
                        int position = adapter.indexOf(row);
                        if (position >= 0) {
                            setSelectedPosition(position, true);
                        }
                    }
                }
            });
        }

    }

    private void setupRows() {
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new ListRowPresenter());
        CategoryRepository repo = new CategoryRepository();
        repo.getAllCategoriesAsync(categories -> {
            categories.forEach(category -> {
                adapter.add(new PageRow(new HeaderItem(category.id, category.name)));
            });
        });
        setAdapter(adapter);
    }
}