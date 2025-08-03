package com.example.bookreader.fragments.settings.appsettings;

import android.os.Bundle;

import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;

import com.example.bookreader.R;

public class CategorySettingsFragment extends LeanbackPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.book_setting, rootKey);
    }
}
