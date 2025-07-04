package com.example.bookreader.fragments;

import android.os.Bundle;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.customclassses.RowItemData;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;
import org.jspecify.annotations.NonNull;
import java.util.List;

public class DeleteBookFragment  extends GuidedStepSupportFragment {

    private final BookDto book;
    private final BookReaderApp app = BookReaderApp.getInstance();

    public DeleteBookFragment(BookDto book) {
        this.book = book;
    }

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
       // Bitmap bitmap = ...;
      //  Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        return new GuidanceStylist.Guidance(
                getContext().getString(R.string.q_delete_book),
                book.name,
                getContext().getString(R.string.confirmation),
                ContextCompat.getDrawable(requireContext(), R.drawable.settings)
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(requireContext())
                .id(1)
                .title(getContext().getString(R.string.yes)+", "+ getContext().getString(R.string.delete))
                .build());

        actions.add(new GuidedAction.Builder(requireContext())
                .id(2)
                .title(getContext().getString(R.string.cancel))
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == 1) {
            new BookRepository().deleteBookByIdAsyncCF(book.id).thenAccept( rows->{
                if(rows != 0){
                    app.getGlobalEventListener().sendEvent(GlobalEventType.ROW_CHANGED,null);
                    app.getGlobalEventListener().sendEvent(GlobalEventType.BOOK_DELETED,new RowItemData(app.getSelectedRow(),book));
                    Toast.makeText(requireContext(), getString(R.string.book_deleted, book.name), Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
                else{
                    requireActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(requireContext(), getString(R.string.oops_error), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
