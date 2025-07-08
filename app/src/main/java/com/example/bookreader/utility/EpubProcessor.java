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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import nl.siegmann.epublib.epub.EpubReader;
import nl. siegmann. epublib. domain. Book;

public class EpubProcessor implements BookProcessor {

    @Override
    public BookInfo processFile(Context context, File bookFile) throws IOException{
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


        if (result.title == null || result.title.trim().isEmpty()) {
            result.title = bookFile.getName();
        }
        if (result.author == null || result.author.trim().isEmpty()) {
            result.author = "Unknown";
        }

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
        }

        result.filePath = bookFile.getAbsolutePath();
        result.previewPath = previewFile.getAbsolutePath();

        return result;
    }

    @Override
    public BookInfo processFile(Context context, Uri bookUri) throws IOException {
        File bookFile = new File(UriHelper.getPath(context,bookUri));
        return processFile(context,bookFile);
    }
}
