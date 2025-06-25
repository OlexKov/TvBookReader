package com.example.bookreader.activities;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.R;
import com.example.bookreader.fragments.BookDetailsFragment;

public class BookDetailsActivity extends FragmentActivity {

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
