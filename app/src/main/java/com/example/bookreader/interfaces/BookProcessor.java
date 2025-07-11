package com.example.bookreader.interfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.bookreader.customclassses.BookInfo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface BookProcessor {
    CompletableFuture<BookInfo> processFileAsync(Context context, Uri bookUri) throws IOException;
    CompletableFuture<BookInfo> processFileAsync(Context context, File bookFile) throws IOException;
    CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int wight) throws IOException;
}
