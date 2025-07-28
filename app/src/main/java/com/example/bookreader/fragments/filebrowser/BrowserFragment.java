package com.example.bookreader.fragments.filebrowser;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BrowseFrameLayout;
import androidx.leanback.widget.DiffCallback;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;
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
    private Button btnConfirm;
    private LinearLayout buttonsContainer;
    private TextView currentPathView;
    private File mainParentFile = Environment.getExternalStorageDirectory();
    private BrowserMode browserMode;
    private BrowserFile currentFile = new BrowserFile(mainParentFile, false);
    private ArrayObjectAdapter storagesAdapter;
    private ArrayObjectAdapter folderGridAdapter;
    private final DiffCallback<MainStorage> storagesDiff = new MainFolderDiffCallback();
    private final List<BrowserFile> selectedFiles = new ArrayList<>();
    private final DiffCallback<BrowserFile> fileDiff = new BrowserFileDiffCallback();
    public final List<String> filesExt = Arrays.asList(new String[]{"pdf", "epub", "fb2","zip","rar"});

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_browser_fragment, container, false);
        BrowseFrameLayout browseFrameLayout = (BrowseFrameLayout) view;
        browseFrameLayout.setOnFocusSearchListener(onFocusSearchListener);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        buttonsContainer = view.findViewById(R.id.file_browser_buttons);
        storageGrid = view.findViewById(R.id.storage_list);
        folderGrid = view.findViewById(R.id.folder_grid);
        btnConfirm = view.findViewById(R.id.file_browser_confirm_button);
        currentPathView = view.findViewById(R.id.file_browser_path);
        currentPathView.setText(mainParentFile.getAbsolutePath());
        browserMode = (BrowserMode) requireActivity().getIntent().getSerializableExtra("mode");
        if (browserMode == null) {
            browserMode = BrowserMode.SINGLE_FILE;
        }
        setTitle(view, browserMode);
        setFolderGrid();
        setStorageGrid();
        setButtonsListeners(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        UsbHelper.registerUsbReceiver(requireContext(), usbReceiver);
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(this, backCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireContext().unregisterReceiver(usbReceiver);
    }

    private void loadFiles() {
        if (currentFile != null && currentFile.getFile().isDirectory()) {
            File[] files = currentFile.getFile().listFiles(filter);
            if (files != null) {
                List<BrowserFile> browserFiles = Arrays.stream(files).map(x -> {
                    BrowserFile exist = selectedFiles.stream()
                            .filter(selected -> selected.getFile().getAbsolutePath().equals(x.getAbsolutePath()))
                            .findFirst().orElse(null);
                    if (exist != null) return exist;
                    return new BrowserFile(x, false);
                }).sorted((BrowserFile f1, BrowserFile f2) -> {
                    if (f1.getFile().isDirectory() && !f2.getFile().isDirectory()) return -1;
                    if (!f1.getFile().isDirectory() && f2.getFile().isDirectory()) return 1;
                    return f1.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
                }).collect(Collectors.toList());
                folderGridAdapter.setItems(browserFiles, fileDiff);
            }
        }
    }

    private void setTitle(View view, BrowserMode browserMode) {
        TextView title = view.findViewById(R.id.file_browser_title);
        switch (browserMode) {
            case FOLDER:
                title.setText(getString(R.string.select_folder));
                buttonsContainer.setVisibility(VISIBLE);
                break;
            case SINGLE_FILE:
                title.setText(getString(R.string.select_file));
                break;
            case MULTIPLE_FILES:
                title.setText(getString(R.string.select_files));
                break;
        }
    }

    private void setFolderGrid() {
        folderGrid.setNumColumns(1);
        folderGridAdapter = new ArrayObjectAdapter(new BrowserFilePresenter(folderClickListener));
        folderGrid.setAdapter(new ItemBridgeAdapter(folderGridAdapter));
        RecyclerView.ItemAnimator animator = folderGrid.getItemAnimator();
        if (animator != null) {
            animator.setRemoveDuration(0);
            animator.setAddDuration(500);
        }
        loadFiles();
    }

    private void setStorageGrid() {
        storageGrid.setNumColumns(1);
        storagesAdapter = new ArrayObjectAdapter(new StorageFolderPresenter(storageSelectListener));
        storagesAdapter.addAll(0, getStorages());
        storageGrid.setAdapter(new ItemBridgeAdapter(storagesAdapter));
        storageGrid.setVerticalSpacing(AnimHelper.convertToDp(requireContext(), 30));
        RecyclerView.ItemAnimator storageListAnimator = storageGrid.getItemAnimator();
        if (storageListAnimator != null) {
            storageListAnimator.setRemoveDuration(0);
            storageListAnimator.setAddDuration(500);
        }
    }

    private List<MainStorage> getStorages() {
        List<MainStorage> storages = new ArrayList<>();
        storages.add(new MainStorage(R.drawable.external_storage, getString(R.string.main_storage), Environment.getExternalStorageDirectory()));
        if (getContext() != null) {
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
                    int drawableId = description.toLowerCase().contains("sd") ? R.drawable.cd_card : R.drawable.usb_drive;
                    storages.add(new MainStorage(drawableId, description, directory));
                }
            }
        }
        return storages;
    }

    private void setButtonsListeners(View view) {
        Button btnCancel = view.findViewById(R.id.file_browser_cancel_button);
        btnCancel.setOnClickListener(v -> {
            if (browserMode == BrowserMode.FOLDER) requireActivity().finish();
            for (BrowserFile file : selectedFiles) {
                file.setChecked(false);
            }
            // int focusablePosition = folderGrid.getSelectedPosition();
            folderGridAdapter.notifyItemRangeChanged(0, folderGridAdapter.size());
            selectedFiles.clear();
            buttonsContainer.setVisibility(GONE);
            folderGrid.setSelectedPosition(0);
            folderGrid.requestFocus();
        });

        btnConfirm.setOnClickListener(v -> {
            Intent result = new Intent();
            if (browserMode == BrowserMode.FOLDER) {
                result.putExtra(BrowserResult.FOLDER_PATH.name(), currentFile.getFile().getAbsolutePath());
                requireActivity().setResult(Activity.RESULT_OK, result);
            } else {
                result.putStringArrayListExtra(BrowserResult.SELECTED_FILES.name(), selectedFiles.stream()
                        .map(file -> file.getFile().getAbsolutePath())
                        .collect(Collectors.toCollection(ArrayList::new)));
                requireActivity().setResult(Activity.RESULT_OK, result);
            }
            requireActivity().finish();
        });
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Uri data = intent.getData();
            if (action == null || data == null) return;
            if (!selectedFiles.isEmpty()
                    && (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_REMOVED)
                    || action.equals(Intent.ACTION_MEDIA_EJECT))) {

                selectedFiles.removeIf(file -> !file.getFile().exists());
                if (selectedFiles.isEmpty()) {
                    buttonsContainer.setVisibility(GONE);
                }
            }
            storagesAdapter.setItems(getStorages(), storagesDiff);
        }
    };

    private final FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (!pathname.isDirectory()) {
                String ext = FileHelper.getFileExtension(pathname);
                return ext != null && filesExt.contains(ext);
            }
            return true;
        }
    };

    @SuppressLint("SetTextI18n")
    private final Consumer<Object> folderClickListener = (item) -> {
        if (item instanceof BrowserFile file) {
            if (file.getFile().isDirectory()) {
                currentFile = file;
                currentPathView.setText(currentFile.getFile().getAbsolutePath());
                loadFiles();
            } else {
                if (browserMode == BrowserMode.MULTIPLE_FILES) {
                    boolean checked = !file.isChecked();
                    file.setChecked(checked);
                    int index = folderGridAdapter.indexOf(file);
                    folderGridAdapter.notifyItemRangeChanged(index, 1);
                    if (checked) {
                        if (!selectedFiles.contains(file)) {
                            selectedFiles.add(file);
                            btnConfirm.setText(getString(R.string.select) + "  (" + selectedFiles.size() + ")");
                        }

                        if (buttonsContainer.getVisibility() != VISIBLE) {
                            buttonsContainer.setVisibility(VISIBLE);
                        }
                    } else {
                        selectedFiles.remove(file);
                        btnConfirm.setText(getString(R.string.select) + "  (" + selectedFiles.size() + ")");
                        if (selectedFiles.isEmpty() && buttonsContainer.getVisibility() == VISIBLE) {
                            buttonsContainer.setVisibility(GONE);
                        }
                    }
                } else if (browserMode == BrowserMode.SINGLE_FILE) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(BrowserResult.SELECTED_FILE_PATH.name(), file.getFile().getAbsolutePath());
                    requireActivity().setResult(Activity.RESULT_OK, resultIntent);
                    requireActivity().finish();
                }
            }
        }
    };

    private final Consumer<Object> storageSelectListener = (item) -> {
        if (item instanceof MainStorage folder) {
            if (mainParentFile.getAbsolutePath().equals(folder.getFile().getAbsolutePath())) return;
            mainParentFile = folder.getFile();
        }
        if(currentFile.getFile() != mainParentFile){
            currentFile.setFile(mainParentFile);
            currentPathView.setText(currentFile.getFile().getAbsolutePath());
            loadFiles();
        }
    };

    private final OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(storageGrid.hasFocus()){
                requireActivity().finish();
            }
            else if (currentFile != null && !currentFile.getFile().getAbsolutePath().equals(mainParentFile.getAbsolutePath())) {
                File parentFile = currentFile.getFile().getParentFile();
                if (parentFile != null) {
                    BrowserFile lastOpenedFile = new BrowserFile(currentFile);
                    currentFile.setFile(parentFile);
                    currentPathView.setText(currentFile.getFile().getAbsolutePath());
                    loadFiles();
                    int lastOpenedFilePosition = folderGridAdapter.indexOf(lastOpenedFile);
                    folderGrid.setSelectedPosition(lastOpenedFilePosition);
                }
            }
            else {
                storageGrid.requestFocus();
            }
        }
    };

    private final BrowseFrameLayout.OnFocusSearchListener onFocusSearchListener = new BrowseFrameLayout.OnFocusSearchListener() {
        @Override
        public @org.jspecify.annotations.Nullable View onFocusSearch(@org.jspecify.annotations.Nullable View focused, int direction) {
            if (direction == View.FOCUS_RIGHT && storageGrid.indexOfChild(focused) != -1) {
                return folderGrid;
            }
            else if (direction == View.FOCUS_DOWN && folderGrid.indexOfChild(focused) != -1) {
                 return  buttonsContainer.getVisibility() != View.GONE ? btnConfirm : folderGrid;
            }
            return null;
        }
    };
}
