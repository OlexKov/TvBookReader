package com.example.bookreader.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.R;
import com.example.bookreader.customclassses.EmptyItem;
import com.example.bookreader.data.database.entity.Book;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.presenters.BookInfoPresenter;
import com.example.bookreader.presenters.EmptyItemPresenter;
import com.example.bookreader.utility.AnimHelper;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.ImageHelper;
import com.example.bookreader.utility.bookutils.BookInfo;
import com.example.bookreader.utility.bookutils.BookProcessor;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LoadFilesFragment extends Fragment {
    private VerticalGridView newFilesList;
    private TextView title;
    private LinearLayout buttonContainer;
    private ArrayObjectAdapter newFileAdapter;
    private ProgressBarManager progressBarManager;
    private Button loadButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.load_files, container, false);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.setBackgroundColor(Color.parseColor("#AF494949"));
        title = view.findViewById(R.id.load_files_title);
        title.setText(getString(R.string.searching));
        setButtons(view);
        setFileList(view);
        setFileListAdapter();
        setProgressBarManager(100);
        List<String> filePaths = requireActivity().getIntent().getStringArrayListExtra("data");
        if(filePaths != null){
            addInfoToPreview(filePaths);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void setButtons(View view){
        buttonContainer = view.findViewById(R.id.load_files_button_container);
        buttonContainer.setVisibility(INVISIBLE);
        loadButton = view.findViewById(R.id.load_files_save_button);
        Button cancelButton = view.findViewById(R.id.load_files_cancel_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBooksToDataBase();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
                requireActivity().overridePendingTransition(0,R.anim.slide_out_top);
            }
        });
    }

    private List<String> getArchivesPaths(List<String> paths){
        return  paths.stream()
                .filter(path->{
                    String ext = FileHelper.getPathFileExtension(path);
                    return ext != null && (ext.equals("zip"));
                })
                .collect(Collectors.toList());
    }

    private void setFileListAdapter(){
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(BookInfo.class,new BookInfoPresenter());
        presenterSelector.addClassPresenter(EmptyItem.class,new EmptyItemPresenter());
        newFileAdapter = new ArrayObjectAdapter(presenterSelector);
        newFileAdapter.add(new EmptyItem(180));
        newFilesList.setAdapter(new ItemBridgeAdapter(newFileAdapter));
        RecyclerView.ItemAnimator animator = newFilesList.getItemAnimator();
        if (animator != null) {
            animator.setRemoveDuration(0);
            animator.setAddDuration(500);
        }
    }

    private void setProgressBarManager(int initialDelay){
        progressBarManager = new ProgressBarManager();
        if(getView() instanceof  ViewGroup root){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(initialDelay);
            progressBarManager.hide();
        }
    }

    private void setFileList(View view){
        newFilesList = view.findViewById(R.id.load_files_list);
        newFilesList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        newFilesList.setNumColumns(1);
        newFilesList.setVerticalSpacing(30);
    }

    private void runOnUiThread(Runnable r) {
        requireActivity().runOnUiThread(r);
    }

    private void addInfoToPreview(List<String> filePaths) {
        List<String> archives = getArchivesPaths(filePaths);
        filePaths.removeAll(archives);
        BooksArchiveReader reader = new BooksArchiveReader();
        for (String archivePath : archives) {
            filePaths.addAll(reader.fileBooksPaths(archivePath));
        }

        if (filePaths.isEmpty()) {
            title.setText(getString(R.string.no_new_books_found));
            return;
        }

        BookRepository bookRepository = new BookRepository();
        processFiles(filePaths, bookRepository);
    }

    private List<CompletableFuture<BookInfo>> getBookInfoFutures(List<String> filePaths){
        return filePaths.stream()
                .map(filePath -> {
                    try {
                        BookProcessor processor = new BookProcessor(requireContext(), filePath);
                        return processor.getInfoAsync().thenApply(bookInfo -> {
                            if (bookInfo == null) {
                                showToast("Error open file " + filePath);
                            }
                            return bookInfo;
                        });
                    } catch (IOException e) {
                        return this.<BookInfo>failedFuture(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private  List<CompletableFuture<Void>> getPreviewFutures(List<BookInfo> bookInfos,AtomicInteger filesCount){
        return bookInfos.stream()
                .map(info -> {
                    try {
                        return new BookProcessor(getContext(), info.filePath)
                                .getPreviewAsync(
                                        0,
                                        AnimHelper.convertToPx(requireContext(), 400),
                                        AnimHelper.convertToPx(requireContext(), 280))
                                .thenAccept(bitmap -> {
                                    info.preview = bitmap;
                                    runOnUiThread(() -> newFileAdapter.add(info));
                                })
                                .exceptionally(ex -> {
                                    Log.e("BookProcessor", "Error loading preview for " + info.filePath, ex);
                                    return null;
                                })
                                .whenComplete((r, ex) -> {
                                    if (filesCount.decrementAndGet() == 0) {
                                        checkAndHideProgress();
                                    }
                                });
                    } catch (IOException e) {
                        // Якщо не можемо створити BookProcessor, зменшуємо лічильник і логіруємо
                        if (filesCount.decrementAndGet() == 0) {
                            checkAndHideProgress();
                        }
                        Log.e("BookProcessor", "IOException creating BookProcessor for " + info.filePath, e);
                        return CompletableFuture.<Void>completedFuture(null);
                    }
                })
                .collect(Collectors.toList());
    }

    private void addBooksInfoToBookInfoList( BookRepository bookRepository, List<Integer> hashes,List<BookInfo> infos){
        bookRepository.getBooksByHashesAsync(hashes)
                .thenAccept(existingHashes -> {
                    List<BookInfo> newInfos = infos.stream()
                            .filter(info -> !existingHashes.contains(info.fileHash))
                            .collect(Collectors.toList());

                    if (newInfos.isEmpty()) {
                        checkAndHideProgress();
                        return;
                    }
                    AtomicInteger filesCount = new AtomicInteger(newInfos.size());
                    List<CompletableFuture<Void>> previewFutures = getPreviewFutures(newInfos,filesCount);
                    CompletableFuture.allOf(previewFutures.toArray(new CompletableFuture[0]))
                            .exceptionally(ex -> {
                                Log.e("BookProcessor", "Error in preview futures", ex);
                                return null;
                            });
                });
    }

    private void processFiles(List<String> filePaths, BookRepository bookRepository) {
        progressBarManager.show();
        List<CompletableFuture<BookInfo>> bookInfosFutures = getBookInfoFutures(filePaths);
        CompletableFuture.allOf(bookInfosFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {

                    List<BookInfo> infos = bookInfosFutures.stream()
                            .map(future -> {
                                try {
                                    return future.join();
                                } catch (CompletionException ex) {
                                    Log.e("BookProcessor", "Error getting BookInfo", ex.getCause());
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    List<Integer> hashes = infos.stream()
                            .map(info -> info.fileHash)
                            .collect(Collectors.toList());

                    addBooksInfoToBookInfoList(bookRepository,hashes,infos);

                })
                .exceptionally(ex -> {
                    Log.e("BookProcessor", "Error processing files", ex);
                    checkAndHideProgress();
                    return null;
                });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SetTextI18n")
    private void checkAndHideProgress() {
        runOnUiThread(() -> {
            int size = newFileAdapter.size();
            if (size > 1) {
                buttonContainer.setVisibility(VISIBLE);
                loadButton.requestFocus();
                title.setText(getString(R.string.new_books_found) + (size - 1));
                newFileAdapter.add(new EmptyItem(300));
                newFilesList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            } else {
                title.setText(getString(R.string.no_new_books_found));
            }
            progressBarManager.hide();
        });
    }

    private  <T> CompletableFuture<T> failedFuture(Throwable ex) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

    private void saveBooksToDataBase() {
        progressBarManager.show();
        List<BookInfo> booksInfos = newFileAdapter.unmodifiableList().stream()
                .filter(BookInfo.class::isInstance)
                .map(BookInfo.class::cast)
                .collect(Collectors.toList());

        BookRepository bookRepository = new BookRepository();

        List<CompletableFuture<BookInfo>> booksFutures = booksInfos.stream()
                .map(bookInfo -> ImageHelper
                        .saveImageAsync(requireContext(), bookInfo.preview, 100, Bitmap.CompressFormat.PNG)
                        .thenApply(path -> {
                            bookInfo.previewPath = path;
                            return bookInfo;
                        })
                ).collect(Collectors.toList());

        CompletableFuture
                .allOf(booksFutures.toArray(new CompletableFuture[0]))
                .thenCompose(v -> {
                    List<Book> books = booksFutures.stream()
                            .map(CompletableFuture::join)
                            .map(BookInfo::getBook)
                            .collect(Collectors.toList());

                    return bookRepository.insertAllAsync(books);
                }).thenAccept((booksIds) -> {
                    requireActivity().runOnUiThread(() -> {
                        progressBarManager.hide();
                        Intent result = new Intent();
                        long[] idsArray = booksIds.stream()
                                .mapToLong(Long::longValue)
                                .toArray();

                        result.putExtra("NEW_FILES_IDS",idsArray);
                        requireActivity().setResult(Activity.RESULT_OK, result);
                        requireActivity().finish();
                        requireActivity().overridePendingTransition(0, R.anim.slide_out_top);
                    });
                });
    }
}

