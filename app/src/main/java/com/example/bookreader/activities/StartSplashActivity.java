package com.example.bookreader.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.util.Consumer;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.utility.eventlistener.GlobalEventType;




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
            app.getGlobalEventListener().subscribe(GlobalEventType.DATABASE_DONE, startMainActivityHandler);
            app.DataBaseInit();
        } else {
            app.updateCategoryCash();
            app.getGlobalEventListener().subscribe(GlobalEventType.CATEGORY_CASH_UPDATED, startMainActivityHandler);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        app.getGlobalEventListener().unSubscribe(GlobalEventType.DATABASE_DONE, startMainActivityHandler);
        app.getGlobalEventListener().unSubscribe(GlobalEventType.CATEGORY_CASH_UPDATED, startMainActivityHandler);
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
