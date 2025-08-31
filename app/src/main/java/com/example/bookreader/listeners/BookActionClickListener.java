package com.example.bookreader.listeners;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.SparseArrayObjectAdapter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.activities.BookReaderActivity;
import com.example.bookreader.activities.FileBrowserActivity;
import com.example.bookreader.constants.BookInfoActionType;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.fragments.DeleteBookFragment;
import com.example.bookreader.fragments.settings.book.EditBookFragment;
import com.example.bookreader.utility.eventlistener.GlobalEventType;


public class BookActionClickListener implements OnActionClickedListener {
    private final Context context;
    private final BookDto book;
    private final SparseArrayObjectAdapter actionsAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private boolean favoriteToggled = false;

    public BookActionClickListener(Context context, BookDto book, SparseArrayObjectAdapter actionsAdapter) {
        this.context = context;
        this.book = book;
        this.actionsAdapter = actionsAdapter;
    }

    @Override
    public void onActionClicked(Action action) {
        BookInfoActionType type = BookInfoActionType.fromId(action.getId());
        if(type == null) return;
        switch (type) {
            case BOOK_READ:
                Intent intent = new Intent(context, BookReaderActivity.class);
                intent.putExtra("book_to_read",book);
                if (context instanceof Activity activity) {
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_in_bottom, 0);
                }
                break;

            case BOOK_EDIT:
                GuidedStepSupportFragment.add(
                        ((FragmentActivity) context).getSupportFragmentManager(),
                        new EditBookFragment(book)
                );
                break;
            case BOOK_DELETE:
                GuidedStepSupportFragment.add(
                        ((FragmentActivity) context).getSupportFragmentManager(),
                        new DeleteBookFragment(book)
                );
                break;
            case BOOK_TOGGLE_FAVORITE:
                favoriteToggled = !favoriteToggled;
                int index = actionsAdapter.indexOf(action);
                book.isFavorite = !book.isFavorite;
                action.setLabel1(context.getString(
                        book.isFavorite ? R.string.remove_from_favorite : R.string.add_to_favorite));
                actionsAdapter.notifyArrayItemRangeChanged(index, 1);
                break;
        }
    }

    public void postProcessing(){
        if(favoriteToggled){
            BookRepository bookRepo = new BookRepository();
            Consumer<Long> postToggleHandler = (o)->{
                app.getGlobalEventListener().sendEvent(GlobalEventType.BOOK_FAVORITE_UPDATED,book);
            };
            if(book.isFavorite){
                bookRepo.addBookToFavorite(book.id,postToggleHandler);
            }
            else{
                bookRepo.removeBookFromFavorite(book.id,postToggleHandler);
            }
        }
    }

}
