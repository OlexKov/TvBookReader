package com.example.bookreader.fragments.reader;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
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
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.utility.ProcessRuner;
import com.example.bookreader.utility.bookutils.BookProcessor;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BookReaderFragment  extends Fragment {
    private final ProcessRuner processRuner = new ProcessRuner();
    private final BookDto book;
    private RecyclerView recyclerView;
    private BookPageAdapter adapter;
    private BookProcessor bookProcessor;
    private float currentScale = 1f;
    private LinearLayoutManager layoutManager ;
    private int screenWidth ;
    private int screenHeight ;
    private final float PAGE_ASPECT_RATIO = 2f / 3f;
    private final float PAGE_QUALITY = 0.25f;
    private float maxScale ;
    private  List<Bitmap> pages;
    private boolean isPagesUpdating;
    private int firstVisiblePage = 0;


    public BookReaderFragment(@NotNull BookDto book){
        this.book = book;
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
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        maxScale =  screenWidth / PAGE_ASPECT_RATIO / screenHeight;

        recyclerView = view.findViewById(R.id.pageRecyclerView);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(null);

        loadBookPages().thenAccept(pages->{
            this.pages = pages;
            adapter = new BookPageAdapter(pages,currentScale);
            getActivity().runOnUiThread(()->{
                recyclerView.setAdapter(adapter);
            });
        });


        // Вимикаємо фокусування на дочірніх елементах
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        // Обробка кнопок UP/DOWN з пульта
        recyclerView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        recyclerView.smoothScrollBy(0, 200); // вниз
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        recyclerView.smoothScrollBy(0, -200); // вверх
                        return true;

                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        float scale = Math.min(currentScale + 0.2f,maxScale);
                        if(currentScale != scale){
                            currentScale = scale;
                            updateScale();
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        float sce = Math.max(currentScale - 0.2f,1);
                        if(currentScale != sce){
                            currentScale = sce;
                            updateScale();
                        }
                        return true;
                }
            }
            return false;
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // перша видима сторінка
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if(firstVisiblePage != firstVisible){
                    boolean scrollDown = firstVisible > firstVisiblePage;
                    firstVisiblePage = firstVisible;
                    if(scrollDown){
                        pages.remove(pages.size()-1);
                    }
                    else{
                        pages.remove(0);
                    }


                }
            }
        });


    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateScale(){
        adapter.setScale(currentScale);
        processRuner.runDelayed(1000, this::updatePages);
    }

    private CompletableFuture<List<Bitmap>> loadBookPages() {
        int currentHeight = (int)(screenHeight * currentScale * PAGE_QUALITY);
        int currentWidth = (int)(currentHeight * PAGE_ASPECT_RATIO);
        List<Integer> pages = List.of(0,1,2,3,4,5);
        return bookProcessor.getPreviewsAsync(pages, currentHeight, currentWidth);
    }

    private void updatePages(){
        if(!isPagesUpdating){
            isPagesUpdating = true;
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            loadBookPages().thenAccept(pages->{
                this.pages = pages;
                adapter.setPages(pages);
                adapter.notifyItemRangeChanged(firstVisible,lastVisible - firstVisible + 1);
                isPagesUpdating = false;
            });
        }
    }

}
