package com.example.bookreader.listeners;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.activities.BookDetailsActivity;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.data.database.entity.Book;

public class ItemViewClickedListener implements OnItemViewClickedListener {
    RowsSupportFragment fragment;
    Activity activity;
    public ItemViewClickedListener(RowsSupportFragment fragment){
        this.fragment = fragment;
        activity = fragment.requireActivity();

    }
    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Book) {
            Book book = (Book) item;

            // Наприклад: відкриття нової Activity
            Intent intent = new Intent(activity, BookDetailsActivity.class);
            intent.putExtra("BOOK", book);
            activity.startActivity(intent);
        }
        else if(item instanceof TextIcon){
            TextIcon textIcon = (TextIcon) item;
            ActionType actionType = ActionType.fromId(textIcon.id);
            switch (actionType){
                case SETTING_1:
                    Toast.makeText(activity, textIcon.name, Toast.LENGTH_SHORT).show();
                    break;
                case SETTING_2:
                    Toast.makeText(activity, textIcon.name, Toast.LENGTH_SHORT).show();
                    break;
                case SETTING_3:
                    Toast.makeText(activity, textIcon.name, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(activity, "Error...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
