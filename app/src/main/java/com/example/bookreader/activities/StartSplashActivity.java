package com.example.bookreader.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.constants.GlobalEventType;


@SuppressLint("CustomSplashScreen")
public class StartSplashActivity extends Activity {
    private long splashStartTime;
    private final BookReaderApp app = BookReaderApp.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_splash);
        splashStartTime = System.currentTimeMillis();
        if (!app.isDataBaseInit()) {
            app.getGlobalEventListener()
                    .subscribe(GlobalEventType.DATABASE_DONE, (o) -> runOnUiThread(this::tryStartMainActivity));
            app.DataBaseInit();
        } else {
            tryStartMainActivity();
        }
    }

    private void tryStartMainActivity() {
        long elapsed = System.currentTimeMillis() - splashStartTime;
        long delay = 2000 - elapsed;

        if (delay <= 0) {
            startMainActivity();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::startMainActivity, delay);
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Закриваємо SplashActivity, щоб не повертатися назад на неї
    }
}
