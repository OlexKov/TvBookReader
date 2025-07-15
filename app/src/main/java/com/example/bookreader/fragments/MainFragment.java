package com.example.bookreader.fragments;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

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

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.activities.FileBrowserActivity;
import com.example.bookreader.constants.Constants;
import com.example.bookreader.customclassses.MainCategoryInfo;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;

import com.example.bookreader.interfaces.BookProcessor;
import com.example.bookreader.utility.EpubProcessor;
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
import com.example.bookreader.utility.pdf.PdfProcessor;


import java.io.File;
import java.io.IOException;
import java.util.Objects;



public class MainFragment extends BrowseSupportFragment {
   // private BackgroundManager mBackgroundManager;
  //  private Drawable mDefaultBackground;
  //  private DisplayMetrics mMetrics;
    private ArrayObjectAdapter rowsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ProgressBarManager progressBarManager;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rowsAdapter = new StableIdArrayObjectAdapter(new RowPresenterSelector());
        setupEventListeners();
        progressBarManager = new ProgressBarManager();
        if(getView() instanceof  ViewGroup root){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(0);
            progressBarManager.hide();
        }

         //  prepareBackgroundManager();
        setupUIElements();
        setupCategoryRows();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());


        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String selectedFilePath = result.getData().getStringExtra("SELECTED_FILE_PATH");
                        File selectedFile  = new File(selectedFilePath);
                        if(!selectedFile.exists()) return;
                        BookProcessor bookProcessor = Objects.equals(FileHelper.getFileExtension(getContext(), selectedFile), "pdf")
                                ? new PdfProcessor()
                                : new EpubProcessor();


                        try {

                            progressBarManager.show();
                            bookProcessor.processFileAsync(getContext(), selectedFile).thenAccept((bookInfo) -> {
                                requireActivity().runOnUiThread(() -> {
                                    progressBarManager.hide();
                                    new android.app.AlertDialog.Builder(requireActivity())
                                            .setMessage("Title - " + bookInfo.title + "\n" +
                                                    "Author - " + bookInfo.author + "\n" +
                                                    "Pages - " + bookInfo.pageCount + "\n" +
                                                    "File - " + bookInfo.filePath + "\n" +
                                                    "Preview - " + bookInfo.previewPath + "\n")
                                            .setPositiveButton("Закрити", (dialog, which) -> {
                                                dialog.dismiss(); // закриває діалог
                                            })
                                            .setCancelable(true) // можна закрити тапом поза вікном
                                            .show();
                                });


                            });
                        } catch (IOException  e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );

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
            customsView.setOnButton1ClickListener((v)->{


            });

            customsView.setOnButton2ClickListener((v)->{
                if(!(v.getContext() instanceof Activity vActivity)) return;
                Intent intent = new Intent(vActivity, FileBrowserActivity.class);
                filePickerLauncher.launch(intent);
                vActivity.overridePendingTransition(R.anim.slide_in_bottom, 0);
            });

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
}