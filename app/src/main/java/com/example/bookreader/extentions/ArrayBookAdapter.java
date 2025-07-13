package com.example.bookreader.extentions;

import static com.example.bookreader.constants.Constants.INIT_ADAPTER_SIZE;
import static com.example.bookreader.constants.Constants.UPLOAD_SIZE;
import static com.example.bookreader.constants.Constants.UPLOAD_THRESHOLD;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;

import com.example.bookreader.constants.Constants;
import com.example.bookreader.customclassses.RowUploadInfo;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;



public class ArrayBookAdapter extends ArrayObjectAdapter {
    private final RowUploadInfo info;



    public ArrayBookAdapter(@NonNull PresenterSelector presenterSelector,@NonNull RowUploadInfo info) {
        super(presenterSelector);
        this.info = info;
    }


    public ArrayBookAdapter(@NonNull Presenter presenter,@NonNull RowUploadInfo info) {
        super(presenter);
        this.info = info;
    }
    public ArrayBookAdapter(@NonNull RowUploadInfo info) {
        super();
        this.info = info;
    }

    @Override
    public void add(@NonNull Object item) {
        super.add(item);
       //normalize
    }

    @Override
    public void add(int index, @NonNull Object item) {
        super.add(index, item);
       //normalize
    }

    @Override
    public void addAll(int index, @NonNull Collection<?> items) {
        super.addAll(index, items);
        //overflow logic

    }

    public boolean remove(@NonNull Object item) {
        boolean isRemoved = super.remove(item);
        normalizeAdapterSize();
        return  isRemoved;
    }

    public int removeItems(int position, int count) {
        int removedCount = super.removeItems(position,count);
        //normalize
        return removedCount;
    }

    public void paginateRow(BookDto selectedBook){

        int currentFocusPosition = this.indexOf(selectedBook);
        boolean nextThreshold = currentFocusPosition + UPLOAD_THRESHOLD >= INIT_ADAPTER_SIZE - 1;
        if(info == null || info.getMaxElements() <= INIT_ADAPTER_SIZE || info.isLoading()
                || (!nextThreshold  && currentFocusPosition - UPLOAD_THRESHOLD > 0 )) return;
        int firstUploadedElementDbIndex =  info.getLastUploadedElementDbIndex() - INIT_ADAPTER_SIZE;
        int upload_size = nextThreshold ? UPLOAD_SIZE : Math.min(UPLOAD_SIZE, firstUploadedElementDbIndex);
        boolean needUpload = nextThreshold ? info.getLastUploadedElementDbIndex() < info.getMaxElements() : firstUploadedElementDbIndex >= 1;
        if(needUpload){
            int offset = nextThreshold ?  info.getLastUploadedElementDbIndex() : Math.max(0, firstUploadedElementDbIndex - UPLOAD_SIZE);
            info.setLoading(true);
            updateAdapter(nextThreshold,  offset, upload_size);
        }
    }

    private void normalizeAdapterSize() {
        info.setMaxElements(info.getMaxElements() - 1);
        info.setLastUploadedElementDbIndex(info.getLastUploadedElementDbIndex() - 1);
        if(INIT_ADAPTER_SIZE <= info.getMaxElements() && this.size() < INIT_ADAPTER_SIZE ){
            boolean next = info.getLastUploadedElementDbIndex() < info.getMaxElements();
            int firstUploadedElementDbIndex =  info.getLastUploadedElementDbIndex() - INIT_ADAPTER_SIZE;
            int offset = next ? info.getLastUploadedElementDbIndex() : Math.max(0,firstUploadedElementDbIndex  );
            updateAdapter( next, offset, 1);
        }
    }

    private void updateAdapter(boolean next , int offset, int upload_size){
        loadRowBooks(info.getMainCategoryId(),info.getRowCategoryId(),offset,upload_size).thenAccept(books->{
            if(!books.isEmpty()){
                new Handler(Looper.getMainLooper()).post(() -> {
                    int booksCount = books.size();
                    if(next){
                        this.addAll(this.size(), books);
                        if(this.size() > INIT_ADAPTER_SIZE){
                            this.removeItems(0,booksCount);
                        }
                        info.setLastUploadedElementDbIndex(info.getLastUploadedElementDbIndex() + booksCount);
                    }
                    else{
                        this.addAll(0, books);
                        if(this.size() > INIT_ADAPTER_SIZE){
                            this.removeItems(this.size() - booksCount, booksCount);
                            info.setLastUploadedElementDbIndex(info.getLastUploadedElementDbIndex() - booksCount);
                        }
                    }
                    //view.post(()->{
                        info.setLoading(false);
                   // });
                });
            }
        }).exceptionally(ex->{
            Log.e("ERROR", "Exception in future: " + ex.getMessage(), ex);
            return null;
        });
    }

    private CompletableFuture<List<BookDto>> loadRowBooks(Long mainCategoryId, Long rowCategoryId, int offset, int size) {
        BookRepository bookRepository = new BookRepository();
        if(mainCategoryId == Constants.FAVORITE_CATEGORY_ID || rowCategoryId == Constants.FAVORITE_CATEGORY_ID ){
            return bookRepository.getRangeFavoriteBooksAsync(offset,size);
        }
        else if (mainCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
            if (rowCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return bookRepository.getRangeAllBooksAsync(offset,size);
            } else if (rowCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return bookRepository.getRangeUnsortedBooksAsync(offset,size);
            } else {
                return bookRepository.getRangeAllBooksInCategoryIdAsync( rowCategoryId,offset,size);
            }
        }
        else {
            if (rowCategoryId == Constants.ALL_BOOKS_CATEGORY_ID) {
                return bookRepository.getRangeAllBooksInCategoryIdAsync( mainCategoryId,offset,size);
            } else if (rowCategoryId == Constants.UNSORTED_BOOKS_CATEGORY_ID) {
                return bookRepository.getRageUnsortedBooksByCategoryIdAsync(mainCategoryId,offset,size);
            } else {
                return bookRepository.getRangeAllBooksInCategoryIdAsync( mainCategoryId,offset,size);
            }
        }
    }

}
