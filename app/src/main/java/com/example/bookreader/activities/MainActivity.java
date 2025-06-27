package com.example.bookreader.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.fragments.MainFragment;
import com.example.bookreader.R;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler handler = new Handler();
    private final Runnable resetBackPressFlag = () -> doubleBackToExitPressedOnce = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_browse_fragment, new MainFragment())
                    .commitNow();
        }
    }

    @Override
    public void onBackPressed() {

        if (BookReaderApp.getInstance().isMenuOpen()) {
            // Якщо вже в меню
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Натисніть ще раз, щоб вийти", Toast.LENGTH_SHORT).show();
            handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            super.onBackPressed();
        }
    }
}