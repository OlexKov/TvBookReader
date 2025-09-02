package com.example.bookreader.fragments.reader;

import static com.example.bookreader.constants.Constants.READER_MAX_ADAPTER_PAGES;
import static com.example.bookreader.constants.Constants.READER_PAGE_ASPECT_RATIO;
import static com.example.bookreader.constants.Constants.READER_SCALE_STEP;
import static com.example.bookreader.constants.Constants.READER_SCROLL_Y;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.stream.Collectors;

public class BookReaderFragment  extends Fragment {
    private final BookSettingsRepository bookSettingsRepository = new BookSettingsRepository();
    private final ProcessRuner processRuner = new ProcessRuner();
    private final BookDto book;
    private BookScrollBar progressBar;
    private RecyclerView recyclerView;
    private BookPageAdapter adapter;
    private BookProcessor bookProcessor;
    private LinearLayoutManager layoutManager ;
    private int screenWidth ;
    private int screenHeight ;
    private float maxScale ;
    private  List<PagePreview> pages;
    private boolean isPagesUpdating = false;
    private boolean isPageLoading = false;
    private int firstVisiblePage = 0;
    private int firstPageIndex;
    private int lastPageIndex ;
    private int currentPreviewHeight;
    private int currentPreviewWidth;
    private final BookSettingsDto bookSettings;


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
        this.bookProcessor = new BookProcessor(getContext(),book.filePath);
        progressBar = view.findViewById(R.id.scrollBar);
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
        isPageLoading = true;
        firstPageIndex++;
        lastPageIndex++;
        bookProcessor.getPreviewAsync(lastPageIndex, currentPreviewHeight, currentPreviewWidth)
                .thenAccept(page -> {
                        page.preview = ImageHelper.processBitmap(page.preview,bookSettings.invert,bookSettings.contrast,bookSettings.brightness);
                        requireActivity().runOnUiThread(() -> {
                            pages.remove(0);
                            pages.add(page);
                            adapter.notifyItemRemoved(0);
                            adapter.notifyItemInserted(pages.size() - 1);
                            isPageLoading = false;
                });
        });
    }

    private void loadPrevPage() {
        isPageLoading = true;
        firstPageIndex--;
        lastPageIndex--;
        bookProcessor.getPreviewAsync(firstPageIndex, currentPreviewHeight, currentPreviewWidth)
                .thenAccept(page ->{
                    page.preview = ImageHelper.processBitmap(page.preview,bookSettings.invert,bookSettings.contrast,bookSettings.brightness);
                    requireActivity().runOnUiThread(() -> {
                        pages.remove(pages.size() - 1);
                        pages.add(0, page);
                        adapter.notifyItemRemoved(pages.size() - 1);
                        adapter.notifyItemInserted(0);
                        isPageLoading = false;
                    });
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
                        recyclerView.smoothScrollBy(0, READER_SCROLL_Y);
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        recyclerView.smoothScrollBy(0, -READER_SCROLL_Y);
                        return true;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        float scale = Math.min(bookSettings.scale + READER_SCALE_STEP,maxScale);
                        if(bookSettings.scale != scale){
                            bookSettings.scale = scale;
                            updateScale();
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        float sce = Math.max(bookSettings.scale - READER_SCALE_STEP,1);
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

    @SuppressLint("NotifyDataSetChanged")
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
        if(!isPagesUpdating){
            isPagesUpdating = true;
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            View firstChild = recyclerView.getChildAt(0);
            int offset = (firstChild != null) ? firstChild.getTop() : 0;
            loadBookPages().thenAccept(pages->{
                this.pages = pages;
                processPages();
                adapter.setPages(this.pages);
                getActivity().runOnUiThread(()->{
                    adapter.notifyItemRangeChanged(firstVisible,lastVisible - firstVisible + 1);
                    layoutManager.scrollToPositionWithOffset(firstVisible, offset);
                });
                isPagesUpdating = false;
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
}
