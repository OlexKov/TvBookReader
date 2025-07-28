package com.example.bookreader.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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


        newFilesList = view.findViewById(R.id.load_files_list);
        newFilesList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        newFilesList.setNumColumns(1);
        newFilesList.setVerticalSpacing(30);
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


        progressBarManager = new ProgressBarManager();
        if(getView() instanceof  ViewGroup root){
            progressBarManager.setRootView((ViewGroup) root.getRootView());
            progressBarManager.setInitialDelay(0);
            progressBarManager.hide();
        }


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
    private List<String> getArchivesPaths(List<String> paths){
        return  paths.stream()
                .filter(path->{
                    String ext = FileHelper.getPathFileExtension(path);
                    return ext != null && (ext.equals("zip"));
                })
                .collect(Collectors.toList());
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

        progressBarManager.show();
        AtomicInteger filesCount = new AtomicInteger(filePaths.size());

        Set<Integer> existingHashes = new HashSet<Integer>();
        for (int i = 0; i < newFileAdapter.size(); i++) {
            if (newFileAdapter.get(i) instanceof BookInfo info) {
                existingHashes.add(info.hash);
            }
        }

        BookRepository bookRepository = new BookRepository();

        for (String filePath : filePaths) {
            processFile(filePath, filesCount, existingHashes, bookRepository);
        }
    }
    private void processFile(String filePath, AtomicInteger filesCount, Set<Integer> existingHashes, BookRepository bookRepository) {
        try {
            BookProcessor processor = new BookProcessor(requireContext(), filePath);
            processor.getInfoAsync()
                    .thenCompose(bookInfo -> {
                        if (bookInfo == null) {
                            showToast("Error open file " + filePath);
                            return CompletableFuture.completedFuture(null);
                        }

                        if (existingHashes.contains(bookInfo.hash)) {
                            showToast("Duplicate book " + bookInfo.filePath);
                            return CompletableFuture.completedFuture(null);
                        }

                        return bookRepository.isBookExistByHashesAsync(bookInfo.hash)
                                .thenCompose(exists -> {
                                    if (Boolean.TRUE.equals(exists)) return CompletableFuture.completedFuture(null);
                                    try {
                                        return processor.getPreviewAsync(
                                                        0,
                                                        AnimHelper.convertToPx(requireContext(), 400),
                                                        AnimHelper.convertToPx(requireContext(), 300))
                                                .thenAccept(bitmap -> {
                                                    bookInfo.preview = bitmap;
                                                    runOnUiThread(() -> newFileAdapter.add(bookInfo));
                                                });
                                    } catch (IOException e) {
                                        return failedFuture(e);
                                    }
                                });
                    })
                    .whenComplete((res, ex) -> {
                        if (ex != null) Log.e("BookProcessor", "Error: " + filePath, ex);
                        if (filesCount.decrementAndGet() == 0) checkAndHideProgress();
                    });

        } catch (IOException e) {
            Log.e("BookProcessor", "Failed to create processor for file: " + filePath, e);
            if (filesCount.decrementAndGet() == 0) checkAndHideProgress();
        }
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

    private static <T> CompletableFuture<T> failedFuture(Throwable ex) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

    private void saveBooksToDataBase() {
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
                }).thenRun(() -> {
                    requireActivity().runOnUiThread(() -> {
                        requireActivity().finish();
                        requireActivity().overridePendingTransition(0, R.anim.slide_out_top);
                    });
                });
    }
}

