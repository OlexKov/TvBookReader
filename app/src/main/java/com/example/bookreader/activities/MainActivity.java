package com.example.bookreader.activities;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import com.example.bookreader.utility.eventlistener.GlobalEventType;
import com.example.bookreader.fragments.MainFragment;
import com.example.bookreader.R;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends BaseAppActivity {
    private boolean doubleBackToExitPressedOnce = false;
    private boolean backToMain = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Consumer<Boolean> menuChangeHandler = (menuOpen)-> backToMain = menuOpen;
    private static final int REQUEST_STORAGE_PERMISSIONS = 100;

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
                        finish();
                    } else {
                        doubleBackToExitPressedOnce = true;
                        Toast.makeText(MainActivity.this, "Натисніть ще раз, щоб вийти", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                    }
                }
                else if(!backToMain){
                    setEnabled(false);  // Вимикаємо цей callback
                    MainActivity.super.getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                    backToMain = true;
                }
            }
        });
        app.getGlobalEventListener().subscribe(this, GlobalEventType.MENU_STATE_CHANGED,menuChangeHandler,boolean.class);
        checkStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                finishAffinity();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finishAffinity(); // ← Закриває всі активності
            }
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (MANAGE_EXTERNAL_STORAGE)
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    // fallback — загальні налаштування доступу до всіх файлів
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        } else {
            // Android 6–10
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSIONS);
            }
        }
    }

}