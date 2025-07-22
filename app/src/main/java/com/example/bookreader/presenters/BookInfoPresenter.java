package com.example.bookreader.presenters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;
import com.example.bookreader.R;
import com.example.bookreader.utility.bookutils.BookInfo;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BookInfoPresenter extends Presenter {
    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.book_info, parent, false);
        return new Presenter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @Nullable Object item) {
        if (item instanceof BookInfo info){
            ImageView preview = viewHolder.view.findViewById(R.id.preview_image);
            TextView title = viewHolder.view.findViewById(R.id.book_preview_title);
            TextView author = viewHolder.view.findViewById(R.id.book_preview_author);
            TextView year = viewHolder.view.findViewById(R.id.book_preview_year);
            TextView pages = viewHolder.view.findViewById(R.id.book_preview_pages);
            preview.setImageBitmap(info.preview);
            author.setText(info.author);
            year.setText(info.year);
            title.setText(info.title);
            pages.setText(info.pageCount + " ст.");
        }
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

    }
}
