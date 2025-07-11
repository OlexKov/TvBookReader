package com.example.bookreader.fragments.filebrowser;

import static androidx.leanback.widget.FocusHighlight.ZOOM_FACTOR_MEDIUM;

import static com.example.bookreader.utility.ViewHelper.isDescendant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DiffCallback;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.VerticalGridView;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.customclassses.MainFolder;
import com.example.bookreader.presenters.browserpresenters.MainFolderPresenter;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class StoragesFragment extends VerticalGridSupportFragment {
    private final BookReaderApp app = BookReaderApp.getInstance();
    private ArrayObjectAdapter folderRowAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(ZOOM_FACTOR_MEDIUM,false);
        gridPresenter.setNumberOfColumns(1);
        gridPresenter.setShadowEnabled(false);
        setGridPresenter(gridPresenter);
        folderRowAdapter = new ArrayObjectAdapter(new MainFolderPresenter());
        folderRowAdapter.addAll(0,getStorages());

        setAdapter(folderRowAdapter);
        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                if(item instanceof MainFolder folder){
                    app.getGlobalEventListener().sendEvent(GlobalEventType.FILEBROWSER_MAIN_FOLDER_SELECTION_CHANGE,folder);
                }
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VerticalGridView gridView = view.findViewById(androidx.leanback.R.id.browse_grid);
        gridView.setPadding(0, 0, 0, 0);

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), backCallback);

        view.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (newFocus != null && isDescendant(newFocus, view)) {
               backCallback.setEnabled(true);
            } else if (oldFocus != null && isDescendant(oldFocus, view)) {
               backCallback.setEnabled(false);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        requireContext().registerReceiver(usbReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireContext().unregisterReceiver(usbReceiver);
    }

    public void requestFocusOnGrid() {
        View root = getView();
        if (root != null) {
            VerticalGridView gridView = root.findViewById(androidx.leanback.R.id.browse_grid);
            if (gridView != null) {
                gridView.requestFocus();
            }
        }
    }

    private final  OnBackPressedCallback backCallback = new OnBackPressedCallback(true){
        @Override
        public void handleOnBackPressed() {
            requireActivity().finish();
        }
    };

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
                    folderRowAdapter.setItems(getStorages(),folderDiff);
                    break;
            }
        }
    };

    private List<MainFolder> getStorages(){
        List<MainFolder> storages = new ArrayList<>();
        storages.add(new MainFolder( R.drawable.external_storage,getString(R.string.main_storage),Environment.getExternalStorageDirectory()) );
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
                    storages.add(new MainFolder(drawableId, description, directory));
                }
            }
        }
        return storages;
    }

    private final DiffCallback<MainFolder> folderDiff = new DiffCallback<MainFolder>() {
        @Override
        public boolean areItemsTheSame(MainFolder oldItem, MainFolder newItem) {
            // Чи це той самий об’єкт (для ID, шляху тощо)
            return oldItem.getFile().getAbsolutePath().equals(newItem.getFile().getAbsolutePath());
        }

        @Override
        public boolean areContentsTheSame(MainFolder oldItem, MainFolder newItem) {
            // Чи вміст однаковий (наприклад, за датою модифікації)
            return oldItem.getName().equals(newItem.getName());
        }
    };

}
