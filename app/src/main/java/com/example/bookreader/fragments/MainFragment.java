package com.example.bookreader.fragments;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DividerPresenter;
import androidx.leanback.widget.DividerRow;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.lifecycle.LifecycleOwner;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.activities.FileBrowserActivity;
import com.example.bookreader.activities.NewFilesActivity;
import com.example.bookreader.constants.Constants;
import com.example.bookreader.customclassses.FileData;
import com.example.bookreader.customclassses.MainCategoryInfo;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;

import com.example.bookreader.fragments.filebrowser.BrowserMode;
import com.example.bookreader.fragments.filebrowser.BrowserResult;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.data.database.dto.CategoryDto;
import com.example.bookreader.extentions.CustomTitleView;
import com.example.bookreader.extentions.IconHeader;
import com.example.bookreader.extentions.RowPresenterSelector;
import com.example.bookreader.extentions.StableIdArrayObjectAdapter;
import com.example.bookreader.listeners.BrowserTransitionListener;
import com.example.bookreader.listeners.HeaderViewSelectedListener;
import com.example.bookreader.presenters.IconCategoryItemPresenter;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class MainFragment extends BrowseSupportFragment {
   // private BackgroundManager mBackgroundManager;
  //  private Drawable mDefaultBackground;
  //  private DisplayMetrics mMetrics;
    private ArrayObjectAdapter rowsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private ActivityResultLauncher<Intent> fileBrowserLauncher;

    private BrowserMode currentBrowserMode;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rowsAdapter = new StableIdArrayObjectAdapter(new RowPresenterSelector());
        setupEventListeners();

         //  prepareBackgroundManager();
        setupUIElements();
        setupCategoryRows();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());




        fileBrowserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        switch (currentBrowserMode){
                            case FOLDER :
                                String selectedFolderPath = result.getData().getStringExtra(BrowserResult.FOLDER_PATH.name());
                                if(selectedFolderPath != null){
                                    Log.d("BrowserResult",selectedFolderPath);
                                }
                                break;
                            case MULTIPLE_FILES:
                                List<String> selectedFilesPaths = result.getData().getStringArrayListExtra(BrowserResult.SELECTED_FILES.name());
                                if(selectedFilesPaths != null){
                                    Intent intent = new Intent(requireActivity(), NewFilesActivity.class);
                                    intent.putStringArrayListExtra("data",new ArrayList<>(selectedFilesPaths));
                                    requireActivity().startActivity(intent);
                                    requireActivity().overridePendingTransition(R.anim.slide_in_top,0);
                                }

                                break;
                            case SINGLE_FILE:
                                String selectedFilePath = result.getData().getStringExtra(BrowserResult.SELECTED_FILE_PATH.name());
                                if(selectedFilePath != null){
                                    Log.d("BrowserResult",selectedFilePath);
                                }
                                break;
                        }



                    }
                }
        );

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
            customsView.setOnButton1ClickListener((v)->{


            });

            customsView.setOnButton2ClickListener((v)->{
                currentBrowserMode = BrowserMode.MULTIPLE_FILES;
                runFileBrowser(v,currentBrowserMode);
            });

            customsView.setOnButton3ClickListener((v)->{
                currentBrowserMode = BrowserMode.FOLDER;
                runFileBrowser(v,currentBrowserMode);
            });
        }
        LifecycleOwner owner = getViewLifecycleOwner();
        app.getGlobalEventListener().subscribe(owner, GlobalEventType.CATEGORY_CASH_UPDATED,categoryCashUpdateHandler, Object.class);
        app.getGlobalEventListener().subscribe(owner, GlobalEventType.BOOK_FAVORITE_UPDATED,bookFavoriteUpdatedHandler,BookDto.class);
        app.getGlobalEventListener().subscribe(owner, GlobalEventType.NEED_DELETE_CATEGORY,categoryDeleteHandler,Long.class);
    }

    private final Consumer<Long> categoryDeleteHandler = (categoryToDeleteId)->{
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

    private final Consumer<BookDto> bookFavoriteUpdatedHandler = (favoriteBook)->{
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
        if (!app.isMenuOpen()){
            startHeadersTransition(true);
        }
        rowsAdapter.clear();
        BookRepository bookRepository = new BookRepository();
        bookRepository.getFavoriteBooksCount((count)->{

            String allName = getString(R.string.all_category);
            if(count != null && count > 0){
                String favoriteName = getString(R.string.favorite);
                var iconHeader = new IconHeader(
                        Constants.FAVORITE_CATEGORY_ID,
                        favoriteName,
                        R.drawable.books_stack);
                rowsAdapter.add(0,new PageRow( iconHeader));
                app.setSelectedMainCategoryInfo(
                        new MainCategoryInfo(0,favoriteName, R.drawable.books_stack)
                );
            }
            else{
                app.setSelectedMainCategoryInfo(
                        new MainCategoryInfo(2,allName, R.drawable.books_stack)
                );
            }
            rowsAdapter.add( new PageRow(new IconHeader(Constants.ALL_BOOKS_CATEGORY_ID,allName,R.drawable.books_stack)));
            setTitle(app.getSelectedMainCategoryInfo().getName());
            for (CategoryDto category:app.getCategoriesCash()){
                if(category.booksCount > 0 && category.parentId == null){
                    rowsAdapter.add(new PageRow(new IconHeader(category.id, category.name,category.iconId)));
                }
            }
            rowsAdapter.add(new DividerRow());
            rowsAdapter.add(new PageRow(new IconHeader(Constants.SETTINGS_CATEGORY_ID, getString(R.string.settings),R.drawable.settings)));
            setAdapter(rowsAdapter);

        });
    }

    private void runFileBrowser(View v,BrowserMode browserMode){
        if(!(v.getContext() instanceof Activity vActivity)) return;
        Intent intent = new Intent(vActivity, FileBrowserActivity.class);
        intent.putExtra("mode",browserMode);
        fileBrowserLauncher.launch(intent);
        vActivity.overridePendingTransition(R.anim.slide_in_bottom, 0);
    }


}