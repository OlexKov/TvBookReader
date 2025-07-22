package com.example.bookreader.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.bookreader.R;
import com.example.bookreader.fragments.NewFilesFragment;
import com.example.bookreader.utility.LocaleHelper;

public class NewFilesActivity extends FragmentActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "uk"));  // або інша мова
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        setContentView(R.layout.new_files_activity);
        fm.beginTransaction()
                .replace(R.id.new_files, new NewFilesFragment())
                .commit();

    }
}
