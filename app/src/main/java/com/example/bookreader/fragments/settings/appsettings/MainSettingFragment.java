package com.example.bookreader.fragments.settings.appsettings;

import android.os.Bundle;

import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;

import com.example.bookreader.R;

public class MainSettingFragment extends LeanbackPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_book_setting, rootKey);
    }
}
