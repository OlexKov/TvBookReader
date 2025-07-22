package com.example.bookreader.utility.bookutils;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.bookutils.interfaces.IBookProcessor;
import com.example.bookreader.utility.bookutils.pdf.PdfProcessor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class BookProcessor {
    private final IBookProcessor bookProcessor;
    private final File bookFile;
    public BookProcessor (Context context, File bookFile){
        this.bookFile = bookFile;
        String ext = FileHelper.getFileExtension( bookFile);
        if(ext == null) throw new IllegalStateException(" File extension is not exist");
        bookProcessor = switch (ext){
            case "pdf"  -> new PdfProcessor(context);
            case "epub" -> new EpubProcessor(context);
            case "fb2"  -> new Fb2Processor(context);
            default -> throw new IllegalStateException("Unexpected value: " + ext);
        };
    }

    public  CompletableFuture<BookPaths> savePreviewAsync() throws IOException{
        return bookProcessor.savePreviewAsync(bookFile);
    }

    public CompletableFuture<BookInfo> getInfoAsync() throws IOException {
        CompletableFuture<BookInfo> infoFuture = bookProcessor.getInfoAsync(bookFile);
        CompletableFuture<Bitmap> previewFuture =  bookProcessor.getPreviewAsync(bookFile,0,400,300);
        return CompletableFuture.allOf(infoFuture,previewFuture).thenApply(v->{
            BookInfo info = infoFuture.join();
            info.preview = previewFuture.join();
            return info;
        });
    }

    public CompletableFuture<Bitmap> getPreviewAsync( int pageIndex, int height, int wight) throws IOException{
        return bookProcessor.getPreviewAsync(bookFile, pageIndex, height, wight);
    }


}
