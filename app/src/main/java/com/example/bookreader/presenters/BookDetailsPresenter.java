package com.example.bookreader.presenters;

import android.util.Log;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.bookreader.data.database.entity.Book;

public class BookDetailsPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Book book = (Book) item;

        if (book != null) {
            viewHolder.getTitle().setText(book.name);
            viewHolder.getSubtitle().setText( " sdf sdf sdf sdf sdf sdf sdf s");
            viewHolder.getBody().setText(" sdfsfsd fsdf sdf sdfs df sdfs df");

        }

    }
}
