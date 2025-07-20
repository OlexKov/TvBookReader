package com.example.bookreader.utility.bookutils.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.bookreader.utility.bookutils.BookInfo;
import com.example.bookreader.utility.bookutils.BookPaths;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface BookProcessor {
    CompletableFuture<BookPaths> savePreviewAsync(File bookFile) throws IOException;
    CompletableFuture<BookInfo> getInfoAsync(Uri bookUri) throws IOException;
    CompletableFuture<BookInfo> getInfoAsync(File bookFile) throws IOException;
    CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int wight) throws IOException;
}
