package com.example.bookreader.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import com.example.bookreader.R;
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
   private  ArrayObjectAdapter rowsAdapter;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rowsAdapter = new StableIdArrayObjectAdapter(new RowPresenterSelector());
         setupEventListeners();
         setTitle("Всі");
        addHeaderButtons(view);
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
        //Зміна заголовку відповідно до обраної категорії
        HeadersSupportFragment supportFragment = getHeadersSupportFragment();
        if(supportFragment != null)
        {
            supportFragment.setOnHeaderViewSelectedListener(new HeaderViewSelectedListener(this));
        }
        setBrowseTransitionListener(new BrowserTransitionListener());
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

    private void addHeaderButtons(View view){
        View titleView = view.findViewById(androidx.leanback.R.id.browse_title_group);
        if (titleView instanceof ViewGroup) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            View customTitle = inflater.inflate(R.layout.header_image_button, (ViewGroup) titleView, false);
            ((ViewGroup)titleView).addView(customTitle);

            ImageButton btn1 = customTitle.findViewById(R.id.button1);
            ImageButton btn2 = customTitle.findViewById(R.id.button2);
            ImageButton btn3 = customTitle.findViewById(R.id.button3);

            View.OnFocusChangeListener focusListener = new HeaderButtonOnFocusListener();
            View.OnKeyListener keyListener = new HeaderButtonOnKeyListener(btn1,btn2,btn3);

            btn1.setOnKeyListener(keyListener);
            btn2.setOnKeyListener(keyListener);
            btn3.setOnKeyListener(keyListener);

            btn1.setOnFocusChangeListener(focusListener);
            btn2.setOnFocusChangeListener(focusListener);
            btn3.setOnFocusChangeListener(focusListener);

            btn1.setOnClickListener(v -> Toast.makeText(getContext(), "Кнопка 1 натиснута", Toast.LENGTH_SHORT).show());
            btn2.setOnClickListener(v -> Toast.makeText(getContext(), "Кнопка 2 натиснута", Toast.LENGTH_SHORT).show());
            btn3.setOnClickListener(v -> Toast.makeText(getContext(), "Кнопка 2 натиснута", Toast.LENGTH_SHORT).show());
            LinearLayout buttonContainer = customTitle.findViewById(R.id.button_container);



            buttonContainer.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    btn1.requestFocus();
                }
            });

        }
    }

}