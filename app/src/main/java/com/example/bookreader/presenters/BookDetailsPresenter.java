package com.example.bookreader.presenters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;

public class BookDetailsPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(@NonNull ViewHolder viewHolder, @NonNull Object item) {
        if(!(item instanceof BookDto book)) return;
        viewHolder.getTitle().setText(book.title);
        viewHolder.getSubtitle().setText( " sdf sdf sdf sdf sdf sdf sdf s");
        viewHolder.getBody().setText(" sdfsfsd fsdf sdf sdfs df sdfs df");
    }
}
