package com.example.bookreader.presenters;

import android.content.Context;
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
import com.example.bookreader.utility.AnimHelper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BookPreviewPresenter extends Presenter {

    private static class ViewSize {
       public int width;
       public int height;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(setCardView(parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
        if(!(item instanceof BookDto book) || !(viewHolder.view instanceof ImageCardView cardView)) return;
        Context context = cardView.getContext();
        cardView.setTitleText(book.title);
        cardView.setContentText(book.author);
        TextView titleView = cardView.findViewById(androidx.leanback.R.id.title_text);
        if (titleView != null) {
            titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleView.setMarqueeRepeatLimit(-1);
            titleView.setSingleLine(true);
            titleView.setHorizontallyScrolling(true);
        }
        cardView.setInfoAreaBackgroundColor(
                ContextCompat.getColor(context, android.R.color.transparent)
        );
        ImageView imageView = cardView.getMainImageView();
        if(imageView != null){
            ViewSize size = getSize(cardView.getContext());
            String previewSource = book.previewPath != null && !book.previewPath.isEmpty()
                    ? book.previewPath
                    : "https://picsum.photos/400?random=" + book.id;
            Glide.with(context)
                    .load(previewSource)
                    .override(size.width, size.height)
                    .into(imageView);
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        if (viewHolder.view instanceof ImageCardView cardView && cardView.getMainImageView() != null) {
            Glide.with(cardView.getContext().getApplicationContext()).clear(cardView.getMainImageView());
        }
    }

    private ViewSize getSize( Context context){
        ViewSize size = new ViewSize();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        size.width = (int)(displayMetrics.widthPixels / 7);
        size.height = (int)(displayMetrics.heightPixels / 3);
        return size;
    }

    private ImageCardView setCardView(ViewGroup parent){
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
        Context context = parent.getContext();
        ViewSize size = getSize(context);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background));
        cardView.setMainImageScaleType(ImageView.ScaleType.FIT_XY);
        cardView.setMainImageDimensions(size.width, size.height);

        ViewGroup.MarginLayoutParams layoutParams =
                new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
       // layoutParams.setMargins(0, 0, AnimHelper.convertToPx(context,3), 0);
      //  cardView.setLayoutParams(layoutParams);
        return cardView;
    }
}
