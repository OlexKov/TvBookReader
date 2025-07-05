package com.example.bookreader.listeners;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.SparseArrayObjectAdapter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.ActionType;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.fragments.DeleteBookFragment;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

public class BookActionClickListener implements OnActionClickedListener {
    private final Context context;
    private final BookDto book;
    private final SparseArrayObjectAdapter actionsAdapter;
    private final BookReaderApp app  =BookReaderApp.getInstance();

    public BookActionClickListener(Context context, BookDto book, SparseArrayObjectAdapter actionsAdapter) {
        this.context = context;
        this.book = book;
        this.actionsAdapter = actionsAdapter;
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
            case BOOK_TOGGLE_FAVORITE:
                BookRepository bookRepo = new BookRepository();
                int index = actionsAdapter.indexOf(action);
                if(!book.isFavorite){
                    bookRepo.addBookAToFavorite(book.id,(v)->{
                        action.setLabel1(context.getString(R.string.remove_from_favorite));
                        actionsAdapter.notifyArrayItemRangeChanged(index, 1);
                    });
                    book.isFavorite = true;
                }
                else{
                     bookRepo.removeBookFromFavorite(book.id,(v)-> {
                         action.setLabel1(context.getString(R.string.add_to_favorite));
                         actionsAdapter.notifyArrayItemRangeChanged(index, 1);
                     });
                    book.isFavorite = false;
                }
                app.getGlobalEventListener().sendEvent(GlobalEventType.BOOK_UPDATED,book);
                app.getGlobalEventListener().sendEvent(GlobalEventType.BOOK_FAVORITE_UPDATED,book);
                break;
        }
    }
}
