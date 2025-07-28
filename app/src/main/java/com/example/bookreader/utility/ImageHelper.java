package com.example.bookreader.utility;

import static com.example.bookreader.constants.Constants.PREVIEWS_DIR;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class ImageHelper {

    public static CompletableFuture<String> saveImageAsync(Context context, Bitmap image , int quality , Bitmap.CompressFormat format){
        return CompletableFuture.supplyAsync(()-> saveImage(context,image,quality,format));
    }

    public static String saveImage(Context context, Bitmap image , int quality , Bitmap.CompressFormat format){
        File previewDir = new File(context.getFilesDir(), PREVIEWS_DIR);
        if (!previewDir.exists()) previewDir.mkdirs();
        File previewFile = new File(previewDir, UUID.randomUUID().toString() + ".png");
        try (FileOutputStream out = new FileOutputStream(previewFile)) {
            image.compress(format, quality, out);
            out.flush();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return previewFile.getAbsolutePath();
    }

    public static  CompletableFuture<Boolean> removeImage(String imagePath){
        return CompletableFuture.supplyAsync(()->{
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                return file.delete();
            }
            return false;
        });
    }
}
