package com.example.bookreader.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.listeners.BookActionClickListener;
import com.example.bookreader.presenters.BookDetailsPresenter;
import com.example.bookreader.presenters.CustomBookDetailsPresenter;
import com.example.bookreader.presenters.StringPresenter;
import com.example.bookreader.utility.AnimHelper;

public class BookDetailsFragment  extends DetailsSupportFragment {
    private static final int DETAIL_THUMB_WIDTH = 270;
    private static final int DETAIL_THUMB_HEIGHT = 400;


    private static final String TAG = "MediaItemDetailsFragment";
    private final SparseArrayObjectAdapter actionAdapter = new SparseArrayObjectAdapter();
    private  BookDto book;
    BookActionClickListener clickListener;

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

    private void buildDetails() {
        Object serializedBook = getActivity().getIntent().getSerializableExtra("BOOK");
        if(!(serializedBook instanceof  BookDto bookDto)) return;
        book = bookDto;
        clickListener = new BookActionClickListener(getContext(),book,actionAdapter);
        ArrayObjectAdapter rowsAdapter = AttachBookDetailsPresenter(clickListener);
        setDetailsOverview(rowsAdapter,book);
        setAdditionalMediaRow(rowsAdapter);
        setAdapter(rowsAdapter);
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
        DetailsOverviewRow detailsOverview = new DetailsOverviewRow(book);
        setActions(detailsOverview);
        setOverviewImage(detailsOverview,rowsAdapter);
        rowsAdapter.add(detailsOverview);
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
