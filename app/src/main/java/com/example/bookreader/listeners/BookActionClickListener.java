package com.example.bookreader.listeners;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.leanback.widget.Action;
import androidx.leanback.widget.OnActionClickedListener;

import com.example.bookreader.constants.BookActionType;
import com.example.bookreader.data.database.entity.Book;

public class BookActionClickListener implements OnActionClickedListener {
    private final Context context;
    private final Book book;

    public BookActionClickListener(Context context, Book book) {
        this.context = context;
        this.book = book;
    }

    @Override
    public void onActionClicked(Action action) {
        BookActionType type = BookActionType.fromId(action.getId());
        if(type == null) return;
        switch (type) {
            case READ:
                Toast.makeText(context, "читати...", Toast.LENGTH_SHORT).show();
                break;

            case EDIT:
                Toast.makeText(context, "редагувати...", Toast.LENGTH_SHORT).show();
                break;
            case DELETE:
                Toast.makeText(context, "видалити...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
