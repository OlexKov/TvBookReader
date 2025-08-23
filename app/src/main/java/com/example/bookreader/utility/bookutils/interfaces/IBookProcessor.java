package com.example.bookreader.utility.bookutils.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;
import com.example.bookreader.data.database.dto.BookDto;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface IBookProcessor {
    CompletableFuture<String> savePreviewAsync(String bookPath,int height, int wight) ;
    CompletableFuture<String> savePreviewAsync(File bookFile,int height, int wight) ;
    CompletableFuture<BookDto> getInfoAsync(Uri bookUri) ;
    CompletableFuture<BookDto> getInfoAsync(File bookFile) ;
    CompletableFuture<BookDto> getInfoAsync(String bookPath) ;
    CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int wight) ;
    CompletableFuture<Bitmap> getPreviewAsync(String bookPath, int pageIndex, int height, int wight);
}
