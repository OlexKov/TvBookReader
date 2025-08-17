package com.example.bookreader.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.data.database.repository.BookRepository;
import com.example.bookreader.fragments.BookDetailsFragment;
import com.example.bookreader.utility.LocaleHelper;


public class BookDetailsActivity extends FragmentActivity {
    BookReaderApp app = BookReaderApp.getInstance();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, app.getLocalLanguage()));
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_details);
        if (savedInstanceState == null) {
            Object id = getIntent().getSerializableExtra("BOOK_ID");
            if(id instanceof Long bookId){
                BookDetailsFragment fragment = BookDetailsFragment.newInstance(bookId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.details_fragment, fragment)
                        .commit();
            }
        }
    }

    public void changeBook(Long bookId) {
        BookDetailsFragment fragment = BookDetailsFragment.newInstance(bookId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.details_fragment, fragment)
                .addToBackStack(null) // дозволяє повернутися назад
                .commit();
    }
}
