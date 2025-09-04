package com.example.bookreader.activities;


import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import com.example.bookreader.R;
import com.example.bookreader.fragments.LoadFilesFragment;

public class NewFilesActivity extends BaseAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        setContentView(R.layout.new_files_activity);
        fm.beginTransaction()
                .replace(R.id.load_files, new LoadFilesFragment())
                .commit();

    }
}
