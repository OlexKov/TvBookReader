package com.example.bookreader.utility.bookutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.bookreader.utility.ArchiveHelper.ArchivePathHelper;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.bookutils.fb2parser.FictionBook;
import com.example.bookreader.utility.bookutils.interfaces.IBookProcessor;
import com.example.bookreader.utility.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
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

    public EpubProcessor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<BookPaths> savePreviewAsync(String bookPath,int height, int wight) throws IOException {
        if(ArchivePathHelper.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try (BooksArchiveReader reader = new BooksArchiveReader(ArchivePathHelper.archivePath(bookPath))){
                    InputStream stream = reader.openFile(ArchivePathHelper.internalPath(bookPath));
                    return  savePreview(stream,FileHelper.getFileName(bookPath),bookPath,300,400);
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
    public CompletableFuture<BookPaths> savePreviewAsync(File bookFile,int height, int wight) throws IOException {
        return  CompletableFuture.supplyAsync(() -> {
            try(InputStream stream = new FileInputStream(bookFile)) {
               return savePreview(stream,bookFile.getName(),bookFile.getAbsolutePath(),300,400);
            } catch (Exception e) {
                Log.e("EpubProcessor", "Error generating preview", e);
                throw new RuntimeException(e);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(File bookFile) throws IOException {
       return   CompletableFuture.supplyAsync(() -> {
            try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(bookFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                InputStream stream = new FileInputStream(fd.getFileDescriptor());
                return getInfo(stream, bookFile.getName());
            } catch (Exception e) {
                Log.e("EpubProcessor", "Error reading book info", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(String bookPath) throws IOException {
        if(ArchivePathHelper.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try (BooksArchiveReader reader = new BooksArchiveReader(ArchivePathHelper.archivePath(bookPath))){
                    InputStream stream = reader.openFile(ArchivePathHelper.internalPath(bookPath));
                    return  getInfo(stream, FileHelper.getFileName(bookPath));
                }
                catch (Exception e) {
                    Log.e(TAG, "Error reading book info", e);
                    return null;
                }
            });
        }
        else{
            return getInfoAsync (new File(bookPath));
        }
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(Uri bookUri) throws IOException {
        File bookFile = new File(FileHelper.getPath(context, bookUri));
        return getInfoAsync(bookFile);
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int width) {
        return CompletableFuture.supplyAsync(() -> {
            try(InputStream stream = new FileInputStream(bookFile)) {
                 return extractCoverPreview(stream, width, height);
            } catch (Exception e) {
                Log.e("EpubProcessor", "Error in getPreviewAsync", e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(String bookPath, int pageIndex, int height, int wight) throws IOException {
        if(ArchivePathHelper.isArchivePath(bookPath)){
            return  CompletableFuture.supplyAsync(() -> {
                try (BooksArchiveReader reader = new BooksArchiveReader(ArchivePathHelper.archivePath(bookPath))){
                    InputStream stream = reader.openFile(ArchivePathHelper.internalPath(bookPath));
                    return extractCoverPreview(stream, wight, height);
                }
                catch (Exception e) {
                    Log.e(TAG, "Error reading book info", e);
                    return null;
                }
            });
        }
        else{
            return getPreviewAsync(new File(bookPath), pageIndex, height, wight);
        }
    }

    private Bitmap extractCoverPreview(InputStream stream, int width, int height) {
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

    private BookPaths savePreview(InputStream stream,String bookName,String bookPath,int wight, int height) throws IOException {
        Bitmap cover = extractCoverPreview(stream, wight, height);
        if (cover == null) {
            Log.d("EpubProcessor", "Cover not found");
            return null;
        }

        // Збереження прев’ю
        File previewDir = new File(context.getFilesDir(), "previews");
        if (!previewDir.exists()) previewDir.mkdirs();

        if ( bookName.toLowerCase().endsWith(".epub")) {
            bookName = bookName.substring(0, bookName.length() - 5);
        }

        File previewFile = new File(previewDir,  bookName + "_preview.png");
        try (FileOutputStream out = new FileOutputStream(previewFile)) {
            cover.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BookPaths result = new BookPaths();
        result.filePath = bookPath;
        result.previewPath = previewFile.getAbsolutePath();
        return result;
    }

    private BookInfo getInfo(InputStream stream,String bookName) throws IOException {
        BookInfo result = new BookInfo();
        EpubReader epubReader = new EpubReader();
        Book book = epubReader.readEpub(stream);

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
            result.title = bookName.substring(0,bookName.length() - 4);
        }
        return result;
    }
}