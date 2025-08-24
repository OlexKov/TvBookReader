package com.example.bookreader.utility.bookutils;

import static com.example.bookreader.constants.Constants.PREVIEWS_DIR;
import static com.example.bookreader.utility.ToastHelper.createToast;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.example.bookreader.R;
import com.example.bookreader.data.database.dto.BookDto;
import com.example.bookreader.utility.ArchiveHelper.BooksArchiveReader;
import com.example.bookreader.utility.ImageHelper;
import com.example.bookreader.utility.bookutils.interfaces.IBookProcessor;
import com.example.bookreader.utility.FileHelper;
import com.example.bookreader.utility.bookutils.fb2parser.Annotation;
import com.example.bookreader.utility.bookutils.fb2parser.Binary;
import com.example.bookreader.utility.bookutils.fb2parser.Description;
import com.example.bookreader.utility.bookutils.fb2parser.Element;
import com.example.bookreader.utility.bookutils.fb2parser.FictionBook;
import com.example.bookreader.utility.bookutils.fb2parser.Image;
import com.example.bookreader.utility.bookutils.fb2parser.Person;
import com.example.bookreader.utility.bookutils.fb2parser.PublishInfo;
import com.example.bookreader.utility.bookutils.fb2parser.TitleInfo;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Fb2Processor implements IBookProcessor {
    private static final String TAG = "Fb2Processor";
    private final Context context;
    private final BooksArchiveReader reader = new BooksArchiveReader();

    public Fb2Processor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<String> savePreviewAsync(String bookPath,int height, int wight)  {
       if(BooksArchiveReader.isArchivePath(bookPath)){
           return  CompletableFuture.supplyAsync(() -> {
               try {
                   InputStream stream = reader.openFile(bookPath);
                   FictionBook fb = new FictionBook(stream);
                   return  savePreview(fb,height,wight);
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
        return  CompletableFuture.supplyAsync(() -> {
            try {
                FictionBook fb = new FictionBook(bookFile);
                return  savePreview(fb,height, wight);
            }
            catch (Exception e) {
                String message = "Error " + '"' + bookFile.getName() + '"' + " book preview saving";
                Log.e(TAG, message, e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<BookDto> getInfoAsync(File bookFile) {
        return CompletableFuture.supplyAsync(() -> {
            try  {
                FictionBook fb = new FictionBook(bookFile);
                return getBookInfo(fb,bookFile.getName());

            }
            catch (Exception e) {
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
                try {
                    InputStream stream = reader.openFile(bookPath);
                    FictionBook fb = new FictionBook(stream);
                    return  getBookInfo(fb,FileHelper.getFileName(bookPath));
                }
                catch (Exception e) {
                    String message = "Error reading" + '"' + FileHelper.getFileName(bookPath) + '"' + " book file";
                    createToast(context,message);
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
        File file = new File(FileHelper.getPath(context, bookUri));
        return getInfoAsync(file);
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int width) {
        return CompletableFuture.supplyAsync(() -> {
            try  {
                FictionBook fb = new FictionBook(bookFile);
                return extractCoverPreview(fb, width, height);
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
                    FictionBook fb = new FictionBook(stream);
                    return extractCoverPreview(fb, wight, height);
                }
                catch (Exception e) {
                    String message = "Error create preview " + '"' + FileHelper.getFileName(bookPath) + '"' + " book file";
                    Log.e(TAG, message, e);
                    return null;
                }
            });
        }
        else{
            return getPreviewAsync(new File(bookPath),pageIndex,height,wight);
        }
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

            Binary bin = binaries.get(coverId);
            if (bin == null) return null;

            String base64String = bin.getBinary();
            if (base64String.isEmpty()) return null;

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

    private String savePreview(FictionBook fb, int height, int wight){
        Bitmap cover = extractCoverPreview(fb, wight, height);
        if (cover == null) {
            Log.d(TAG, "Cover image not found");
            return null;
        }
        return ImageHelper.saveImage(context,PREVIEWS_DIR,cover,100, Bitmap.CompressFormat.PNG);
    }

    private BookDto getBookInfo(FictionBook fb,String bookName){
        BookDto result = new BookDto();
        result.title = fb.getTitle();

        result.author =  Optional.of(fb)
                .map(FictionBook::getDescription)
                .map(Description::getTitleInfo)
                .map(TitleInfo::getAuthors)
                .filter((ArrayList<Person> list) -> !list.isEmpty())
                .map(list -> list.get(0).getFullName())
                .orElse(context.getString(R.string.unknown));

        result.description = Optional.of(fb)
                .map(FictionBook::getDescription)
                .map(Description::getTitleInfo)
                .map(TitleInfo::getAnnotation)
                .map(Annotation::getElements)
                .filter((ArrayList<Element> list) -> !list.isEmpty())
                .map(list -> list.stream()
                        .map(Element::getText)
                        .collect(Collectors.joining("")))
                .orElse(context.getString(R.string.unknown));

        result.year = Optional.of(fb)
                .map(FictionBook::getDescription)
                .map(Description::getPublishInfo)
                .map(PublishInfo::getYear)
                .orElse(context.getString(R.string.unknown));

        if (result.title == null || result.title.trim().isEmpty()) {
            result.title = bookName.substring(0,bookName.length() - 4);
        }
        return result;
    }
}