package com.example.bookreader.utility.bookutils.pdf;
import static com.example.bookreader.constants.Constants.PREVIEWS_DIR;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.ImageHelper;
import com.example.bookreader.utility.bookutils.interfaces.IBookProcessor;
import com.example.bookreader.utility.FileHelper;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileInputStream;
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
    public CompletableFuture<String> savePreviewAsync(String bookPath, int height, int wight) {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream stream = reader.openFile(bookPath);
                    return savePreview(stream, height,wight);
                }
                catch (Exception e) {
                    String message = "Error " + '"' + FileHelper.getFileName(bookPath) + '"' + " book preview saving";
                    Log.e(TAG, message, e);
                    return null;
                }
            });
        }
        else{
            return savePreviewAsync (new File(bookPath), height, wight);
        }
    }

    @Override
    public CompletableFuture<String> savePreviewAsync(File bookFile,int height, int wight) {
        return   CompletableFuture.supplyAsync(()->{
            // 1. Рендер першої сторінки
            try (FileInputStream inputStream = new FileInputStream(bookFile)) {
                return savePreview(inputStream,height,wight);
            } catch (IOException e) {
                String message = "Error " + '"' + bookFile.getName() + '"' + " book preview saving";
                Log.e(TAG, message, e);
                return null;
            }
        });
    }


    @Override
    public CompletableFuture<BookDto> getInfoAsync(File bookFile){
        return  CompletableFuture.supplyAsync(()->{
            try (FileInputStream stream = new FileInputStream(bookFile)) {
               return getInfo(stream,bookFile.getName());
            }
            catch (Exception e) {
                String message = "Error reading " + '"' + bookFile.getName() + '"' + " book file";
                Log.e(TAG, message, e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<BookDto> getInfoAsync(String bookPath)  {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream stream = reader.openFile(bookPath);
                    return  getInfo((FileInputStream) stream,FileHelper.getFileName(bookPath));
                }
                catch (Exception e) {
                    String message = "Error reading " + '"' + FileHelper.getFileName(bookPath) + '"' + " book file";
                    Log.e(TAG, message, e);
                    return null;
                }
            });
        }
        else{
            return getInfoAsync(new File(bookPath));
        }
    }

    @Override
    public  CompletableFuture<Bitmap> getPreviewAsync(File bookFile,int pageIndex, int height, int width) {
        return CompletableFuture.supplyAsync(()->{
            try(FileInputStream stream  = new FileInputStream(bookFile)){
                return createPreview(stream, pageIndex, height, width);
            }
            catch (IOException e) {
                String message = "Error create preview " + '"' + bookFile.getName() + '"' + " book file";
                Log.e(TAG, message, e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(String bookPath, int pageIndex, int height, int wight) {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream stream = reader.openFile(bookPath);
                    return createPreview((FileInputStream) stream, pageIndex, height,wight);
                }
                catch (Exception e) {
                    String message = "Error create preview " + '"' + FileHelper.getFileName(bookPath) + '"' + " book file";
                    Log.e(TAG, message, e);
                    return null;
                }
            });
        }
        else{
            return getPreviewAsync(new File(bookPath), pageIndex,  height, wight);
        }
    }

    @Override
    public CompletableFuture<BookDto> getInfoAsync( Uri bookUri)  {
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

    private String savePreview(InputStream inputStream,int height,int wight) throws IOException {
        Bitmap bitmap = createPreview((FileInputStream) inputStream, 0, height, wight);
        return ImageHelper.saveImage(context,PREVIEWS_DIR,bitmap,100,Bitmap.CompressFormat.PNG);
    }

    private BookDto getInfo(FileInputStream stream, String bookName) throws IOException {
        BookDto result = new BookDto();
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
