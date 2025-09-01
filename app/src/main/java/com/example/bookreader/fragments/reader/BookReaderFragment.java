package com.example.bookreader.fragments.reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.bookreader.utility.bookutils.BookProcessor;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BookReaderFragment  extends Fragment {
    private final BookDto book;
    private RecyclerView recyclerView;
    private BookPageAdapter adapter;
    private BookProcessor bookProcessor;
    private float currentScale = 1f;
    private LinearLayoutManager layoutManager ;
    private int screenWidth ;
    private int screenHeight ;
    private final float aspectRatio = 2f / 3f;
    private float maxScale ;


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
        maxScale =  screenWidth / aspectRatio / screenHeight;

        recyclerView = view.findViewById(R.id.pageRecyclerView);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        updateScale();

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
                        float scale = Math.min(currentScale + 0.1f,maxScale);
                        if(currentScale != scale){
                            currentScale = scale;
                            updateScale();
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        float sce = Math.max(currentScale - 0.1f,1);
                        if(currentScale != sce){
                            currentScale = sce;
                            updateScale();
                        }
                        return true;
                }
            }
            return false;
        });


    }

    private void updateScale(){
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        View firstChild = recyclerView.getChildAt(0);
        int offset = (firstChild != null) ? firstChild.getTop() : 0;
        loadBookPages().thenAccept(pages->{
            adapter = new BookPageAdapter(pages,currentScale);
            getActivity().runOnUiThread(()->{
                recyclerView.setAdapter(adapter);
                layoutManager.scrollToPositionWithOffset(firstVisible, offset);
            });
        });
    }

    private CompletableFuture<List<Bitmap>> loadBookPages() {
        int currentHeight = (int)(screenHeight * currentScale);
        int currentWidth = (int)(currentHeight * aspectRatio);
        Log.d("PARAMS_LOG","currentHeight - "+String.valueOf(currentHeight));
        Log.d("PARAMS_LOG","currentWidth - "+String.valueOf(currentWidth));
        List<Integer> pages = List.of(0,1,2,3,4,5);
        return bookProcessor.getPreviewsAsync(pages, currentHeight, currentWidth);
    }

}
