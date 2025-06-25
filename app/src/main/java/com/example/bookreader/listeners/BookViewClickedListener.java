package com.example.bookreader.listeners;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.example.bookreader.activities.BookDetailsActivity;
import com.example.bookreader.data.database.entity.Book;

public class BookViewClickedListener implements OnItemViewClickedListener {
    RowsSupportFragment fragment;
    Activity activity;
    public BookViewClickedListener(RowsSupportFragment fragment){
        this.fragment = fragment;
        activity = fragment.getActivity();
    }
    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Book) {
            Book book = (Book) item;
            Toast.makeText(activity, "Натиснуто: " + book.name, Toast.LENGTH_SHORT).show();

            // Наприклад: відкриття нової Activity
            Intent intent = new Intent(activity, BookDetailsActivity.class);
            intent.putExtra("BOOK", book);
            activity.startActivity(intent);
        }
    }
}
