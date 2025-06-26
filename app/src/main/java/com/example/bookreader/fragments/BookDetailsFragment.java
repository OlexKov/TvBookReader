package com.example.bookreader.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

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
import com.example.bookreader.constants.ActionType;

import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.listeners.BookActionClickListener;
import com.example.bookreader.presenters.BookDetailsPresenter;
import com.example.bookreader.presenters.StringPresenter;

public class BookDetailsFragment  extends DetailsSupportFragment {
    private static final int DETAIL_THUMB_WIDTH = 400;
    private static final int DETAIL_THUMB_HEIGHT = 500;


    private static final String TAG = "MediaItemDetailsFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        buildDetails();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Переміщення фокусу на опис якщо зверху є рядок
        // getView().post(() -> setSelectedPosition(1, true));
    }

    private void buildDetails() {
        Book book = (Book) getActivity().getIntent().getSerializableExtra("BOOK");
        if(book == null) return;
        // Attach your media item details presenter to the row presenter:
        ArrayObjectAdapter rowsAdapter = AttachBookDetailsPresenter(book);

        setDetailsOverview(rowsAdapter,book);
        setAdititionalMediaRow(rowsAdapter);
        setAdapter(rowsAdapter);
    }

    private int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void setActions(DetailsOverviewRow detailsOverview){
        SparseArrayObjectAdapter actionAdapter = new SparseArrayObjectAdapter();
        actionAdapter.set(0, new Action(ActionType.BOOK_READ.getId(), "Читати"));
        actionAdapter.set(1, new Action(ActionType.BOOK_EDIT.getId(), "Редагувати"));
        actionAdapter.set(2, new Action(ActionType.BOOK_DELETE.getId(), "Видалити"));
        detailsOverview.setActionsAdapter(actionAdapter);
    }

    private void setOverviewImage(DetailsOverviewRow detailsOverview,ArrayObjectAdapter rowsAdapter){
        int width = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(this)
                .asBitmap()
                .load("https://picsum.photos/600/800")
                .centerCrop()
                .into(new CustomTarget<Bitmap>(width,height) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        detailsOverview.setImageBitmap(getActivity(), resource);
                        rowsAdapter.notifyArrayItemRangeChanged(0, rowsAdapter.size());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Плейсхолдер або очищення
                    }
                });
    }

    private ArrayObjectAdapter  AttachBookDetailsPresenter(Book book){
        ClassPresenterSelector selector = new ClassPresenterSelector();

        // Attach your media item details presenter to the row presenter:
        FullWidthDetailsOverviewRowPresenter rowPresenter =
                new FullWidthDetailsOverviewRowPresenter(
                        new BookDetailsPresenter());
        rowPresenter.setOnActionClickedListener(new BookActionClickListener(getContext(),book));
        selector.addClassPresenter(DetailsOverviewRow.class, rowPresenter);
        selector.addClassPresenter(ListRow.class,  new ListRowPresenter());
        return new ArrayObjectAdapter(selector);
    }

    private void setDetailsOverview( ArrayObjectAdapter rowsAdapter,Book book){
        DetailsOverviewRow detailsOverview = new DetailsOverviewRow(book);
        setActions(detailsOverview);
        setOverviewImage(detailsOverview,rowsAdapter);
        rowsAdapter.add(detailsOverview);
    }

    private void setAdititionalMediaRow( ArrayObjectAdapter rowsAdapter){
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new StringPresenter());
        listRowAdapter.add("Media Item 3");
        listRowAdapter.add("Media Item 4");
        listRowAdapter.add("Media Item 5");
        HeaderItem header = new HeaderItem(0, "Подібні книги");
        rowsAdapter.add(new ListRow(header, listRowAdapter));
    }
}
