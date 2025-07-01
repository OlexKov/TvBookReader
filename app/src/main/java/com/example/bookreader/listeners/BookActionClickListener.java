package com.example.bookreader.listeners;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.OnActionClickedListener;

import com.example.bookreader.constants.ActionType;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.fragments.DeleteBookFragment;

public class BookActionClickListener implements OnActionClickedListener {
    private final Context context;
    private final BookDto book;

    public BookActionClickListener(Context context, BookDto book) {
        this.context = context;
        this.book = book;
    }

    @Override
    public void onActionClicked(Action action) {
        ActionType type = ActionType.fromId(action.getId());
        if(type == null) return;
        switch (type) {
            case BOOK_READ:
                Toast.makeText(context, "читати...", Toast.LENGTH_SHORT).show();
                break;

            case BOOK_EDIT:
                Toast.makeText(context, "редагувати...", Toast.LENGTH_SHORT).show();
                break;
            case BOOK_DELETE:
                GuidedStepSupportFragment.add(
                        ((FragmentActivity) context).getSupportFragmentManager(),
                        new DeleteBookFragment(book) // передаємо книгу
                );
                //Toast.makeText(context, "видалити...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
