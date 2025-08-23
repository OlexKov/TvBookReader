package com.example.bookreader.fragments;

import static com.example.bookreader.utility.ImageHelper.getBlurBitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.SparseArrayObjectAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.BookInfoActionType;

import com.example.bookreader.constants.Constants;
import com.example.bookreader.customclassses.RowItemData;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.data.database.repository.TagRepository;
import com.example.bookreader.diffcallbacks.BookDtoDiffCallback;
import com.example.bookreader.listeners.BookActionClickListener;
import com.example.bookreader.listeners.BookClickedListener;
import com.example.bookreader.presenters.BookDetailsPresenter;
import com.example.bookreader.presenters.BookPreviewPresenter;
import com.example.bookreader.presenters.CustomBookDetailsPresenter;
import com.example.bookreader.utility.AnimHelper;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BookDetailsFragment  extends DetailsSupportFragment {
    private static final int DETAIL_THUMB_WIDTH = 270;
    private static final int DETAIL_THUMB_HEIGHT = 400;


    private static final String TAG = "MediaItemDetailsFragment";
    private final SparseArrayObjectAdapter actionAdapter = new SparseArrayObjectAdapter();
    private  BookDto book;
    private BookActionClickListener clickListener;
    private DetailsOverviewRow detailsOverviewRow;
    private ArrayObjectAdapter rowsAdapter;
    private BackgroundManager mBackgroundManager;
    private CustomBookDetailsPresenter rowPresenter;
    private ArrayObjectAdapter similarBooksAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private final BookRepository bookRepository = new BookRepository();
    private final TagRepository tagRepository = new TagRepository();
    private boolean firstStart = true;
    private FragmentManager fragmentManager;

    public static BookDetailsFragment newInstance(Long bookId) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("BOOK_ID", bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = requireActivity().getSupportFragmentManager();
        getParentFragmentManager().setFragmentResultListener(
                "book_edit_result",
                this,
                (requestKey, bundle) -> {
                    var book = bundle.getSerializable("updated_book");
                    if(book instanceof BookDto updatedBook){
                        updateBookUIData(updatedBook);
                    }
                }
        );
        buildDetails();
        setOnItemViewClickedListener(new BookClickedListener(getActivity()));
        app.getGlobalEventListener().subscribe(GlobalEventType.BOOK_TAGS_CHANGED,tagsUpdateHandler,BookDto.class);
        app.getGlobalEventListener().subscribe(GlobalEventType.BOOK_DELETED,bookDeleteHandler, RowItemData.class);
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(clickListener != null){
            clickListener.postProcessing();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        app.getGlobalEventListener().unSubscribe(GlobalEventType.BOOK_TAGS_CHANGED,tagsUpdateHandler);
        app.getGlobalEventListener().unSubscribe(GlobalEventType.BOOK_DELETED,bookDeleteHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!firstStart){
            if (book == null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if(!fragmentManager.popBackStackImmediate()){
                        requireActivity().finish();
                    }
                });
            }
            else{
                getBlurBitmap(requireContext(),book.previewPath,mBackgroundManager::setBitmap);
            }
        }
        else{
            firstStart = false;
        }
    }

    private void updateBookUIData(BookDto updatedBook){
            if (updatedBook == null) return;
            if (detailsOverviewRow != null) {
                detailsOverviewRow.setItem(updatedBook);
                if(!Objects.equals(updatedBook.previewPath, book.previewPath)) {
                    setOverviewImage(detailsOverviewRow, rowsAdapter);
                }
                // Оновлюємо адаптер у головному потоці UI
                requireActivity().runOnUiThread(() -> {
                    if (rowsAdapter != null) {
                        int index = rowsAdapter.indexOf(detailsOverviewRow);
                        if (index >= 0) {
                            rowsAdapter.notifyItemRangeChanged(index, 1);
                        }
                    }
                    if(!Objects.equals(updatedBook.previewPath, book.previewPath)){
                        getBlurBitmap(requireContext(),book.previewPath,mBackgroundManager::setBitmap);
                    }
                });
            }
            this.book = updatedBook;
    };

    private final Consumer<RowItemData> bookDeleteHandler = (data) ->{
        if(book != null){
            setBook(book.id);
            setSimilarBooks();
        }
    };

    private final Consumer<BookDto> tagsUpdateHandler = this::tagsUpdated;

    private void setBook(Long bookId){
        book = bookRepository.getByIdAsync(bookId).join();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(requireActivity());
        if (!mBackgroundManager.isAttached()) {
            mBackgroundManager.attach(requireActivity().getWindow());
        }
        DisplayMetrics mMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        getBlurBitmap(requireContext(),book.previewPath,mBackgroundManager::setBitmap);
    }

    private void tagsUpdated(BookDto book){
        if(book != null){
            rowPresenter.updateTags().thenAccept(this::updateSimilarBooks);
        }
    }

    private void buildDetails() {
        Object serializedBookId = getArguments() != null
                ? getArguments().getSerializable("BOOK_ID")
                : getActivity().getIntent().getSerializableExtra("BOOK_ID");

        if(!(serializedBookId instanceof  Long bookId)) return;

        setBook(bookId);
        if(book == null) return;
        clickListener = new BookActionClickListener(getContext(),book,actionAdapter);
        rowsAdapter = AttachBookDetailsPresenter(clickListener);
        setDetailsOverview(rowsAdapter,book);
        setAdditionalMediaRow(rowsAdapter);
        setAdapter(rowsAdapter);
        prepareBackgroundManager();
    }

    private void setActions(DetailsOverviewRow detailsOverview){
        actionAdapter.set(BookInfoActionType.BOOK_READ.getId(), new Action(BookInfoActionType.BOOK_READ.getId(), getString(R.string.read)));
        actionAdapter.set(BookInfoActionType.BOOK_EDIT.getId(), new Action(BookInfoActionType.BOOK_EDIT.getId(), getString(R.string.edit)));
        actionAdapter.set(BookInfoActionType.BOOK_DELETE.getId(), new Action(BookInfoActionType.BOOK_DELETE.getId(), getString(R.string.delete)));
        actionAdapter.set(BookInfoActionType.BOOK_TOGGLE_FAVORITE.getId(), new Action(
                BookInfoActionType.BOOK_TOGGLE_FAVORITE.getId(),
                book.isFavorite
                ? getString(R.string.remove_from_favorite)
                : getString(R.string.add_to_favorite)
        ));
        detailsOverview.setActionsAdapter(actionAdapter);
    }

    private void setOverviewImage(DetailsOverviewRow detailsOverview,ArrayObjectAdapter rowsAdapter){
        int width = AnimHelper.convertToPx(requireContext(), DETAIL_THUMB_WIDTH);
        int height = AnimHelper.convertToPx(requireContext(), DETAIL_THUMB_HEIGHT);
        String source = book.previewPath != null && !book.previewPath.isEmpty()?book.previewPath : "https://picsum.photos/600/800";
        Glide.with(this)
                .asBitmap()
                .load(source)
                .centerCrop()
                .into(new CustomTarget<Bitmap>(width,height) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        detailsOverview.setImageBitmap(getContext(), resource);
                        rowsAdapter.notifyArrayItemRangeChanged(0, rowsAdapter.size());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Плейсхолдер або очищення
                    }
                });
    }

    private ArrayObjectAdapter AttachBookDetailsPresenter(BookActionClickListener clickListener){
        ClassPresenterSelector selector = new ClassPresenterSelector();
        rowPresenter = new CustomBookDetailsPresenter(new BookDetailsPresenter());
        rowPresenter.setOnActionClickedListener(clickListener);
        selector.addClassPresenter(DetailsOverviewRow.class, rowPresenter);
        selector.addClassPresenter(ListRow.class, new ListRowPresenter());
        return new ArrayObjectAdapter(selector);
    }

    private void setDetailsOverview( ArrayObjectAdapter rowsAdapter,BookDto book){
        detailsOverviewRow = new DetailsOverviewRow(book);
        setActions(detailsOverviewRow);
        setOverviewImage(detailsOverviewRow,rowsAdapter);
        rowsAdapter.add(detailsOverviewRow);
    }

    private void setAdditionalMediaRow(ArrayObjectAdapter rowsAdapter){
        similarBooksAdapter = new ArrayObjectAdapter(new BookPreviewPresenter());
        setSimilarBooks();
        HeaderItem header = new HeaderItem(Constants.SIMILAR_ROW_ID, getString(R.string.similar_books));
        rowsAdapter.add(new ListRow(header, similarBooksAdapter));
    }

    private void setSimilarBooks(){
        if(book != null){
            tagRepository.getByBookIdAsync(book.id).thenAccept(tags->{
                var tagsIds = tags.stream().map(tag->tag.id).collect(Collectors.toList());
                updateSimilarBooks(tagsIds);
            });
        }
    }

    private void updateSimilarBooks(List<Long> tagsIds){
        if(tagsIds.isEmpty()){
            similarBooksAdapter.clear();
        }
        else{
            bookRepository.getByTagsListAsync(tagsIds).thenAccept(similarBooks->{
                similarBooks.removeIf(similarBook->Objects.equals(similarBook.id, book.id));
                similarBooksAdapter.setItems(similarBooks,new BookDtoDiffCallback());
            });
        }
    }
}
