package com.example.bookreader.activities;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.constants.GlobalEventType;
import com.example.bookreader.fragments.MainFragment;
import com.example.bookreader.R;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {
    private boolean doubleBackToExitPressedOnce = false;
    private boolean backToMain = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BookReaderApp app = BookReaderApp.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_browse_fragment, new MainFragment())
                    .commitNow();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d("Exit log", String.valueOf(backToMain));
                if (app.isMenuOpen()) {
                    if (doubleBackToExitPressedOnce) {
                        Log.d("Exit log","App exit");
                        finish();
                    } else {
                        doubleBackToExitPressedOnce = true;
                        Toast.makeText(MainActivity.this, "Натисніть ще раз, щоб вийти", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                    }
                }
                else if(!backToMain){
                    Log.d("Exit log","App back");
                    setEnabled(false);  // Вимикаємо цей callback
                    MainActivity.super.getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                    backToMain = true;
                }
            }
        });
        app.getGlobalEventListener().subscribe(GlobalEventType.MENU_STATE_CHANGED,(menuOpen)->{
            backToMain = (boolean)menuOpen;
        });
    }
}