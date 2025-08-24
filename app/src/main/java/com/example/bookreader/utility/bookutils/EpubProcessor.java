package com.example.bookreader.utility.bookutils;

import static com.example.bookreader.constants.Constants.PREVIEWS_DIR;
import static com.example.bookreader.utility.ToastHelper.createToast;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.ImageHelper;
import com.example.bookreader.utility.bookutils.interfaces.IBookProcessor;
import com.example.bookreader.utility.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;


public class EpubProcessor implements IBookProcessor {

    private final Context context;
    private static final String TAG = "EpubProcessor";
    private final BooksArchiveReader reader = new BooksArchiveReader();

    public EpubProcessor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<String> savePreviewAsync(String bookPath,int height, int wight) {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try {
                    InputStream stream = reader.openFile(bookPath);
                    return  savePreview(stream,300,400);
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
    public CompletableFuture<String> savePreviewAsync(File bookFile,int height, int wight)  {
        return  CompletableFuture.supplyAsync(() -> {
            try(InputStream stream = new FileInputStream(bookFile)) {
               return savePreview(stream,300,400);
            } catch (Exception e) {
                String message = "Error " + '"' + bookFile.getName() + '"' + " book preview saving";
                Log.e(TAG, message, e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<BookDto> getInfoAsync(File bookFile) {
        return CompletableFuture.supplyAsync(() -> {
            try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(bookFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                InputStream stream = new FileInputStream(fd.getFileDescriptor());
                BookDto result = getInfo(stream, bookFile.getName());
                result.fileSize = bookFile.length();
                result.filePath = bookFile.getAbsolutePath();
                return result;
            } catch (Exception e) {
                String message = "Error reading" + '"' + bookFile.getName() + '"' + " book file";
                Log.e(TAG, message, e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<BookDto> getInfoAsync(String bookPath)  {
        if(BooksArchiveReader.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try(InputStream stream = reader.openFile(bookPath)) {
                    return  getInfo(stream, FileHelper.getFileName(bookPath));
                }
                catch (Exception e) {
                    String message = "Error reading" + '"' + FileHelper.getFileName(bookPath) + '"' + " book file";
                    Log.e(TAG, message, e);
                    return null;
                }
            });
        }
        else{
            return getInfoAsync (new File(bookPath));
        }
    }

    @Override
    public CompletableFuture<BookDto> getInfoAsync(Uri bookUri) {
        File bookFile = new File(FileHelper.getPath(context, bookUri));
        return getInfoAsync(bookFile);
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int width) {
        return CompletableFuture.supplyAsync(() -> {
            try(InputStream stream = new FileInputStream(bookFile)) {
                 return extractCoverPreview(stream, width, height);
            } catch (Exception e) {
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
                    return extractCoverPreview(stream, wight, height);
                }
                catch (Exception e) {
                    String message = "Error create preview " + '"' + FileHelper.getFileName(bookPath) + '"' + " book file";
                    Log.e(TAG, message, e);
                    return null;
                }
            });
        }
        else{
            return getPreviewAsync(new File(bookPath), pageIndex, height, wight);
        }
    }

    private Bitmap extractCoverPreview(InputStream stream, int width, int height) throws IOException {
        try {
            EpubReader epubReader = new EpubReader();
            Book book = epubReader.readEpub(stream);
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

    private String savePreview(InputStream stream,int wight, int height) throws Exception {
        Bitmap cover = extractCoverPreview(stream, wight, height);
        if (cover == null) {
            Log.d("EpubProcessor", "Cover not found");
            return null;
        }

        return ImageHelper.saveImage(context,PREVIEWS_DIR,cover,100,Bitmap.CompressFormat.PNG);
    }

    private BookDto getInfo(InputStream stream,String bookName) throws Exception {
        EpubReader epubReader = new EpubReader();
        Book book = epubReader.readEpub(stream);
        if (book.getSpine() == null || book.getSpine().getSpineReferences().isEmpty()) {
            throw new Exception("Error epub format");
        }
        BookDto result = new BookDto();
        result.title = book.getTitle();

        result.author = Optional.of(book)
                .map(Book::getMetadata)
                .map(Metadata::getAuthors)
                .filter((List<Author> list) -> !list.isEmpty())
                .map(list -> list.get(0).getFirstname() + " " + list.get(0).getLastname())
                .orElse(context.getString(R.string.unknown));

        result.year = Optional.of(book)
                .map(Book::getMetadata)
                .map(Metadata::getDates)
                .filter((List<Date> list) -> !list.isEmpty())
                .map(list -> list.get(0).getValue())
                .orElse(context.getString(R.string.unknown));

        result.description = Optional.of(book)
                .map(Book::getMetadata)
                .map(Metadata::getDescriptions)
                .filter((List<String> list) -> !list.isEmpty())
                .map(list -> String.join("",list))
                .orElse(context.getString(R.string.unknown));

        if (result.title == null || result.title.trim().isEmpty()) {
            result.title = bookName.substring(0,bookName.length() - 5);
        }
        return result;
    }
}