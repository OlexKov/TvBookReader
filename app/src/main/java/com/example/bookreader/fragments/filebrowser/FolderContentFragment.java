package com.example.bookreader.fragments.filebrowser;

import static androidx.leanback.widget.FocusHighlight.ZOOM_FACTOR_MEDIUM;

import static com.example.bookreader.utility.ViewHelper.isDescendant;

import android.annotation.SuppressLint;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;

import androidx.leanback.widget.DiffCallback;
import androidx.leanback.widget.GridLayoutManager;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.customclassses.BrowserFile;
import com.example.bookreader.customclassses.MainStorage;
import com.example.bookreader.diffcallbacks.BrowserFileDiffCallback;
import com.example.bookreader.presenters.browserpresenters.BrowserFilePresenter;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class FolderContentFragment extends VerticalGridSupportFragment {
    private File mainParentFile =  Environment.getExternalStorageDirectory();
    private final DiffCallback<BrowserFile> fileDiff = new BrowserFileDiffCallback();
    private BrowserFile currentFile = new BrowserFile(mainParentFile,false);
    private ArrayObjectAdapter rowAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private TextView pathTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(ZOOM_FACTOR_MEDIUM,false);
        int columnCount = calculateSpanCount(requireContext(), 80);

        gridPresenter.setNumberOfColumns(columnCount);
        setGridPresenter(gridPresenter);
        gridPresenter.setShadowEnabled(false);

        rowAdapter = new ArrayObjectAdapter(new BrowserFilePresenter());
        loadFiles();
        setAdapter(rowAdapter);

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if(item instanceof BrowserFile file){
                    if(file.getFile().isDirectory()){
                        currentFile = file;
                        pathTextView.setText(currentFile.getFile().getAbsolutePath());
                        loadFiles();
                    }
                    else{
                        file.setChecked(!file.isChecked());
                        int index = rowAdapter.indexOf(file);
                        rowAdapter.notifyItemRangeChanged(index,1);
//                        Intent resultIntent = new Intent();
//                        resultIntent.putExtra("SELECTED_FILE_PATH", file.getAbsolutePath());
//                        requireActivity().setResult(Activity.RESULT_OK, resultIntent);
//                        requireActivity().finish();
                    }
                }
            }
        });

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(this, backCallback);
    }



    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pathTextView = requireActivity().findViewById(R.id.file_browser_path);
        app.getGlobalEventListener().subscribe(GlobalEventType.FILEBROWSER_MAIN_FOLDER_SELECTION_CHANGE,mainFolderChangeHandler);
        pathTextView.setText(currentFile.getFile().getAbsolutePath());
        VerticalGridView gridView =  view.findViewById(androidx.leanback.R.id.browse_grid);
        if (gridView != null) {
            RecyclerView.ItemAnimator animator = gridView.getItemAnimator();
            if (animator != null) {
                animator.setRemoveDuration(0);
                animator.setAddDuration(500);
            }
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            gridView.setLayoutParams(params);
            gridView.setPadding(95, 0, 0, 0);
         }

        view.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (newFocus != null && isDescendant(newFocus, view)) {
                backCallback.setEnabled(true);
            } else if (oldFocus != null && isDescendant(oldFocus, view)) {
                backCallback.setEnabled(false);
            }
        });
   }



    @Override
    public void onStop() {
        super.onStop();
        app.getGlobalEventListener().unSubscribe(GlobalEventType.FILEBROWSER_MAIN_FOLDER_SELECTION_CHANGE,mainFolderChangeHandler);
    }

    private final Consumer<Object> mainFolderChangeHandler = (folder)->{
        if(folder instanceof MainStorage changedFolder){
            if(mainParentFile.getAbsolutePath().equals(changedFolder.getFile().getAbsolutePath())) return;
            mainParentFile = changedFolder.getFile();
            currentFile.setFile(mainParentFile);
            pathTextView.setText(currentFile.getFile().getAbsolutePath());
            loadFiles();
        }
    };

    private void loadFiles(){
    if (currentFile != null && currentFile.getFile().isDirectory()) {
        File[] files = currentFile.getFile().listFiles(filter);
        if(files != null && files.length > 0){
            List<BrowserFile> browserFiles = Arrays.stream(files).map(x->new BrowserFile(x,false)).collect(Collectors.toList());
            browserFiles.sort((BrowserFile f1, BrowserFile f2) -> {
                if (f1.getFile().isDirectory() && !f2.getFile().isDirectory()) return -1;
                if (!f1.getFile().isDirectory() && f2.getFile().isDirectory()) return 1;
                return f1.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
            });
            rowAdapter.setItems(browserFiles,fileDiff);
        }
    }
    }

    private  int calculateSpanCount(Context context, int itemWidthDp) {
    // Отримати ширину екрана в dp
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;

    // Порахувати кількість колонок
    int columnCount = (int) (screenWidthDp / itemWidthDp);
    int totalSpacingDp = 30 * (columnCount - 1);
    int availableDp = (int) screenWidthDp - totalSpacingDp;
    int spanCount = availableDp / itemWidthDp;

    // Гарантувати мінімум 1 колонку
    return Math.max(spanCount, 1);
}



    private final OnBackPressedCallback backCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            if(currentFile != null && !currentFile.getFile().getAbsolutePath().equals(mainParentFile.getAbsolutePath())){
                File parentFile = currentFile.getFile().getParentFile();
                if(parentFile != null){
                    currentFile.setFile(parentFile);
                    pathTextView.setText(currentFile.getFile().getAbsolutePath());
                    loadFiles();
                }
            }
            else{
                StoragesFragment storagesFragment =
                        (StoragesFragment) requireActivity()
                                .getSupportFragmentManager()
                                .findFragmentByTag("STORAGES");
                if(storagesFragment != null){
                    storagesFragment.requestFocusOnGrid();
                }

            }
        }
    };

    private final FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if(!pathname.isDirectory()){
                String ext = FileHelper.getFileExtension(getContext(),pathname);
                return ext != null && (ext.equals("pdf") || ext.equals("epub"));
            }
            return true;
        }
    };

}
