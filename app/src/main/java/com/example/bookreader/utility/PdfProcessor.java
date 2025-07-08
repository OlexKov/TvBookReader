package com.example.bookreader.utility;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.example.bookreader.customclassses.BookInfo;
import com.example.bookreader.interfaces.BookProcessor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public class PdfProcessor implements BookProcessor {

    @Override
    public BookInfo processFile(Context context, File bookFile) throws IOException {
        BookInfo result = new BookInfo();

        // 1. Метадані PDF
        RandomAccessRead rar = new RandomAccessReadBufferedFile(bookFile);
        try (PDDocument document = Loader.loadPDF(rar)) {
            PDDocumentInformation info = document.getDocumentInformation();
            result.title = info.getTitle();
            result.author = info.getAuthor();
            result.pageCount = document.getNumberOfPages();
        }

        // 2. Рендер першої сторінки
        Bitmap bitmap;

        try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(bookFile, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(fd);) {

            PdfRenderer.Page page = renderer.openPage(0);
            int previewWidth = page.getWidth() / 2;
            int previewHeight = page.getHeight() / 2;
            bitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
            Matrix matrix = new Matrix();
            matrix.setScale(0.5f, 0.5f);
            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
        }
        if (result.title == null || result.title.trim().isEmpty()) {
            result.title = bookFile.getName();
        }
        if (result.author == null || result.author.trim().isEmpty()) {
            result.author = "Unknown";
        }

        // 3. Збереження прев’ю
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
        }

        // 4. Повернення даних
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
