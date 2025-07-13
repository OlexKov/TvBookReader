package com.example.bookreader.utility.pdf;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;


import com.example.bookreader.customclassses.BookInfo;
import com.example.bookreader.interfaces.BookProcessor;
import com.example.bookreader.utility.FileHelper;
import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public class PdfProcessor implements BookProcessor {

    @Override
    public CompletableFuture<BookInfo> processFileAsync(Context context, File bookFile) throws IOException {
        long start = System.currentTimeMillis();
        CompletableFuture<BookInfo> getBookInfoFuture = CompletableFuture.supplyAsync(()->{
            Log.d("TIMER", "Start getBookInfo at " + (System.currentTimeMillis() - start));
            // 1. Метадані PDF
            BookInfo result = new BookInfo();
            try (PDDocument document = PDDocument.load(bookFile,  null, null, null, MemoryUsageSetting.setupMainMemoryOnly())) {
                PDDocumentInformation info = document.getDocumentInformation();
                result.title = info.getTitle();
                result.author = info.getAuthor();
                result.pageCount = document.getNumberOfPages();
            }
            catch (Exception e) {
                // Якщо хочеш, можна логувати тут
                throw new RuntimeException(e); // винесемо в exceptionally
            }
            Log.d("TIMER", "End getBookInfo at " + (System.currentTimeMillis() - start));
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null; // або new BookInfo() з помилковим статусом
        });


        CompletableFuture<BookInfo> getBookPreviewFuture = CompletableFuture.supplyAsync(()->{
            Log.d("TIMER", "Start getBookPreview at " + (System.currentTimeMillis() - start));
            // 2. Рендер першої сторінки
            BookInfo result = new BookInfo();
            Bitmap bitmap = createPreview(bookFile, 0, 300, 400);

            // 3. Збереження прев’ю
            File previewDir = new File(context.getFilesDir(), "previews");
            if (!previewDir.exists()) previewDir.mkdirs();

            String baseName = bookFile.getName();
            if (baseName.toLowerCase().endsWith(".pdf")) {
                baseName = baseName.substring(0, baseName.length() - 4);
            }

            File previewFile = new File(previewDir, baseName + "_preview.png");

            try (FileOutputStream out = new FileOutputStream(previewFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
            }catch (Exception e) {
                // Якщо хочеш, можна логувати тут
                throw new RuntimeException(e); // винесемо в exceptionally
            }

            // 4. Повернення даних
            result.filePath = bookFile.getAbsolutePath();
            result.previewPath = previewFile.getAbsolutePath();
            Log.d("TIMER", "End getBookPreview at " + (System.currentTimeMillis() - start));
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null; // або new BookInfo() з помилковим статусом
        });


        return CompletableFuture.allOf(getBookInfoFuture, getBookPreviewFuture).thenApply(v -> {
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
    public  CompletableFuture<Bitmap> getPreviewAsync(File bookFile,int pageIndex, int height, int width) throws IOException {
        return CompletableFuture.supplyAsync(()->{
            return createPreview(bookFile, pageIndex, height, width);
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null; // або new BookInfo() з помилковим статусом
        });
    }


    @Override
    public CompletableFuture<BookInfo> processFileAsync(Context context, Uri bookUri) throws IOException {
        File file = new File(FileHelper.getPath(context, bookUri));
        return processFileAsync(context, file);
    }

    private Bitmap createPreview(File bookFile,int pageIndex, int height, int width){
        Bitmap bitmap;
        try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(bookFile, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(fd)) {
            PdfRenderer.Page page = renderer.openPage(pageIndex);
            // Отримай реальний розмір сторінки
            int pageWidth = page.getWidth();
            int pageHeight = page.getHeight();
            // Обрахунок масштабу по висоті або ширині (щоб помістити в бітмап з бажаними пропорціями)
            float scale = Math.min((float) width / pageWidth, (float) height / pageHeight);
            int bmpWidth = (int) (pageWidth * scale);
            int bmpHeight = (int) (pageHeight * scale);
            bitmap = Bitmap.createBitmap(bmpWidth,  bmpHeight, Bitmap.Config.ARGB_8888);
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
        } catch (Exception e) {
            // Якщо хочеш, можна логувати тут
            throw new RuntimeException(e); // винесемо в exceptionally
        }
        return bitmap;
    }
}
