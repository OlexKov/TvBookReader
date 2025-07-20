package com.example.bookreader.utility.bookutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.bookreader.utility.bookutils.interfaces.BookProcessor;
import com.example.bookreader.utility.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;


public class EpubProcessor implements BookProcessor {
    private final Context context;

    public EpubProcessor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<BookPaths> savePreviewAsync(File bookFile) throws IOException {
        return  CompletableFuture.supplyAsync(() -> {
            BookPaths result = new BookPaths();
            try {
                EpubReader epubReader = new EpubReader();
                Book book = epubReader.readEpub(new FileInputStream(bookFile));

                Bitmap cover = extractCoverPreview(book, 300, 400);
                if (cover == null) {
                    Log.d("EpubProcessor", "Cover not found");
                    return null;
                }

                // Збереження прев’ю
                File previewDir = new File(context.getFilesDir(), "previews");
                if (!previewDir.exists()) previewDir.mkdirs();

                String baseName = bookFile.getName();
                if (baseName.toLowerCase().endsWith(".epub")) {
                    baseName = baseName.substring(0, baseName.length() - 5);
                }

                File previewFile = new File(previewDir, baseName + "_preview.png");
                try (FileOutputStream out = new FileOutputStream(previewFile)) {
                    cover.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                }

                result.filePath = bookFile.getAbsolutePath();
                result.previewPath = previewFile.getAbsolutePath();

            } catch (Exception e) {
                Log.e("EpubProcessor", "Error generating preview", e);
                throw new RuntimeException(e);
            }
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(File bookFile) throws IOException {

       return   CompletableFuture.supplyAsync(() -> {
           BookInfo result = new BookInfo();
           try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(bookFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
                EpubReader epubReader = new EpubReader();
                Book book = epubReader.readEpub(fis);

                result.title = book.getTitle();

                result.author = Optional.of(book)
                            .map(Book::getMetadata)
                            .map(Metadata::getAuthors)
                            .filter((List<Author> list) -> !list.isEmpty())
                            .map(list -> list.get(0).getFirstname() + " " + list.get(0).getLastname())
                            .orElse("Unknown");

                result.year = Optional.of(book)
                        .map(Book::getMetadata)
                        .map(Metadata::getDates)
                        .filter((List<Date> list) -> !list.isEmpty())
                        .map(list -> list.get(0).getValue())
                        .orElse("Unknown");

               result.description = Optional.of(book)
                       .map(Book::getMetadata)
                       .map(Metadata::getDescriptions)
                       .filter((List<String> list) -> !list.isEmpty())
                       .map(list -> String.join(" ",list))
                       .orElse("Unknown");

               result.pageCount = book.getSpine().size();

               if (result.title == null || result.title.trim().isEmpty()) {
                   result.title = bookFile.getName().substring(0,bookFile.getName().length() - 4);
               }

            } catch (Exception e) {
                Log.e("EpubProcessor", "Error reading book info", e);
                throw new RuntimeException(e);
            }
           return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(Uri bookUri) throws IOException {
        File bookFile = new File(FileHelper.getPath(context, bookUri));
        return getInfoAsync(bookFile);
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int width) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EpubReader epubReader = new EpubReader();
                Book book = epubReader.readEpub(new FileInputStream(bookFile));
                return extractCoverPreview(book, width, height);
            } catch (Exception e) {
                Log.e("EpubProcessor", "Error in getPreviewAsync", e);
                return null;
            }
        });
    }

    private Bitmap extractCoverPreview(Book book, int width, int height) {
        try {
            Resource coverImage = book.getCoverImage();
            if (coverImage == null) return null;

            byte[] imageData = coverImage.getData();
            if (imageData == null || imageData.length == 0) return null;

            // Визначаємо sampleSize без повного декодування
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            int sampleSize = 1;
            while ((originalWidth / sampleSize) > width || (originalHeight / sampleSize) > height) {
                sampleSize *= 2;
            }

            BitmapFactory.Options scaledOptions = new BitmapFactory.Options();
            scaledOptions.inSampleSize = sampleSize;

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, scaledOptions);
            if (bitmap == null) return null;

            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        } catch (Exception e) {
            Log.e("EpubProcessor", "Failed to extract cover preview", e);
        }
        return null;
    }
}