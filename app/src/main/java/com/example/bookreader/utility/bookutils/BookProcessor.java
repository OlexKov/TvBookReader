package com.example.bookreader.utility.bookutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.HashHelper;
import com.example.bookreader.utility.bookutils.interfaces.IBookProcessor;
import com.example.bookreader.utility.bookutils.pdf.PdfProcessor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class BookProcessor {
    private final IBookProcessor bookProcessor;
    private final String bookPath;

    public BookProcessor (Context context, String bookPath){
        this.bookPath = bookPath;
        String ext = FileHelper.getPathFileExtension(bookPath);
        bookProcessor = getBookProcessor(context,ext);
    }

    public BookProcessor (Context context, File bookFile){
        this.bookPath = bookFile.getAbsolutePath();
        String ext = FileHelper.getFileExtension( bookFile);
        bookProcessor = getBookProcessor(context,ext);
    }

    public  CompletableFuture<String> savePreviewAsync() throws IOException{
        return bookProcessor.savePreviewAsync(bookPath,400,300);
    }

    public CompletableFuture<BookDto> getInfoAsync() throws IOException {
        return bookProcessor.getInfoAsync(bookPath).thenApply((info)->{
            info.filePath = bookPath;
            info.fileSize = BooksArchiveReader.isArchivePath(bookPath) ? new BooksArchiveReader().getFileSize(bookPath) : new File(bookPath).length();
            info.fileHash = HashHelper.getStringHash(info.author + info.pageCount + info.description + info.year + info.fileSize);
            return  info;
        });
    }

    public CompletableFuture<Bitmap> getPreviewAsync( int pageIndex, int height, int wight) throws IOException{
        return bookProcessor.getPreviewAsync(bookPath, pageIndex, height, wight);
    }

    private IBookProcessor getBookProcessor(Context context,String ext){
        if(ext == null) throw new IllegalStateException(" File extension is not exist");
        return switch (ext){
            case "pdf"  -> new PdfProcessor(context);
            case "epub" -> new EpubProcessor(context);
            case "fb2"  -> new Fb2Processor(context);
            default -> throw new IllegalStateException("Unexpected value: " + ext);
        };
    }
}
