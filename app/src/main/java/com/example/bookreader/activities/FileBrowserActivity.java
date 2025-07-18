package com.example.bookreader.activities;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;


import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.widget.BrowseFrameLayout;


import com.example.bookreader.R;
import com.example.bookreader.fragments.filebrowser.BrowserFragment;
import com.example.bookreader.utility.LocaleHelper;

import org.jspecify.annotations.Nullable;

public class FileBrowserActivity extends FragmentActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "uk"));  // або інша мова
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        setContentView(R.layout.browser_activity);
        fm.beginTransaction()
                .replace(R.id.file_browser, new BrowserFragment())
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
