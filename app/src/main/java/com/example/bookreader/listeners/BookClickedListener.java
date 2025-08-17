package com.example.bookreader.listeners;

import android.app.Activity;
import android.content.Intent;

import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.example.bookreader.activities.BookDetailsActivity;
import com.example.bookreader.data.database.dto.BookDto;

public class BookClickedListener implements OnItemViewClickedListener {
    private final Activity activity ;
    public BookClickedListener(Activity activity){
          this.activity = activity;
    }
    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof BookDto book){
            if(activity instanceof BookDetailsActivity bookDetailsActivity){
               bookDetailsActivity.changeBook(book.id);
            }
            else{
                Intent intent = new Intent(activity, BookDetailsActivity.class);
                intent.putExtra("BOOK_ID", book.id);
                activity.startActivity(intent);
            }
        }
    }
}
