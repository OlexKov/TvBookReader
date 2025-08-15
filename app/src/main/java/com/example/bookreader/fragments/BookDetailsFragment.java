package com.example.bookreader.fragments;

import static com.example.bookreader.utility.ImageHelper.getBlurBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.SparseArrayObjectAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.listeners.BookActionClickListener;
import com.example.bookreader.presenters.BookDetailsPresenter;
import com.example.bookreader.presenters.CustomBookDetailsPresenter;
import com.example.bookreader.presenters.StringPresenter;
import com.example.bookreader.utility.AnimHelper;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import java.util.Objects;

public class BookDetailsFragment  extends DetailsSupportFragment {
    private static final int DETAIL_THUMB_WIDTH = 270;
    private static final int DETAIL_THUMB_HEIGHT = 400;


    private static final String TAG = "MediaItemDetailsFragment";
    private final SparseArrayObjectAdapter actionAdapter = new SparseArrayObjectAdapter();
    private BookDto book;
    private BookActionClickListener clickListener;
    private DetailsOverviewRow detailsOverviewRow;
    private ArrayObjectAdapter rowsAdapter;
    private BackgroundManager mBackgroundManager;
    private final BookReaderApp app = BookReaderApp.getInstance();

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        app.getGlobalEventListener().subscribe(getViewLifecycleOwner(),GlobalEventType.UPDATE_BOOK_DETAILS,this::bookUpdatedHandler,BookDto.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildDetails();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(clickListener != null){
            clickListener.postProcessing();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Переміщення фокусу на опис якщо зверху є рядок
        // getView().post(() -> setSelectedPosition(1, true));
    }

    private void bookUpdatedHandler(BookDto updatedBook){
            if (updatedBook == null) return;
            if (detailsOverviewRow != null) {
                detailsOverviewRow.setItem(updatedBook);
                if(!Objects.equals(updatedBook.previewPath, book.previewPath)) {
                    setOverviewImage(detailsOverviewRow, rowsAdapter);
                }
                // Оновлюємо адаптер у головному потоці UI
                requireActivity().runOnUiThread(() -> {
                    if (rowsAdapter != null) {
                        int index = rowsAdapter.indexOf(detailsOverviewRow);
                        if (index >= 0) {
                            rowsAdapter.notifyArrayItemRangeChanged(index, 1);
                        }
                    }
                    if(!Objects.equals(updatedBook.previewPath, book.previewPath)){
                        getBlurBitmap(requireContext(),book.previewPath,mBackgroundManager::setBitmap);
                    }
                });
            }
            this.book = updatedBook;
    };

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(requireActivity());
        mBackgroundManager.attach(requireActivity().getWindow());
        DisplayMetrics mMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        getBlurBitmap(requireContext(),book.previewPath,mBackgroundManager::setBitmap);
    }

    private void buildDetails() {
        Object serializedBook = getActivity().getIntent().getSerializableExtra("BOOK");
        if(!(serializedBook instanceof  BookDto bookDto)) return;
        book = bookDto;
        clickListener = new BookActionClickListener(getContext(),book,actionAdapter);
        rowsAdapter = AttachBookDetailsPresenter(clickListener);
        setDetailsOverview(rowsAdapter,book);
        setAdditionalMediaRow(rowsAdapter);
        setAdapter(rowsAdapter);
        prepareBackgroundManager();
    }

    private void setActions(DetailsOverviewRow detailsOverview){
        actionAdapter.set(ActionType.BOOK_READ.getId(), new Action(ActionType.BOOK_READ.getId(), getString(R.string.read)));
        actionAdapter.set(ActionType.BOOK_EDIT.getId(), new Action(ActionType.BOOK_EDIT.getId(), getString(R.string.edit)));
        actionAdapter.set(ActionType.BOOK_DELETE.getId(), new Action(ActionType.BOOK_DELETE.getId(), getString(R.string.delete)));
        actionAdapter.set(ActionType.BOOK_TOGGLE_FAVORITE.getId(), new Action(
                ActionType.BOOK_TOGGLE_FAVORITE.getId(),
                book.isFavorite
                ? getString(R.string.remove_from_favorite)
                : getString(R.string.add_to_favorite)
        ));
        detailsOverview.setActionsAdapter(actionAdapter);
    }

    private void setOverviewImage(DetailsOverviewRow detailsOverview,ArrayObjectAdapter rowsAdapter){
        int width = AnimHelper.convertToPx(requireContext(), DETAIL_THUMB_WIDTH);
        int height = AnimHelper.convertToPx(requireContext(), DETAIL_THUMB_HEIGHT);
        String source = book.previewPath != null && !book.previewPath.isEmpty()?book.previewPath : "https://picsum.photos/600/800";
        Glide.with(this)
                .asBitmap()
                .load(source)
                .centerCrop()
                .into(new CustomTarget<Bitmap>(width,height) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        detailsOverview.setImageBitmap(getContext(), resource);
                        rowsAdapter.notifyArrayItemRangeChanged(0, rowsAdapter.size());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Плейсхолдер або очищення
                    }
                });
    }

    private ArrayObjectAdapter  AttachBookDetailsPresenter(BookActionClickListener clickListener){
        ClassPresenterSelector selector = new ClassPresenterSelector();
        FullWidthDetailsOverviewRowPresenter rowPresenter =
                new CustomBookDetailsPresenter(new BookDetailsPresenter());
        rowPresenter.setOnActionClickedListener(clickListener);
        selector.addClassPresenter(DetailsOverviewRow.class, rowPresenter);
        selector.addClassPresenter(ListRow.class,  new ListRowPresenter());
        return new ArrayObjectAdapter(selector);
    }

    private void setDetailsOverview( ArrayObjectAdapter rowsAdapter,BookDto book){
        detailsOverviewRow = new DetailsOverviewRow(book);
        setActions(detailsOverviewRow);
        setOverviewImage(detailsOverviewRow,rowsAdapter);
        rowsAdapter.add(detailsOverviewRow);
    }

    private void setAdditionalMediaRow(ArrayObjectAdapter rowsAdapter){
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new StringPresenter());
        listRowAdapter.add("Media Item 3");
        listRowAdapter.add("Media Item 4");
        listRowAdapter.add("Media Item 5");
        HeaderItem header = new HeaderItem(0, "Подібні книги");
        rowsAdapter.add(new ListRow(header, listRowAdapter));
    }
}
