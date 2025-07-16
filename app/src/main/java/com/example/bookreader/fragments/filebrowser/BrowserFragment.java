package com.example.bookreader.fragments.filebrowser;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DiffCallback;
import androidx.leanback.widget.ItemBridgeAdapter;

import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.customclassses.BrowserFile;
import com.example.bookreader.customclassses.MainStorage;
import com.example.bookreader.diffcallbacks.BrowserFileDiffCallback;
import com.example.bookreader.diffcallbacks.MainFolderDiffCallback;
import com.example.bookreader.presenters.browserpresenters.BrowserFilePresenter;
import com.example.bookreader.presenters.browserpresenters.StorageFolderPresenter;
import com.example.bookreader.utility.AnimHelper;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.UsbHelper;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BrowserFragment extends Fragment {
    private VerticalGridView storageGrid;
    private VerticalGridView folderGrid;
    private Button btnCancel;
    private Button btnConfirm;
    private LinearLayout buttonsContainer;
    private TextView currentPathView;
    private File mainParentFile =  Environment.getExternalStorageDirectory();
    private final DiffCallback<BrowserFile> fileDiff = new BrowserFileDiffCallback();
    private BrowserFile currentFile = new BrowserFile(mainParentFile,false);
    private ArrayObjectAdapter storagesAdapter;
    private ArrayObjectAdapter folderGridAdapter;
    private final BookReaderApp app = BookReaderApp.getInstance();
    private final DiffCallback<MainStorage> storagesDiff = new MainFolderDiffCallback();
    private final List<BrowserFile> selectedFiles = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(  @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        return inflater.inflate(R.layout.file_browser_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        buttonsContainer = view.findViewById(R.id.file_browser_buttons);
        storageGrid = view.findViewById(R.id.storage_list);
        folderGrid = view.findViewById(R.id.folder_grid);
        btnCancel = view.findViewById(R.id.file_browser_cancel_button);
        btnConfirm = view.findViewById(R.id.file_browser_confirm_button);

        currentPathView = view.findViewById(R.id.file_browser_path);
        currentPathView.setText(mainParentFile.getAbsolutePath());

        //folder grid settings
        int columnCount = calculateSpanCount(requireContext(), 80);
        folderGrid.setNumColumns(columnCount);
        folderGridAdapter = new ArrayObjectAdapter(new BrowserFilePresenter(folderClickListener));
        folderGrid.setAdapter(new ItemBridgeAdapter(folderGridAdapter));
        RecyclerView.ItemAnimator animator = folderGrid.getItemAnimator();
        if (animator != null) {
            animator.setRemoveDuration(0);
            animator.setAddDuration(500);
        }
        folderGrid.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                view.setOnFocusChangeListener((v, hasFocus) -> {
                    v.post(()->{
                        AnimHelper.scale(v,1.2f,hasFocus,150);
                    });
                });
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                view.animate().cancel();
                view.setScaleX(1f);
                view.setScaleY(1f);
            }
        });

        loadFiles();

        //storage grid settings
        storageGrid.setNumColumns(1);
        storagesAdapter = new ArrayObjectAdapter(new StorageFolderPresenter(storageSelectListener));
        storagesAdapter.addAll(0,getStorages());
        storageGrid.setAdapter(new ItemBridgeAdapter(storagesAdapter));
        storageGrid.setVerticalSpacing(AnimHelper.convertToDp(getContext(),30));
        RecyclerView.ItemAnimator storageListAnimator = storageGrid.getItemAnimator();
        if (storageListAnimator != null) {
            storageListAnimator.setRemoveDuration(0);
            storageListAnimator.setAddDuration(500);
        }


        btnCancel.setOnClickListener(v -> {
            for (BrowserFile file:selectedFiles){
                file.setChecked(false);
            }
           // int focusablePosition = folderGrid.getSelectedPosition();
            folderGridAdapter.notifyItemRangeChanged(0,folderGridAdapter.size());
            selectedFiles.clear();
            buttonsContainer.setVisibility(GONE);
            folderGrid.setSelectedPosition(0);
            folderGrid.requestFocus();
        });
        btnConfirm.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Вибрано: ", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        UsbHelper.registerUsbReceiver(requireContext(),usbReceiver);
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(this, backCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireContext().unregisterReceiver(usbReceiver);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Uri data = intent.getData();
            if (action == null || data == null) return;
            switch (action) {
                case Intent.ACTION_MEDIA_MOUNTED:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                case Intent.ACTION_MEDIA_REMOVED:
                case Intent.ACTION_MEDIA_EJECT:
                    storagesAdapter.setItems(getStorages(),storagesDiff);
                    break;
            }
        }
    };

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

    private void loadFiles(){
        if (currentFile != null && currentFile.getFile().isDirectory()) {
            File[] files = currentFile.getFile().listFiles(filter);
            if (files != null) {
                List<BrowserFile> browserFiles = Arrays.stream(files).map(x ->{
                    BrowserFile exist = selectedFiles.stream()
                            .filter(selected->selected.getFile().getAbsolutePath().equals(x.getAbsolutePath()))
                            .findFirst().orElse(null);
                    if(exist != null) return exist;
                    return  new BrowserFile(x, false);
                }).sorted((BrowserFile f1, BrowserFile f2) -> {
                    if (f1.getFile().isDirectory() && !f2.getFile().isDirectory()) return -1;
                    if (!f1.getFile().isDirectory() && f2.getFile().isDirectory()) return 1;
                    return f1.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
                }).collect(Collectors.toList());
                folderGridAdapter.setItems(browserFiles, fileDiff);
            }
        }
    }

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

    private final Consumer<Object> folderClickListener = (item)->{
        if(item instanceof BrowserFile file){
            if(file.getFile().isDirectory()){
                currentFile = file;
                currentPathView.setText(currentFile.getFile().getAbsolutePath());
                loadFiles();
            }
            else{
                boolean checked = !file.isChecked();
                file.setChecked(checked);
                int index = folderGridAdapter.indexOf(file);
                folderGridAdapter.notifyItemRangeChanged(index,1);
                if(checked){
                    if(!selectedFiles.contains(file)){
                        selectedFiles.add(file);
                    }

                    if(buttonsContainer.getVisibility() != VISIBLE){
                        buttonsContainer.setVisibility(VISIBLE);
                    }
                }
                else{
                    selectedFiles.remove(file);
                    if(selectedFiles.isEmpty() && buttonsContainer.getVisibility() == VISIBLE){
                        buttonsContainer.setVisibility(GONE);
                    }
                }

//                        Intent resultIntent = new Intent();
//                        resultIntent.putExtra("SELECTED_FILE_PATH", file.getAbsolutePath());
//                        requireActivity().setResult(Activity.RESULT_OK, resultIntent);
//                        requireActivity().finish();
            }
        }
    };

    private final Consumer<Object> storageSelectListener = (item)->{
        if(item instanceof MainStorage folder){
            if(mainParentFile.getAbsolutePath().equals(folder.getFile().getAbsolutePath())) return;
            mainParentFile = folder.getFile();
            currentFile.setFile(mainParentFile);
            currentPathView.setText(currentFile.getFile().getAbsolutePath());
            loadFiles();
        }
    };

    private List<MainStorage> getStorages(){
        List<MainStorage> storages = new ArrayList<>();
        storages.add(new MainStorage( R.drawable.external_storage,getString(R.string.main_storage),Environment.getExternalStorageDirectory()) );
        if(getContext() != null){
            StorageManager storageManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);
            List<StorageVolume> volumes = storageManager.getStorageVolumes();

            for (StorageVolume volume : volumes) {
                if (volume.isRemovable() && volume.getState().equals(Environment.MEDIA_MOUNTED)) {
                    File directory = null;
                    if (Build.VERSION.SDK_INT >= 30) {
                        directory = volume.getDirectory();
                    } else {
                        String uuid = volume.getUuid();
                        if (uuid != null) {
                            directory = new File("/storage/" + uuid);
                        }
                    }
                    String description = volume.getDescription(getContext());
                    int drawableId = description.toLowerCase().contains("sd")?R.drawable.cd_card : R.drawable.usb_drive;
                    storages.add(new MainStorage(drawableId, description, directory));
                }
            }
        }
        return storages;
    }

    private final OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(currentFile != null && !currentFile.getFile().getAbsolutePath().equals(mainParentFile.getAbsolutePath())){
                File parentFile = currentFile.getFile().getParentFile();
                if(parentFile != null){
                    BrowserFile lastOpenedFile = new BrowserFile(currentFile);
                    currentFile.setFile(parentFile);
                    currentPathView.setText(currentFile.getFile().getAbsolutePath());
                    loadFiles();
                    int lastOpenedFilePosition = folderGridAdapter.indexOf(lastOpenedFile);
                    folderGrid.setSelectedPosition(lastOpenedFilePosition);
                }
            }
            else{
                if(storageGrid.hasFocus()){
                    requireActivity().finish();
                }
                else{
                    storageGrid.requestFocus();
                }
            }
        }
    };
}
