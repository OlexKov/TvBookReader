package com.example.bookreader.activities;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import com.example.bookreader.R;
import com.example.bookreader.fragments.filebrowser.BrowserFragment;

public class FileBrowserActivity extends BaseAppActivity {
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
}
