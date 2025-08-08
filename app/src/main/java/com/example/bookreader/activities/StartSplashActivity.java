package com.example.bookreader.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.utility.eventlistener.GlobalEventType;

@SuppressLint("CustomSplashScreen")
public class StartSplashActivity extends FragmentActivity {
    private long splashStartTime;
    private final BookReaderApp app = BookReaderApp.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_splash);
        splashStartTime = System.currentTimeMillis();
        if (!app.isDataBaseInit()) {
            app.getGlobalEventListener().subscribe(this,GlobalEventType.DATABASE_DONE, startMainActivityHandler, Object.class);
            app.DataBaseInit();
        } else {
            app.updateCategoryCash();
            app.getGlobalEventListener().subscribe(this,GlobalEventType.CATEGORIES_CASH_UPDATED, startMainActivityHandler, Object.class);
        }
    }

    private final Consumer<Object> startMainActivityHandler = (object)->runOnUiThread(this::tryStartMainActivity);

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
