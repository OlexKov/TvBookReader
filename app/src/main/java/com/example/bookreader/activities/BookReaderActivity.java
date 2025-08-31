package com.example.bookreader.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.fragments.reader.BookReaderFragment;
import com.example.bookreader.utility.LocaleHelper;

public class BookReaderActivity extends FragmentActivity {
    BookReaderApp app = BookReaderApp.getInstance();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, app.getLocalLanguage()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_reader_activity);
        if (savedInstanceState == null) {
            Object book = getIntent().getSerializableExtra("book_to_read");
            if(book instanceof BookDto bookToRead){
                BookReaderFragment fragment = new BookReaderFragment(bookToRead);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.book_reader_fragment, fragment)
                        .commit();
            }
        }
    }
}
