package com.example.bookreader.presenters;

import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.example.bookreader.R;
import com.example.bookreader.data.database.entity.Book;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BookPreviewPresenter extends Presenter {
    private static final int WIDTH_ITEM = 300;
    private static final int HEIGHT_ITEM = 400;

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        DisplayMetrics displayMetrics = parent.getContext().getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        ImageCardView cardView = new ImageCardView(parent.getContext());
        cardView.setLayoutParams(new ViewGroup.LayoutParams((int)(width*0.2), (int)(height*0.5)));
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        cardView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.default_background));

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, @Nullable Object item) {
        Book book  = (Book)item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        assert book != null;
        cardView.setTitleText(book.name);
        cardView.setContentText("author");
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE);
        assert cardView.getMainImageView() != null;
        Glide.with(cardView.getContext())
                .load("https://picsum.photos/400?random=" + book.id)
                .into(cardView.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
