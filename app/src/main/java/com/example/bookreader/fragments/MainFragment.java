package com.example.bookreader.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DividerPresenter;
import androidx.leanback.widget.DividerRow;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.extentions.CustomTitleView;
import com.example.bookreader.extentions.IconHeader;
import com.example.bookreader.data.database.repository.CategoryRepository;
import com.example.bookreader.extentions.RowPresenterSelector;
import com.example.bookreader.extentions.StableIdArrayObjectAdapter;
import com.example.bookreader.listeners.BrowserTransitionListener;
import com.example.bookreader.listeners.HeaderButtonOnFocusListener;
import com.example.bookreader.listeners.HeaderButtonOnKeyListener;
import com.example.bookreader.listeners.HeaderViewSelectedListener;
import com.example.bookreader.presenters.IconCategoryItemPresenter;

public class MainFragment extends BrowseSupportFragment {
   // private BackgroundManager mBackgroundManager;
  //  private Drawable mDefaultBackground;
  //  private DisplayMetrics mMetrics;
   private ArrayObjectAdapter rowsAdapter;


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

    private void setupUIElements(){
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(requireContext(), R.color.default_background));
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
            customeView.setOnButton1Icon(R.drawable.books_stack);
        }
    }

    private void setupCategoryRows() {
        CategoryRepository repo = new CategoryRepository();

        rowsAdapter.add(new PageRow( new IconHeader(123123123, "Всі",R.drawable.books_stack)));
        repo.getAllParentCategoriesAsyncCF().thenAccept(categories -> {
            categories.forEach(category -> {
                IconHeader header = new IconHeader(category.id, category.name,R.drawable.books_stack);
                rowsAdapter.add(new PageRow(header));
            });
            rowsAdapter.add(new DividerRow());
            rowsAdapter.add(new PageRow(new IconHeader(100000, "Налаштування",R.drawable.settings)));
            requireActivity().runOnUiThread(() -> { setAdapter(rowsAdapter);});
        });
    }
}