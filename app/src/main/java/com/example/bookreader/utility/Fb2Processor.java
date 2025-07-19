package com.example.bookreader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.bookreader.customclassses.BookInfo;
import com.example.bookreader.interfaces.BookProcessor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CompletableFuture;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Fb2Processor implements BookProcessor {
    private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
    @Override
    public CompletableFuture<BookInfo> processFileAsync(Context context, File bookFile) {
        long start = System.currentTimeMillis();


        CompletableFuture<BookInfo> getBookInfoFuture = CompletableFuture.supplyAsync(() -> {
            Log.d("TIMER", "Start getBookInfo at " + (System.currentTimeMillis() - start));
            BookInfo result = new BookInfo();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(bookFile);

                result.title = getTagText(doc, "book-title");
                result.author = getAuthorName(doc);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Log.d("TIMER", "End getBookInfo at " + (System.currentTimeMillis() - start));
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

        CompletableFuture<BookInfo> getBookPreviewFuture = CompletableFuture.supplyAsync(() -> {
            Log.d("TIMER", "Start getBookPreview at " + (System.currentTimeMillis() - start));
            BookInfo result = new BookInfo();
            try {
                Bitmap cover = extractCoverPreview(bookFile,300,400);

                if (cover == null) {
                    // Можна тут додати fallback — генерацію текстового прев’ю або заглушку
                    Log.d("Fb2Processor", "Cover image not found");
                    return result;
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
                }

                result.filePath = bookFile.getAbsolutePath();
                result.previewPath = previewFile.getAbsolutePath();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Log.d("TIMER", "End getBookPreview at " + (System.currentTimeMillis() - start));
            return result;
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

        return CompletableFuture.allOf(getBookInfoFuture, getBookPreviewFuture).thenApply(v -> {
            BookInfo bookWithInfo = getBookInfoFuture.join();
            BookInfo bookWithPaths = getBookPreviewFuture.join();

            if (bookWithInfo == null) bookWithInfo = new BookInfo();
            if (bookWithPaths == null) bookWithPaths = new BookInfo();

            if (bookWithInfo.title == null || bookWithInfo.title.trim().isEmpty()) {
                bookWithInfo.title = bookFile.getName();
            }
            if (bookWithInfo.author == null || bookWithInfo.author.trim().isEmpty()) {
                bookWithInfo.author = "Unknown";
            }
            bookWithInfo.previewPath = bookWithPaths.previewPath;
            bookWithInfo.filePath = bookWithPaths.filePath;

            return bookWithInfo;
        });
    }

    @Override
    public CompletableFuture<BookInfo> processFileAsync(Context context, android.net.Uri bookUri) throws java.io.IOException {
        File file = new File(FileHelper.getPath(context, bookUri));
        return processFileAsync(context, file);
    }

    @Override
    public CompletableFuture<Bitmap> getPreviewAsync(File bookFile, int pageIndex, int height, int width) {
        // Для FB2 сторінки як у PDF нема, повертаємо обкладинку (або null)
        return CompletableFuture.supplyAsync(() ->  extractCoverPreview(bookFile,width,height));
    }

    // --- Допоміжні методи ---

    private String getTagText(Document doc, String tagName) {
        NodeList list = doc.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return null;
    }

    private String getAuthorName(Document doc) {
        String firstName = getTagText(doc, "first-name");
        String lastName = getTagText(doc, "last-name");
        if ((firstName != null && !firstName.isEmpty()) || (lastName != null && !lastName.isEmpty())) {
            return ((firstName != null) ? firstName : "") + " " + ((lastName != null) ? lastName : "");
        }
        return null;
    }

    public Bitmap extractCoverPreview(File fb2File, int width, int height) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fb2File);

            // Знаходимо <coverpage>
            NodeList coverPages = doc.getElementsByTagName("coverpage");
            if (coverPages.getLength() == 0) return null;

            Element coverPage = (Element) coverPages.item(0);

            // Знаходимо <image> всередині <coverpage>
            NodeList images = coverPage.getElementsByTagName("image");
            if (images.getLength() == 0) return null;

            Element imageElem = (Element) images.item(0);

            // Отримуємо атрибут l:href або xlink:href
            String href = imageElem.getAttributeNS(XLINK_NS, "href");
            if (href == null || !href.startsWith("#")) return null;

            String coverId = href.substring(1); // вирізаємо #

            // Шукаємо відповідний <binary>
            NodeList binaryNodes = doc.getElementsByTagName("binary");
            for (int i = 0; i < binaryNodes.getLength(); i++) {
                Element binElem = (Element) binaryNodes.item(i);
                if (coverId.equals(binElem.getAttribute("id"))) {
                    String base64 = binElem.getTextContent().trim();
                    byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);

                    // Читаємо лише розміри
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(decoded, 0, decoded.length, options);

                    int originalWidth = options.outWidth;
                    int originalHeight = options.outHeight;

                    // Обчислюємо scale
                    int sampleSize = 1;
                    while ((originalWidth / sampleSize) > width || (originalHeight / sampleSize) > height) {
                        sampleSize *= 2;
                    }

                    // Декодуємо з урахуванням масштабу
                    BitmapFactory.Options scaledOptions = new BitmapFactory.Options();
                    scaledOptions.inSampleSize = sampleSize;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length, scaledOptions);

                    if (bitmap == null) return null;

                    // Масштабуємо до точного розміру (можна пропустити, якщо sampleSize достатньо)
                    return Bitmap.createScaledBitmap(bitmap, width, height, true);
                }
            }
        } catch (Exception e) {
            Log.e("Fb2Processor", "Failed to extract preview", e);
        }

        return null;
    }
}