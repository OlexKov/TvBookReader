package com.example.bookreader.presenters;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BookPreviewPresenter extends Presenter {
    private static final int WIDTH_ITEM = 300;
    private static final int HEIGHT_ITEM = 400;

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                super.setSelected(selected);

                TextView titleView = findViewById(androidx.leanback.R.id.title_text);
                if (titleView != null) {
                    titleView.setSelected(selected);  // запускає або зупиняє marquee
                }
            }
        };


        DisplayMetrics displayMetrics = parent.getContext().getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        cardView.setLayoutParams(new ViewGroup.LayoutParams((int)(width*0.17), (int)(height*0.47)));
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.default_background));

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, @Nullable Object item) {
        BookDto book  = (BookDto)item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        if(book != null && cardView != null){
            cardView.setTitleText(book.name);
            cardView.setContentText("author");
            cardView.setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE);
            // cardView.setMainImageAdjustViewBounds(true);
            // cardView.setMainImageDimensions(400, 300);
            // cardView.setCardType(BaseCardView.CARD_TYPE_INFO_OVER | BaseCardView.CARD_TYPE_FLAG_CONTENT);
            TextView titleView = cardView.findViewById(androidx.leanback.R.id.title_text);
            if (titleView != null) {
                titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                titleView.setMarqueeRepeatLimit(-1);
                titleView.setSingleLine(true);
                titleView.setHorizontallyScrolling(true);
            }
            cardView.setInfoAreaBackgroundColor(
                    ContextCompat.getColor(cardView.getContext(), android.R.color.transparent)
            );
            ImageView imageView = cardView.getMainImageView();
            if(imageView != null){
                Glide.with(cardView.getContext())
                        .load("https://picsum.photos/400?random=" + book.id)
                        .into(imageView);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
