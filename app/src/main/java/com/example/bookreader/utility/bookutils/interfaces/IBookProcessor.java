package com.example.bookreader.utility.bookutils.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;
import com.example.bookreader.data.database.dto.BookDto;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface IBookProcessor {
    CompletableFuture<String> savePreviewAsync(String bookPath,int height, int wight) throws IOException;
    CompletableFuture<String> savePreviewAsync(File bookFile,int height, int wight) throws IOException;
    CompletableFuture<BookDto> getInfoAsync(Uri bookUri) throws IOException;
    CompletableFuture<BookDto> getInfoAsync(File bookFile) throws IOException;
    CompletableFuture<BookDto> getInfoAsync(String bookPath) throws IOException;
    CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int wight) throws IOException;
    CompletableFuture<Bitmap> getPreviewAsync(String bookPath, int pageIndex, int height, int wight) throws IOException;
}
