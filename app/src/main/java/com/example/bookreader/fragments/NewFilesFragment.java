package com.example.bookreader.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.VerticalGridPresenter;

import com.example.bookreader.customclassses.FileData;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.presenters.BookInfoPresenter;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.bookutils.BookProcessor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NewFilesFragment extends VerticalGridSupportFragment {
    private ProgressBarManager progressBarManager;
    private ArrayObjectAdapter adapter;


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        view.setBackgroundColor(Color.parseColor("#AF494949"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Нові файли");
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_XSMALL);
        gridPresenter.setNumberOfColumns(1);
        gridPresenter.setShadowEnabled(false);

        setGridPresenter(gridPresenter);
        adapter = new ArrayObjectAdapter(new BookInfoPresenter());
        List<String> filePaths = requireActivity().getIntent().getStringArrayListExtra("data");
        progressBarManager = new ProgressBarManager();
        if(getView() instanceof  ViewGroup root){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(0);
            progressBarManager.hide();
        }
        progressBarManager.show();
        checkFilesAsync(filePaths).thenAccept((filesData)->{
            AtomicInteger filesCount = new AtomicInteger(filesData.size());
            for (FileData fileData:filesData){
                BookProcessor bookProcessor = new BookProcessor(requireContext(), fileData.file);
                try {
                    bookProcessor.getInfoAsync().thenAccept((bookInfo) -> {
                        if (bookInfo == null) {
                            Toast.makeText(requireContext(), "Error open file " + fileData.file.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.add(bookInfo);
                            filesCount.getAndDecrement();
                            if(filesCount.get() == 0){
                                progressBarManager.hide();
                            }
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        setAdapter(adapter);
    }

    private CompletableFuture<List<FileData>> checkFilesAsync(List<String> paths) {
        return CompletableFuture.supplyAsync(() ->
                paths.parallelStream().map(path -> {
                            FileData data = new FileData(0, new File(path));
                            try {
                                if (data.file.exists()) {
                                    data.hash = FileHelper.getFileHash(data.file);
                                }
                            } catch (Exception e) {
                                data.hash = 0;
                            } finally {
                                if (data.hash == 0) {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "File error - " + data.file.getName(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                            return data;
                        }).filter(data -> data.hash != 0)
                        .distinct().collect(Collectors.toList())
        ).thenCompose((filesData)-> {
            BookRepository bookRepository = new BookRepository();
            List<Integer> hashes = filesData.stream().map(x -> x.hash).collect(Collectors.toList());
            return bookRepository.getBooksByHashesAsync(hashes).thenApply((dbHashes) ->
            {
                if (dbHashes.isEmpty()) {
                    return filesData;
                }
                return filesData.stream().filter(data -> !dbHashes.contains(data.hash)).collect(Collectors.toList());
            });
        });
    }
}
