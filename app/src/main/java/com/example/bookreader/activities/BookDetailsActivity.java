package com.example.bookreader.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.R;
import com.example.bookreader.fragments.BookDetailsFragment;
import com.example.bookreader.utility.LocaleHelper;

public class BookDetailsActivity extends FragmentActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "uk"));  // або інша мова
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment, new BookDetailsFragment())
                    .commitNow();
        }
    }
}
