package com.example.bookreader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.example.bookreader.customclassses.BookInfo;
import com.example.bookreader.interfaces.BookProcessor;
import com.kursx.parser.fb2.Binary;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Fb2Processor implements BookProcessor {
    private static final String TAG = "Fb2Processor";
    private final Context context;

    public Fb2Processor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<BookInfo> processFileAsync(File bookFile) {
        long start = System.currentTimeMillis();

        CompletableFuture<BookInfo> getBookInfoFuture = CompletableFuture.supplyAsync(() -> {
            Log.d(TAG, "Start getBookInfo at " + (System.currentTimeMillis() - start));
            BookInfo result = new BookInfo();
            try  {
                FictionBook fb = new FictionBook(bookFile);
                result.title = fb.getTitle();
                result.author = fb.getAuthors().get(0).getFullName();
            } catch (Exception e) {
                Log.e(TAG, "Error reading book info", e);
                throw new RuntimeException(e);
            }
            Log.d(TAG, "End getBookInfo at " + (System.currentTimeMillis() - start));
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

        CompletableFuture<BookInfo> getBookPreviewFuture = CompletableFuture.supplyAsync(() -> {
            Log.d(TAG, "Start getBookPreview at " + (System.currentTimeMillis() - start));
            BookInfo result = new BookInfo();
            try {
                FictionBook fb = new FictionBook(bookFile);
                Bitmap cover = extractCoverPreview(fb, 300, 400);
                if (cover == null) {
                    Log.d(TAG, "Cover image not found");
                    return result;
                }

                // Збереження прев’ю
                File previewDir = new File(context.getFilesDir(), "previews");
                if (!previewDir.exists()) previewDir.mkdirs();

                String baseName = bookFile.getName();
                if (baseName.toLowerCase().endsWith(".fb2")) {
                    baseName = baseName.substring(0, baseName.length() - 4);
                }

                File previewFile = new File(previewDir, baseName + "_preview.png");
                try (FileOutputStream out = new FileOutputStream(previewFile)) {
                    cover.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                }

                result.filePath = bookFile.getAbsolutePath();
                result.previewPath = previewFile.getAbsolutePath();

            } catch (Exception e) {
                Log.e(TAG, "Error generating preview", e);
                throw new RuntimeException(e);
            }
            Log.d(TAG, "End getBookPreview at " + (System.currentTimeMillis() - start));
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

        return CompletableFuture.allOf(getBookInfoFuture, getBookPreviewFuture).thenApply(v -> {
            BookInfo bookWithInfo = getBookInfoFuture.join();
            BookInfo bookWithPaths = getBookPreviewFuture.join();

            if (bookWithInfo == null) bookWithInfo = new BookInfo();
            if (bookWithPaths == null) bookWithPaths = new BookInfo();

            if (bookWithInfo.title == null || bookWithInfo.title.trim().isEmpty()) {
                bookWithInfo.title = bookFile.getName();
            }
            if (bookWithInfo.author == null || bookWithInfo.author.trim().isEmpty()) {
                bookWithInfo.author = "Unknown";
            }
            bookWithInfo.previewPath = bookWithPaths.previewPath;
            bookWithInfo.filePath = bookWithPaths.filePath;

            return bookWithInfo;
        });
    }

    @Override
    public CompletableFuture<BookInfo> processFileAsync(Uri bookUri) throws java.io.IOException {
        File file = new File(FileHelper.getPath(context, bookUri));
        return processFileAsync(file);
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int width) {
        return CompletableFuture.supplyAsync(() -> {
            try  {
                FictionBook fb = new FictionBook(bookFile);
                return extractCoverPreview(fb, width, height);
            } catch (Exception e) {
                Log.e(TAG, "Error in getPreviewAsync", e);
                return null;
            }
        });
    }

    private Bitmap extractCoverPreview(FictionBook fb, int width, int height) {
        try {
            ArrayList<Image> coverImages = fb.getDescription().getTitleInfo().getCoverPage();
            if (coverImages == null || coverImages.isEmpty()) {
                return null;
            }
            String href = coverImages.get(0).getValue(); // Наприклад "#cover.jpg"

            if (href == null || !href.startsWith("#")) {
                return null;
            }

            String coverId = href.substring(1);
            Map<String, Binary> binaries = fb.getBinaries();
            if (binaries == null) return null;

            Binary bin = binaries.get(coverId);
            if (bin == null) return null;

            String base64String = bin.getBinary();
            if (base64String == null || base64String.isEmpty()) return null;

            byte[] imageData = Base64.decode(base64String, Base64.DEFAULT);

            // Отримуємо розміри без декодування повністю
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
            Log.e(TAG, "Failed to extract cover preview", e);
        }
        return null;
    }
}