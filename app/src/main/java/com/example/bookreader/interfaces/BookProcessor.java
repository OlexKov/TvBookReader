package com.example.bookreader.interfaces;

import android.content.Context;
import android.net.Uri;

import com.example.bookreader.customclassses.BookInfo;

import java.io.File;
import java.io.IOException;

public interface BookProcessor {
    BookInfo processFile(Context context, Uri bookUri) throws IOException;
    BookInfo processFile(Context context, File bookFile) throws IOException;
}
