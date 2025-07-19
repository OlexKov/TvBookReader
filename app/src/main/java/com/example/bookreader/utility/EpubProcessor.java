package com.example.bookreader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import android.graphics.Paint;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.example.bookreader.customclassses.BookInfo;
import com.example.bookreader.interfaces.BookProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import nl.siegmann.epublib.epub.EpubReader;
import nl. siegmann. epublib. domain. Book;

public class EpubProcessor implements BookProcessor {
    private final Context context;

    public EpubProcessor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<BookInfo> processFileAsync( File bookFile) throws IOException{

        CompletableFuture<BookInfo> getBookInfoFuture = CompletableFuture.supplyAsync(()->{
            BookInfo result = new BookInfo();
            // Читаємо EPUB метадані
            try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(bookFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
                EpubReader epubReader = new EpubReader();
                Book book = epubReader.readEpub(fis);

                result.title = book.getTitle();
                if (book.getMetadata().getAuthors() != null && !book.getMetadata().getAuthors().isEmpty()) {
                    result.author = book.getMetadata().getAuthors().get(0).getFirstname() + " " + book.getMetadata().getAuthors().get(0).getLastname();
                }

                result.pageCount = book.getSpine().size(); // Кількість розділів у spine, приблизно як сторінки
            }
            catch (Exception e) {
                // Якщо хочеш, можна логувати тут
                throw new RuntimeException(e); // винесемо в exceptionally
            }

            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null; // або new BookInfo() з помилковим статусом
        });



        CompletableFuture<BookInfo> getBookPreviewFuture = CompletableFuture.supplyAsync(()->{
            // 2. Рендер першої сторінки
            BookInfo result = new BookInfo();
            // Для прев’ю створимо заглушку (текстовий Bitmap, бо рендерити epub сторінки складно)
            Bitmap bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            canvas.drawText(result.title, 10, 50, paint);
            canvas.drawText(result.author, 10, 110, paint);

            // Збереження прев’ю
            File previewDir = new File(context.getFilesDir(), "previews");
            if (!previewDir.exists()) previewDir.mkdirs();

            String baseName = bookFile.getName();
            if (baseName.toLowerCase().endsWith(".epub")) {
                baseName = baseName.substring(0, baseName.length() - 5);
            }
            File previewFile = new File(previewDir, baseName + "_preview.png");

            try (FileOutputStream out = new FileOutputStream(previewFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
            }catch (Exception e) {
                // Якщо хочеш, можна логувати тут
                throw new RuntimeException(e); // винесемо в exceptionally
            }

            result.filePath = bookFile.getAbsolutePath();
            result.previewPath = previewFile.getAbsolutePath();

            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null; // або new BookInfo() з помилковим статусом
        });

        return CompletableFuture.allOf(getBookInfoFuture,getBookPreviewFuture).thenApply(v->{
            BookInfo bookWithInfo = getBookInfoFuture.join();
            BookInfo bookWithPaths = getBookPreviewFuture.join();
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
    public CompletableFuture<Bitmap> getPreviewAsync( File bookFile, int pageIndex, int height, int wight) throws IOException {
        return CompletableFuture.supplyAsync(()->{
            // Для прев’ю створимо заглушку (текстовий Bitmap, бо рендерити epub сторінки складно)
            Bitmap bitmap = Bitmap.createBitmap(wight, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            canvas.drawText("Title", 10, 50, paint);
            canvas.drawText("Author", 10, 110, paint);
            return bitmap;
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null; // або new BookInfo() з помилковим статусом
        });
    }

    @Override
    public CompletableFuture<BookInfo> processFileAsync(Uri bookUri) throws IOException {
        File bookFile = new File(FileHelper.getPath(context,bookUri));
        return processFileAsync(bookFile);
    }
}
