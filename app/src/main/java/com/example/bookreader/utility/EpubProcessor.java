package com.example.bookreader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.bookreader.customclassses.BookInfo;
import com.example.bookreader.interfaces.BookProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nl.siegmann.epublib.epub.EpubReader;
import nl. siegmann. epublib. domain. Book;

public class EpubProcessor implements BookProcessor {

    @Override
    public BookInfo processFile(Context context, File epubFile) throws IOException {
        String title;
        String author = null;
        int pageCount = 0;  // EPUB не має сторінок, можна рахувати розділи

        // Читаємо EPUB метадані
        try (FileInputStream fis = new FileInputStream(epubFile)) {
            EpubReader epubReader = new EpubReader();
            Book book = epubReader.readEpub(fis);

            title = book.getTitle();
            if (book.getMetadata().getAuthors() != null && !book.getMetadata().getAuthors().isEmpty()) {
                author = book.getMetadata().getAuthors().get(0).getFirstname() + " " + book.getMetadata().getAuthors().get(0).getLastname();
            }

            pageCount = book.getSpine().size(); // Кількість розділів у spine, приблизно як сторінки
        }

        if (title == null || title.trim().isEmpty()) {
            title = epubFile.getName();
        }
        if (author == null || author.trim().isEmpty()) {
            author = "Unknown";
        }

        // Для прев’ю створимо заглушку (текстовий Bitmap, бо рендерити epub сторінки складно)
        Bitmap bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        canvas.drawText(title, 10, 50, paint);
        canvas.drawText(author, 10, 110, paint);

        // Збереження прев’ю
        File previewDir = new File(context.getFilesDir(), "previews");
        if (!previewDir.exists()) previewDir.mkdirs();

        String baseName = epubFile.getName();
        if (baseName.toLowerCase().endsWith(".epub")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }
        File previewFile = new File(previewDir, baseName + "_preview.png");

        try (FileOutputStream out = new FileOutputStream(previewFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        }

        BookInfo result = new BookInfo();
        result.title = title;
        result.author = author;
        result.pageCount = pageCount;
        result.filePath = epubFile.getAbsolutePath();
        result.previewPath = previewFile.getAbsolutePath();

        return result;
    }
}
