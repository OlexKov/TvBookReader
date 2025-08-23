package com.example.bookreader.utility;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastHelper {
    public static void createToast(Context context,String message){
        new Handler(Looper.getMainLooper()).post(()-> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
}
