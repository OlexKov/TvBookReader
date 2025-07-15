package com.example.bookreader.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;



import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.VerticalGridSupportFragment;


import com.example.bookreader.R;
import com.example.bookreader.fragments.filebrowser.FolderContentFragment;
import com.example.bookreader.fragments.filebrowser.StoragesFragment;
import com.example.bookreader.utility.LocaleHelper;

public class FileBrowserActivity extends FragmentActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "uk"));  // або інша мова
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        setContentView(R.layout.file_browser_activity);
        VerticalGridSupportFragment rowsFragment = new StoragesFragment();
        VerticalGridSupportFragment gridFragment = new FolderContentFragment();
        fm.beginTransaction()
                .add(R.id.storage_folders, rowsFragment,"STORAGES")
                .add(R.id.folder_content, gridFragment,"FOLDER_CONTENT")
                .commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }

    public void onCloseClicked(View view) {
        finish(); // закриває активність
    }
}
