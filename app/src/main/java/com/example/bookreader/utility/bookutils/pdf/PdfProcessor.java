package com.example.bookreader.utility.bookutils.pdf;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;


import com.example.bookreader.R;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
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
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;


public class PdfProcessor implements IBookProcessor {
    private static final String TAG = "PdfProcessor";
    private final Context context;
    private final BooksArchiveReader reader = new BooksArchiveReader();

    public PdfProcessor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<BookPaths> savePreviewAsync(String bookPath, int height, int wight) throws IOException {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream stream = reader.openFile(bookPath);
                    return savePreview(stream,FileHelper.getFileName(bookPath), bookPath, height,wight);
                }
                catch (Exception e) {
                    Log.e(TAG, "Error reading book info", e);
                    return null;
                }
            });
        }
        else{
            return savePreviewAsync (new File(bookPath), height, wight);
        }
    }

    @Override
    public CompletableFuture<BookPaths> savePreviewAsync(File bookFile,int height, int wight) throws IOException{
        return   CompletableFuture.supplyAsync(()->{
            // 1. Рендер першої сторінки
            try (FileInputStream inputStream = new FileInputStream(bookFile)) {
                return savePreview(inputStream,bookFile.getName(), bookFile.getAbsolutePath(), height,wight);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public CompletableFuture<BookInfo> getInfoAsync(File bookFile) throws IOException {

        return  CompletableFuture.supplyAsync(()->{
            try (FileInputStream stream = new FileInputStream(bookFile)) {
               return getInfo(stream,bookFile.getName());
            }
            catch (Exception e) {
                 throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(String bookPath) throws IOException {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream stream = reader.openFile(bookPath);
                    return  getInfo((FileInputStream) stream,FileHelper.getFileName(bookPath));
                }
                catch (Exception e) {
                    Log.e(TAG, "Error reading book info", e);
                    return null;
                }
            });
        }
        else{
            return getInfoAsync(new File(bookPath));
        }
    }

    @Override
    public  CompletableFuture<Bitmap> getPreviewAsync(File bookFile,int pageIndex, int height, int width) throws IOException {
        return CompletableFuture.supplyAsync(()->{
            try(FileInputStream stream  = new FileInputStream(bookFile)){
                return createPreview(stream, pageIndex, height, width);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(String bookPath, int pageIndex, int height, int wight) throws IOException {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream stream = reader.openFile(bookPath);
                    return createPreview((FileInputStream) stream, pageIndex, height,wight);
                }
                catch (Exception e) {
                    Log.e(TAG, "Error reading book info", e);
                    return null;
                }
            });
        }
        else{
            return getPreviewAsync(new File(bookPath), pageIndex,  height, wight);
        }
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync( Uri bookUri) throws IOException {
        File file = new File(FileHelper.getPath(context, bookUri));
        return getInfoAsync(file);
    }

    private Bitmap createPreview(FileInputStream inputStream, int pageIndex, int height, int width) throws IOException {
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        PdfDocument document = pdfiumCore.newDocument(ParcelFileDescriptor.dup(inputStream.getFD()));
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
        if (document != null) {
            pdfiumCore.closeDocument(document);
        }
        return result;
    }

    private BookPaths savePreview(InputStream inputStream,String bookName,String bookPath,int height,int wight) throws IOException {
        Bitmap bitmap = createPreview((FileInputStream) inputStream, 0, height, wight);
        // 2. Збереження прев’ю
        File previewDir = new File(context.getFilesDir(), "previews");
        if (!previewDir.exists()) previewDir.mkdirs();


        if (bookName.toLowerCase().endsWith(".pdf")) {
            bookName = bookName.substring(0, bookName.length() - 4);
        }

        File previewFile = new File(previewDir, bookName + "_preview.png");

        try (FileOutputStream out = new FileOutputStream(previewFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 4. Повернення даних
        BookPaths result = new BookPaths();
        result.filePath = bookPath;
        result.previewPath = previewFile.getAbsolutePath();
        return result;
    }

    private BookInfo getInfo(FileInputStream stream,String bookName) throws IOException {
        BookInfo result = new BookInfo();
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        PdfDocument document = pdfiumCore.newDocument(ParcelFileDescriptor.dup(stream.getFD()));
        PdfDocument.Meta meta = pdfiumCore.getDocumentMeta(document);
        result.title = meta.getTitle();
        result.author = meta.getAuthor();
        result.description = meta.getSubject();
        result.pageCount = pdfiumCore.getPageCount(document);

        if (result.title == null || result.title.trim().isEmpty()) {
            result.title = bookName.substring(0,bookName.length() - 4);
        }
        if (result.author == null || result.author.trim().isEmpty()) {
            result.author = context.getString(R.string.unknown);
        }
        if (result.description == null || result.description.trim().isEmpty()) {
            result.description = context.getString(R.string.unknown);
        }
        if (result.year == null || result.year.trim().isEmpty()) {
            result.year = context.getString(R.string.unknown);
        }
        return result;
    }
}
