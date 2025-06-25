package com.example.bookreader.listeners;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.leanback.widget.Action;
import androidx.leanback.widget.OnActionClickedListener;

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
        int id = (int) action.getId();
        switch (id) {
            case 0:
                Toast.makeText(context, "читати...", Toast.LENGTH_SHORT).show();
                break;

            case 1:
                Toast.makeText(context, "редагувати...", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(context, "видалити...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
