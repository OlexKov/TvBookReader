package com.example.bookreader.fragments;

import android.os.Bundle;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.repository.BookRepository;
import org.jspecify.annotations.NonNull;
import java.util.List;

public class DeleteBookFragment  extends GuidedStepSupportFragment {

    private final Book book;

    public DeleteBookFragment(Book book) {
        this.book = book;
    }

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
       // Bitmap bitmap = ...;
      //  Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        return new GuidanceStylist.Guidance(
                "Видалити книгу?",
                book.name,
                "Підтвердження",
                ContextCompat.getDrawable(requireContext(), R.drawable.settings)
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(requireContext())
                .id(1)
                .title("Так, видалити")
                .build());

        actions.add(new GuidedAction.Builder(requireContext())
                .id(2)
                .title("Скасувати")
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == 1) {
            new BookRepository().deleteBook(book,rows->{
                if(rows != 0){
                    BookReaderApp.getInstance().setRowsChanget(true);
                    Toast.makeText(requireContext(), "Книгу \"" + book.name + "\" видалено", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
                else{
                    requireActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(requireContext(), "Упс...щось пішло не так...:(", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
