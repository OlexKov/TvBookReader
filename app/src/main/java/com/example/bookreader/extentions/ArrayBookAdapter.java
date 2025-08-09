package com.example.bookreader.extentions;

import static com.example.bookreader.constants.Constants.INIT_ADAPTER_SIZE;
import static com.example.bookreader.constants.Constants.UPLOAD_SIZE;
import static com.example.bookreader.constants.Constants.UPLOAD_THRESHOLD;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.VerticalGridView;

import com.example.bookreader.customclassses.RowUploadInfo;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.diffcallbacks.BookDtoDiffCallback;

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ArrayBookAdapter extends ArrayObjectAdapter {
    private  RowUploadInfo info;
    private final BookDtoDiffCallback bookDiffCallback = new BookDtoDiffCallback();
    private final VerticalGridView gridView;
    private final BookRepository bookRepository = new BookRepository();


    public ArrayBookAdapter(@NonNull PresenterSelector presenterSelector,Long mainCategoryId,Long rowCategoryId,VerticalGridView gridView) {
        super(presenterSelector);
        this.gridView = gridView;
        init( mainCategoryId, rowCategoryId);

    }

    public ArrayBookAdapter(@NonNull Presenter presenter,Long mainCategoryId,Long rowCategoryId,VerticalGridView gridView) {
        super(presenter);
        this.gridView = gridView;
        init( mainCategoryId, rowCategoryId);
    }

    public ArrayBookAdapter(Long mainCategoryId,Long rowCategoryId,VerticalGridView gridView) {
        super();
        this.gridView = gridView;
        init( mainCategoryId, rowCategoryId);
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
        if(isRemoved){
            normalizeAdapterSize();
        }
        return  isRemoved;
    }

    public int removeItems(int position, int count) {
        int removedCount = super.removeItems(position,count);
        //normalize
        return removedCount;
    }

    private void normalizeAdapterSize() {
        Long bookCount =  bookRepository.getRowBooksCountAsync(info.getMainCategoryId(),info.getRowCategoryId()).join();
        int def = (int)(info.getMaxElementsDb() - bookCount);
        if(def != 0){
            info.setMaxElementsDb(bookCount);
            info.setLastUploadedElementDbIndex(info.getLastUploadedElementDbIndex() - def);
        }
    }

    public void tryPaginateRow(BookDto selectedBook){
        int currentFocusPosition = this.indexOf(selectedBook);
        Log.d("PAGINATION","currentFocusPosition - "+String.valueOf(currentFocusPosition));
        boolean nextThreshold = currentFocusPosition + UPLOAD_THRESHOLD > size();
        Log.d("PAGINATION","nextThreshold - "+String.valueOf(nextThreshold));
        boolean prevThreshold = currentFocusPosition - UPLOAD_THRESHOLD < 0;
        Log.d("PAGINATION","prevThreshold - "+String.valueOf(prevThreshold));
        if(info == null || info.getMaxElementsDb() <= INIT_ADAPTER_SIZE || info.isLoading()
                || (!nextThreshold  && !prevThreshold  )) return;
        int firstUploadedElementDbIndex =  info.getLastUploadedElementDbIndex() - INIT_ADAPTER_SIZE;
        Log.d("PAGINATION","firstUploadedElementDbIndex - "+String.valueOf(firstUploadedElementDbIndex));
        boolean canUpload = nextThreshold ? info.getLastUploadedElementDbIndex() < info.getMaxElementsDb() : firstUploadedElementDbIndex >= 1;
        Log.d("PAGINATION","canUpload - "+String.valueOf(canUpload));
        if(canUpload){
            info.setLastUploadedElementDbIndex(
                    nextThreshold ? (int)Math.min(info.getMaxElementsDb(),info.getLastUploadedElementDbIndex() + UPLOAD_SIZE)
                                  : Math.max(INIT_ADAPTER_SIZE,info.getLastUploadedElementDbIndex() - UPLOAD_SIZE ));
            info.setLoading(true);
            Log.d("PAGINATION","LastUploadedElementDbIndex - "+String.valueOf(info.getLastUploadedElementDbIndex()));
            updateAdapter();
        }
        Log.d("PAGINATION","------------------------- - ");
    }

    private void updateAdapter(){
        loadRowBooks(info.getMainCategoryId(),info.getRowCategoryId()).thenAccept(books->{
            if(!books.isEmpty()){
                new Handler(Looper.getMainLooper()).post(() -> {
                    setItems(books,bookDiffCallback);
                    gridView.post(() -> {
                        info.setLoading(false);
                    });

                });
            }
        }).exceptionally(ex->{
            Log.e("ERROR", "Exception in future: " + ex.getMessage(), ex);
            return null;
        });
    }

    private CompletableFuture<List<BookDto>> loadRowBooks(Long mainCategoryId, Long rowCategoryId) {
        int offset = Math.max(0, info.getLastUploadedElementDbIndex() - INIT_ADAPTER_SIZE);
        return bookRepository.loadRowBooksAsync(mainCategoryId, rowCategoryId,offset,INIT_ADAPTER_SIZE);
    }

    private void init( Long mainCategoryId,Long rowCategoryId){
        this.info = new RowUploadInfo();
        this.info.setMainCategoryId(mainCategoryId);
        this.info.setRowCategoryId(rowCategoryId);
        this.info.setMaxElementsDb(bookRepository.getRowBooksCountAsync(mainCategoryId,rowCategoryId).join());
        this.info.setLastUploadedElementDbIndex((int)Math.min(info.getMaxElementsDb(),INIT_ADAPTER_SIZE));
        updateAdapter();
    }

    public Long getMainCategoryId() {return info.getMainCategoryId();}

    public Long getRowCategoryId() {return info.getRowCategoryId();}

    public Long getDbElementsCount() {return info.getMaxElementsDb();}

    public  void reinit(){
       init( info.getMainCategoryId(),info.getRowCategoryId());
    }

}
