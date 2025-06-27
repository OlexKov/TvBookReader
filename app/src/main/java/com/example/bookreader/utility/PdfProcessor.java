package com.example.bookreader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.example.bookreader.customclassses.BookInfo;
import com.example.bookreader.interfaces.BookProcessor;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfProcessor implements BookProcessor {
    public BookInfo processFile(Context context, File pdfFile) throws IOException {
        String title;
        String author;
        int pageCount;
        // 1. Метадані PDF
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDDocumentInformation info = document.getDocumentInformation();
            title = info.getTitle();
            author = info.getAuthor();
            pageCount = document.getNumberOfPages();
        }

        if (title == null || title.trim().isEmpty()) {
            title = pdfFile.getName();
        }
        if (author == null || author.trim().isEmpty()) {
            author = "Unknown";
        }

        // 2. Рендер першої сторінки
        Bitmap bitmap;
        try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(fd)) {

            PdfRenderer.Page page = renderer.openPage(0);

            bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
        }

        // 3. Збереження прев’ю
        File previewDir = new File(context.getFilesDir(), "previews");
        if (!previewDir.exists()) previewDir.mkdirs();

        String baseName = pdfFile.getName();
        if (baseName.toLowerCase().endsWith(".pdf")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        File previewFile = new File(previewDir, baseName + "_preview.png");

        try (FileOutputStream out = new FileOutputStream(previewFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        }

        // 4. Повернення даних
        BookInfo result = new BookInfo();
        result.title = title;
        result.author = author;
        result.pageCount = pageCount;
        result.filePath = pdfFile.getAbsolutePath();
        result.previewPath = previewFile.getAbsolutePath();

        return result;
    }
}
