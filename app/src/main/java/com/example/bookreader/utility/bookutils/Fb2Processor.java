package com.example.bookreader.utility.bookutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Fb2Processor implements IBookProcessor {
    private static final String TAG = "Fb2Processor";
    private final Context context;

    public Fb2Processor(Context context){
        this.context = context;
    }

    @Override
    public CompletableFuture<BookPaths> savePreviewAsync(File bookFile) {
        return  CompletableFuture.supplyAsync(() -> {
            BookPaths result = new BookPaths();
            try {
                FictionBook fb = new FictionBook(bookFile);
                Bitmap cover = extractCoverPreview(fb, 300, 400);
                if (cover == null) {
                    Log.d(TAG, "Cover image not found");
                    return null;
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
                }catch (Exception e) {
                    Log.e(TAG, "Error reading book info", e);
                    throw new RuntimeException(e);
                }
                result.filePath = bookFile.getAbsolutePath();
                result.previewPath = previewFile.getAbsolutePath();

            }
            catch (Exception e) {
                Log.e(TAG, "Error reading book info", e);
                throw new RuntimeException(e);
            }
           return result;

        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(File bookFile) {

        return CompletableFuture.supplyAsync(() -> {
            BookInfo result = new BookInfo();
            try  {
                FictionBook fb = new FictionBook(bookFile);
                result.title = fb.getTitle();

                result.author =  Optional.of(fb)
                        .map(FictionBook::getDescription)
                        .map(Description::getTitleInfo)
                        .map(TitleInfo::getAuthors)
                        .filter((ArrayList<Person> list) -> !list.isEmpty())
                        .map(list -> list.get(0).getFullName())
                        .orElse("Unknown");

                result.description = Optional.of(fb)
                        .map(FictionBook::getDescription)
                        .map(Description::getTitleInfo)
                        .map(TitleInfo::getAnnotation)
                        .map(Annotation::getElements)
                        .filter((ArrayList<Element> list) -> !list.isEmpty())
                        .map(list -> list.get(0).getText())
                        .orElse("Unknown");

                result.year = Optional.of(fb)
                        .map(FictionBook::getDescription)
                        .map(Description::getPublishInfo)
                        .map(PublishInfo::getYear)
                        .orElse("Unknown");

                if (result.title == null || result.title.trim().isEmpty()) {
                    result.title = bookFile.getName().substring(0,bookFile.getName().length() - 4);
                }

            }
            catch (Exception e) {
                Log.e(TAG, "Error reading book info", e);
                throw new RuntimeException(e);
            }

            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

    }

    @Override
    public CompletableFuture<BookInfo> getInfoAsync(Uri bookUri) throws java.io.IOException {
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
}