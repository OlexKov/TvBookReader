package com.example.bookreader.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
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

    @androidx.annotation.NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                getString(R.string.q_delete_book),
                book.title,
                getString(R.string.confirmation),
                Drawable.createFromPath(book.previewPath)
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(requireContext())
                .id(1)
                .title(getString(R.string.yes)+", "+ getString(R.string.delete))
                .build());

        actions.add(new GuidedAction.Builder(requireContext())
                .id(2)
                .title(getString(R.string.cancel))
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager supportFragmentManager = requireActivity().getSupportFragmentManager();
        if (action.getId() == 1) {
            new BookRepository().deleteBookByIdAsyncCF(book.id).thenAccept( deletedRowsCount->{
                if (deletedRowsCount != 0) {
                    FileHelper.deleteFile(book.previewPath);
 //                        if(BooksArchiveReader.isArchivePath(book.filePath)){
//                            new BooksArchiveReader().deleteFileFromArchive(book.filePath);
//                        }
//                        else{
//                            FileHelper.deleteFile(book.filePath);
//                        }
                    app.getGlobalEventListener().sendEvent(GlobalEventType.ROW_CHANGED, null);
                    app.getGlobalEventListener().sendEvent(GlobalEventType.BOOK_DELETED, new RowItemData(app.getSelectedRow(), book));

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), getString(R.string.book_deleted, book.title), Toast.LENGTH_SHORT).show();
                        int backStackCount = supportFragmentManager.getBackStackEntryCount();
                        if (backStackCount < 2) {
                            requireActivity().finish();
                        } else {
                            supportFragmentManager.popBackStackImmediate(supportFragmentManager.getBackStackEntryAt(backStackCount - 2).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    });
                }
                else {
                    requireActivity().runOnUiThread(() -> {
                        supportFragmentManager.popBackStack();
                        Toast.makeText(requireContext(), getString(R.string.oops_error), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
        else {
            supportFragmentManager.popBackStack();
        }
    }
}
