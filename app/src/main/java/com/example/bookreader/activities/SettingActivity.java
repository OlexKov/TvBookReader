package com.example.bookreader.activities;

import android.os.Bundle;
import com.example.bookreader.fragments.settings.appsettings.SettingRootFragment;

public class SettingActivity extends BaseAppActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new SettingRootFragment())
                    .commit();
        }
    }
}
