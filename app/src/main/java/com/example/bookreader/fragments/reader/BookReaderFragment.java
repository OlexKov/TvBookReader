package com.example.bookreader.fragments.reader;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.bookreader.constants.Constants.READER_MAX_ADAPTER_PAGES;
import static com.example.bookreader.constants.Constants.READER_PAGE_ASPECT_RATIO;
import static com.example.bookreader.constants.Constants.READER_SCALE_STEPS;
import static com.example.bookreader.constants.Constants.READER_SCROLL_STEPS;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.R;
import com.example.bookreader.customclassses.PagePreview;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.dto.BookSettingsDto;
import com.example.bookreader.data.database.repository.BookSettingsRepository;
import com.example.bookreader.extentions.BookScrollBar;
import com.example.bookreader.utility.ImageHelper;
import com.example.bookreader.utility.ProcessRuner;
import com.example.bookreader.utility.bookutils.BookProcessor;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class BookReaderFragment  extends Fragment {
    private final BookSettingsRepository bookSettingsRepository = new BookSettingsRepository();
    private final ProcessRuner processRuner = new ProcessRuner();
    private final ProcessRuner bigSpinnerRuner = new ProcessRuner();
    private final ProcessRuner smallSpinnerRuner = new ProcessRuner();
    private final BookDto book;
    private ProgressBar bigSpinner;
    private ProgressBar smallSpinner;
    private BookScrollBar progressBar;
    private RecyclerView recyclerView;
    private BookPageAdapter adapter;
    private BookProcessor bookProcessor;
    private LinearLayoutManager layoutManager ;
    private int screenWidth ;
    private int screenHeight ;
    private int scrollHeight ;
    private float maxScale ;
    private float scaleStep;
    private  List<PagePreview> pages;
    private boolean isPagesUpdating = false;
    private volatile boolean isPageLoading = false;
    private int firstVisiblePage = 0;
    private int firstPageIndex;
    private int lastPageIndex ;
    private int currentPreviewHeight;
    private int currentPreviewWidth;
    private final BookSettingsDto bookSettings;
    private final Semaphore pageLoadingSemaphore = new Semaphore(1);


    public BookReaderFragment(@NotNull BookDto book){
        this.book = book;
        bookSettings = bookSettingsRepository.getByBookIdAsync(book.id).join();
        initPagesRange();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        bookSettingsRepository.updateAsync(bookSettings.getBookSetting());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.book_reader_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(Color.BLACK);
        bookProcessor = new BookProcessor(getContext(),book.filePath);
        progressBar = view.findViewById(R.id.scrollBar);
        bigSpinner = view.findViewById(R.id.bigProgressBar);
        smallSpinner = view.findViewById(R.id.smallProgressBar);
        progressBar.setMax(book.pageCount);
        initParams();
        setRecyclerView(view);
        updatePreviewSize();
        setAndInitAdapter();
        setKeyListener();
        setScrollListener();
    }

    private void initParams(){
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        maxScale =  screenWidth / READER_PAGE_ASPECT_RATIO / screenHeight;
        scaleStep = (maxScale - 1.0f) / READER_SCALE_STEPS;
        scrollHeight = screenHeight / READER_SCROLL_STEPS;
    }

    private void setScrollListener(){
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                updateBookPosition(firstVisible);
                boolean scrollDown = firstVisible > firstVisiblePage;
                firstVisiblePage = firstVisible;
                progressBar.setProgress(bookSettings.lastReadPageIndex);
                if (scrollDown && shouldLoadNext()) {
                    loadNextPage();
                } else if (!scrollDown && shouldLoadPrev()) {
                    loadPrevPage();
                }
            }
        });
    }

    private boolean shouldLoadNext() {
        return !isPageLoading
                && (firstVisiblePage + 1 == READER_MAX_ADAPTER_PAGES - 1)
                && (book.pageCount - 1 > lastPageIndex);
    }

    private boolean shouldLoadPrev() {
        return !isPageLoading
                && firstVisiblePage - 1 < 0
                && firstPageIndex != 0;
    }

    private void loadNextPage() {
        showSmallSpinner();
        try {
            pageLoadingSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        isPageLoading = true;
        firstPageIndex++;
        lastPageIndex++;
        bookProcessor.getPreviewAsync(lastPageIndex, currentPreviewHeight, currentPreviewWidth)
                .thenAccept(page -> {
                    page.preview = ImageHelper.processBitmap(page.preview,bookSettings.invert,bookSettings.contrast,bookSettings.brightness);
                    requireActivity().runOnUiThread(()->{
                        pages.remove(0);
                        adapter.notifyItemRemoved(0);
                        pages.add(page);
                        adapter.notifyItemInserted(pages.size() - 1);
                    });
                    isPageLoading = false;
                    pageLoadingSemaphore.release();
                    hideSmallSpinner();
                });
    }

    private void loadPrevPage()  {
        showSmallSpinner();
        try {
            pageLoadingSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        isPageLoading = true;
        firstPageIndex--;
        lastPageIndex--;
        bookProcessor.getPreviewAsync(firstPageIndex, currentPreviewHeight, currentPreviewWidth)
                .thenAccept(page ->{
                    page.preview = ImageHelper.processBitmap(page.preview,bookSettings.invert,bookSettings.contrast,bookSettings.brightness);
                    int removeIndex = pages.size() - 1;
                    requireActivity().runOnUiThread(()->{
                        pages.remove(removeIndex);
                        adapter.notifyItemRemoved(removeIndex);
                        pages.add(0, page);
                        adapter.notifyItemInserted(0);
                    });
                    isPageLoading = false;
                    pageLoadingSemaphore.release();
                    hideSmallSpinner();
                });
    }

    private void updateBookPosition(int firstVisible) {
        View firstChild = recyclerView.getChildAt(0);
        bookSettings.pageOffset = (firstChild != null) ? firstChild.getTop() : 0;
        bookSettings.lastReadPageIndex = pages.get(firstVisible).pageIndex;
    }

    private void setKeyListener(){
        // Обробка кнопок UP/DOWN з пульта
        recyclerView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        recyclerView.smoothScrollBy(0, scrollHeight);
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        recyclerView.smoothScrollBy(0, -scrollHeight);
                        return true;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        float scale = Math.min(bookSettings.scale + scaleStep,maxScale);
                        if(bookSettings.scale != scale){
                            bookSettings.scale = scale;
                            updateScale();
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        float sce = Math.max(bookSettings.scale - scaleStep,1);
                        if(bookSettings.scale != sce){
                            bookSettings.scale = sce;
                            updateScale();
                        }
                        return true;
                }
            }
            return false;
        });
    }

    private void setAndInitAdapter(){
        showBigSpinner();
        loadBookPages().thenAccept(pages->{
            this.pages = pages;
            processPages();
            adapter = new BookPageAdapter(this.pages,bookSettings.scale);
            requireActivity().runOnUiThread(()->{
                recyclerView.setAdapter(adapter);
                if(bookSettings.lastReadPageIndex != 0 || bookSettings.pageOffset != 0){
                    int index = java.util.stream.IntStream.range(0, pages.size())
                            .filter(i -> pages.get(i).pageIndex == bookSettings.lastReadPageIndex)
                            .findFirst()
                            .orElse(-1);
                    layoutManager.scrollToPositionWithOffset(index,bookSettings.pageOffset);
                }
                hideBigSpinner();
            });
        });
    }

    private void setRecyclerView(View view){
        recyclerView = view.findViewById(R.id.pageRecyclerView);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(null);
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    private void initPagesRange(){
        int halfBufferSize = READER_MAX_ADAPTER_PAGES / 2;
        int lastBookPageIndex = book.pageCount-1;
        if(bookSettings.lastReadPageIndex < halfBufferSize){
            firstPageIndex = 0;
        }
        else if(bookSettings.lastReadPageIndex + halfBufferSize >= lastBookPageIndex){
            firstPageIndex = lastBookPageIndex - (READER_MAX_ADAPTER_PAGES - 1);
        }
        else{
            firstPageIndex = bookSettings.lastReadPageIndex - halfBufferSize;
        }
        lastPageIndex = Math.min(firstPageIndex + READER_MAX_ADAPTER_PAGES - 1,lastBookPageIndex);

    }

    private void updateScale(){
        updatePreviewSize();
        adapter.setScale(bookSettings.scale);
        processRuner.runDelayed(1000, this::updatePages);
    }

    private CompletableFuture<List<PagePreview>> loadBookPages() {
        List<Integer> pages = java.util.stream.IntStream
                .range(firstPageIndex, lastPageIndex + 1)
                .boxed()
                .collect(Collectors.toList());
        return bookProcessor.getPreviewsAsync(pages, currentPreviewHeight, currentPreviewWidth);
    }

    private void updatePages(){
        showSmallSpinner();
        if(!isPagesUpdating){
            isPagesUpdating = true;
            loadBookPages().thenAccept(pages->{
                try {
                    pageLoadingSemaphore.acquire();
                    pageLoadingSemaphore.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for(int i = 0; i < pages.size(); i++){
                    var newPage = pages.get(i);
                    for(int k = 0; k < this.pages.size(); k++){
                        var oldPage =  this.pages.get(k);
                        if(newPage.pageIndex == oldPage.pageIndex){
                            oldPage.preview = ImageHelper.processBitmap(newPage.preview,bookSettings.invert,bookSettings.contrast,bookSettings.brightness);
                            break;
                        }
                    }
                }
                requireActivity().runOnUiThread(()->{
                    adapter.notifyItemRangeChanged(0,pages.size());
                });

                //updateVisiblePages();
                isPagesUpdating = false;
                hideSmallSpinner();
            });
        }
    }

    private void updatePreviewSize(){
        currentPreviewHeight = (int)(screenHeight * bookSettings.scale * bookSettings.quality);
        currentPreviewWidth = (int)(currentPreviewHeight * READER_PAGE_ASPECT_RATIO);
    }

    public void processPages() {
        this.pages.parallelStream().forEach(page->{
            page.preview = ImageHelper.processBitmap(page.preview,bookSettings.invert,bookSettings.contrast,bookSettings.brightness);
        });
    }

    private void showSpinner(ProgressBar bar,ProcessRuner runer,int startDelay){
        runer.runDelayed(startDelay,()->bar.setVisibility(VISIBLE));
    }

    private void hideSpinner(ProgressBar bar,ProcessRuner runer,int startDelay){
        runer.runDelayed(startDelay,()->bar.setVisibility(GONE));
    }

    private void showBigSpinner(){
        showSpinner(bigSpinner,bigSpinnerRuner,300);
    }

    private void hideBigSpinner(){
        hideSpinner(bigSpinner,bigSpinnerRuner,0);
    }

    private void showSmallSpinner(){
        showSpinner(smallSpinner,smallSpinnerRuner,400);
    }

    private void hideSmallSpinner(){
        hideSpinner(smallSpinner,smallSpinnerRuner,0);
    }

}
