package com.example.bookreader.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.app.ProgressBarManager;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.customclassses.EmptyItem;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.fragments.settings.booksettings.EditBookFragment;
import com.example.bookreader.presenters.BookInfoPresenter;
import com.example.bookreader.presenters.EmptyItemPresenter;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.bookutils.BookProcessor;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LoadFilesFragment extends Fragment {
    private final BookReaderApp app = BookReaderApp.getInstance();
    private VerticalGridView newFilesList;
    private TextView title;
    private TextView subTitle;
    private TextView titleValue;
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
        subTitle = view.findViewById(R.id.load_files_subtitle);
        titleValue = view.findViewById(R.id.load_files_title_value);
        setButtons(view);
        setFileList(view);
        setFileListAdapter();
        setProgressBarManager(100);
        List<String> filePaths = requireActivity().getIntent().getStringArrayListExtra("data");
        if(filePaths != null){
            prepareAndScanFileList(filePaths);
        }
       app.getGlobalEventListener().subscribe(getViewLifecycleOwner(), GlobalEventType.LOAD_BOOK_UPDATED,loadBookUpdatedHandler,BookDto.class);
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
        presenterSelector.addClassPresenter(BookDto.class,new BookInfoPresenter(bookClickHandler));
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

    private final Consumer<BookDto> bookClickHandler = (book)->{
        GuidedStepSupportFragment.add(
                requireActivity().getSupportFragmentManager(),
                new EditBookFragment(book)
        );
    };

    private final Consumer<BookDto> loadBookUpdatedHandler = (updatedBook)->{
        int index = newFileAdapter.indexOf(updatedBook);
        if(index >= 0){
            newFileAdapter.notifyItemRangeChanged(index,1);
        }
    };

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

    private boolean isEmptyList(List<String> list){
       if (list.isEmpty()) {
           runOnUiThread(()-> title.setText(getString(R.string.no_new_books_found)) );
           return true;
       }
       return false;
   }

    private void prepareAndScanFileList(List<String> filePaths) {
        List<String> archives = getArchivesPaths(filePaths);
        if(!archives.isEmpty()){
            filePaths.removeAll(archives);
            for (String archivePath : archives) {
                filePaths.addAll(new BooksArchiveReader().fileBooksPaths(archivePath));
            }
        }
        if (isEmptyList(filePaths)) return;
        BookRepository bookRepository = new BookRepository();
        bookRepository.getAllPathsAsync().thenAccept((paths)->{
            List<String> filteredPaths = filePaths.stream()
                    .filter(path->!paths.contains(path))
                    .collect(Collectors.toList());
            if (isEmptyList(filteredPaths)) return;
            loadBookInfosAsync(filteredPaths, bookRepository);
        });

    }

    @SuppressLint("SetTextI18n")
    private void setProgressString(float pointPerPercent, AtomicInteger count){
        float progress = count.get() * pointPerPercent;
        String progressText = String.format(Locale.US, "%.2f", progress);
        titleValue.setText(progressText + "%");
        count.getAndIncrement();
    }

    private List<CompletableFuture<BookDto>> getBookInfoFutures(List<String> filePaths){
        AtomicInteger count = new AtomicInteger(1);
        float pointPerPercent = 100F/filePaths.size();
        return filePaths.stream()
                .map(filePath -> {
                    try {
                         return new BookProcessor(requireContext(), filePath).getInfoAsync().thenApply(bookInfo -> {
                            if (bookInfo == null) {
                                showToast("Error open file " + filePath);
                            }
                            else{
                                runOnUiThread(() -> {
                                    if(pointPerPercent < 20){
                                        subTitle.setText(bookInfo.title);
                                    }
                                    if (pointPerPercent < 4) {
                                        setProgressString( pointPerPercent, count);
                                    }
                                });
                            }
                            return bookInfo;
                         });
                    } catch (IOException e) {
                        showToast("Error load file " + filePath);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void addBooksInfoToBookInfoList( BookRepository bookRepository,List<BookDto> infos){
        List<Integer> hashes = infos.stream()
                .map(info -> info.fileHash)
                .collect(Collectors.toList());
        bookRepository.getBooksByHashesAsync(hashes)
                .thenAccept(existingHashes -> {
                    List<BookDto> newInfos =  !existingHashes.isEmpty()
                            ? infos.stream().filter(info -> !existingHashes.contains(info.fileHash))
                                   .collect(Collectors.toList())
                            : infos;
                    if (newInfos.isEmpty()) {
                        checkAndHideProgress();
                        return;
                    }

                    runOnUiThread(() ->{
                        newFileAdapter.addAll(1,newInfos);
                        newFilesList.setSelectedPosition(1);
                    });

                    checkAndHideProgress();
                });
    }

    private void loadBookInfosAsync(List<String> filePaths, BookRepository bookRepository) {
        if(filePaths.isEmpty()) return;
        runOnUiThread(()->{
            progressBarManager.show();
            title.setText(getString(R.string.scanning));
        });
        List<CompletableFuture<BookDto>> bookInfosFutures = getBookInfoFutures(filePaths);
            CompletableFuture.allOf(bookInfosFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    List<BookDto> infos = bookInfosFutures.stream()
                            .map(future -> {
                                try {
                                    return future.join();
                                } catch (CompletionException ex) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    addBooksInfoToBookInfoList(bookRepository,infos);
                })
                .exceptionally(ex -> {
                    checkAndHideProgress();
                    return null;
                });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private void checkAndHideProgress() {
        runOnUiThread(() -> {
            subTitle.setText("");
            titleValue.setText("");
            int size = newFileAdapter.size();
            if (size > 1) {
                buttonContainer.setVisibility(VISIBLE);
                loadButton.requestFocus();
                title.setText(getString(R.string.new_books_found));
                titleValue.setText(String.valueOf(size - 1));
                newFileAdapter.add(new EmptyItem(300));
                newFilesList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            } else {
                title.setText(getString(R.string.no_new_books_found));
            }
            progressBarManager.hide();
        });
    }

    private void disableFocus(){
        newFilesList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        buttonContainer.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        newFilesList.clearFocus();
        buttonContainer.clearFocus();
    }

    private void saveBooksToDataBase() {
        disableFocus();
        progressBarManager.show();
        title.setText(getString(R.string.saving));
        List<BookDto> booksInfos = newFileAdapter.unmodifiableList().stream()
                .filter(BookDto.class::isInstance)
                .map(BookDto.class::cast)
                .collect(Collectors.toList());

        BookRepository bookRepository = new BookRepository();
        AtomicInteger count = new AtomicInteger(1);
        float pointPerPercent = 100F/booksInfos.size();
        @SuppressLint("SetTextI18n") List<CompletableFuture<Long>> booksFutures = booksInfos.stream()
                .map(bookInfo -> {
                    BookProcessor bookProcessor = new BookProcessor(requireContext(),bookInfo.filePath);
                            try {
                                return  bookProcessor.savePreviewAsync().thenApply(path -> {
                                    bookInfo.previewPath = path;
                                    runOnUiThread(()->{
                                        if(pointPerPercent < 20){
                                            subTitle.setText(bookInfo.title);
                                        }
                                        if(pointPerPercent < 4){
                                            setProgressString( pointPerPercent, count);
                                        }
                                    });
                                    try{
                                        return bookRepository.insert(bookInfo.getBook());
                                    }
                                    catch (Exception e){
                                        FileHelper.deleteFile(path);
                                        return null;
                                    }
                                });
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                       }
                ).filter(Objects::nonNull)
                .collect(Collectors.toList());

        CompletableFuture
                .allOf(booksFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    List<Long> booksIds = booksFutures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
                  //  app.updateCategoryCash();
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
                }).exceptionally(ex -> {
                    Log.e("ERROR",  ex.getMessage());
                    return null;
                });;
    }
}

