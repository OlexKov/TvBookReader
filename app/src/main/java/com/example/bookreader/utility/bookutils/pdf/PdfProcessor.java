package com.example.bookreader.utility.bookutils.pdf;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;


import com.example.bookreader.utility.bookutils.BookInfo;
import com.example.bookreader.utility.bookutils.BookPaths;
import com.example.bookreader.utility.bookutils.interfaces.IBookProcessor;
import com.example.bookreader.utility.FileHelper;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public class PdfProcessor implements IBookProcessor {
    private final Context context;

    public PdfProcessor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<BookPaths> savePreviewAsync(File bookFile) throws IOException{
        return   CompletableFuture.supplyAsync(()->{
            // 1. Рендер першої сторінки
            Bitmap bitmap = createPreview( bookFile, 0, 300, 400);

            // 2. Збереження прев’ю
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
                 throw new RuntimeException(e);
            }

            // 4. Повернення даних
            BookPaths result = new BookPaths();
            result.filePath = bookFile.getAbsolutePath();
            result.previewPath = previewFile.getAbsolutePath();
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null;
        });
    }


    @Override
    public CompletableFuture<BookInfo> getInfoAsync(File bookFile) throws IOException {

        return  CompletableFuture.supplyAsync(()->{
             // 1. Метадані PDF
            BookInfo result = new BookInfo();
            try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(bookFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                PdfiumCore pdfiumCore = new PdfiumCore(context);
                PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
                PdfDocument.Meta meta = pdfiumCore.getDocumentMeta(pdfDocument);
                result.title = meta.getTitle();
                result.author = meta.getAuthor();
                result.description = meta.getSubject();
                result.pageCount = pdfiumCore.getPageCount(pdfDocument);

                if (result.title == null || result.title.trim().isEmpty()) {
                    result.title = bookFile.getName().substring(0,bookFile.getName().length() - 4);
                }
                if (result.author == null || result.author.trim().isEmpty()) {
                    result.author = "Unknown";
                }
                if (result.description == null || result.description.trim().isEmpty()) {
                    result.description = "Unknown";
                }
                if (result.year == null || result.year.trim().isEmpty()) {
                    result.year = "Unknown";
                }
            }
            catch (Exception e) {
                 throw new RuntimeException(e);
            }
           return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace(); // або Log.e(...)
            return null;
        });

    }

    @Override
    public  CompletableFuture<Bitmap> getPreviewAsync(File bookFile,int pageIndex, int height, int width) throws IOException {
        return CompletableFuture.supplyAsync(()->{
            return createPreview(bookFile, pageIndex, height, width);
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync( Uri bookUri) throws IOException {
        File file = new File(FileHelper.getPath(context, bookUri));
        return getInfoAsync(file);
    }

    private Bitmap createPreview(File bookFile, int pageIndex, int height, int width) {
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        PdfDocument document = null;

        try (FileInputStream inputStream = new FileInputStream(bookFile)) {
            document = pdfiumCore.newDocument(ParcelFileDescriptor.dup(inputStream.getFD()));
            pdfiumCore.openPage(document, pageIndex);

            int pageWidth = pdfiumCore.getPageWidthPoint(document, pageIndex);
            int pageHeight = pdfiumCore.getPageHeightPoint(document, pageIndex);

            // Створюємо зображення точного розміру без збереження пропорцій
            Bitmap temp = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888);
            temp.eraseColor(Color.WHITE);

            pdfiumCore.renderPageBitmap(document, temp, pageIndex, 0, 0, pageWidth, pageHeight);

            // Масштабуємо до потрібного розміру без збереження пропорцій
            Bitmap result = Bitmap.createScaledBitmap(temp, width, height, true);

            temp.recycle(); // звільняємо пам’ять
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error rendering PDF preview", e);
        } finally {
            if (document != null) {
                pdfiumCore.closeDocument(document);
            }
        }
    }
}
