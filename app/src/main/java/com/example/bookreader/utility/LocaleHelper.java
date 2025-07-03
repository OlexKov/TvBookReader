package com.example.bookreader.utility;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        return context;
    }

    // Метод, який можна викликати у Activity
    public static void updateResources(Activity activity, String languageCode) {
        Context context = setLocale(activity, languageCode);
        activity.getResources().updateConfiguration(
                context.getResources().getConfiguration(),
                context.getResources().getDisplayMetrics());
        activity.recreate();
    }
}
