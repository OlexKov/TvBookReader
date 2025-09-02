package com.example.bookreader.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class ImageHelper {

    public static CompletableFuture<String> saveImageAsync(Context context,String dirName, Bitmap image , int quality , Bitmap.CompressFormat format){
        return CompletableFuture.supplyAsync(()-> saveImage(context,dirName,image,quality,format));
    }

    public static String saveImage(Context context,String dirName, Bitmap image , int quality , Bitmap.CompressFormat format){
        File previewDir = new File(context.getFilesDir(), dirName);
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

    public static void getBlurBitmap(Context context,int radius,int sampling, String path, Consumer<Bitmap> handler){
        Glide.with(context)
                .asBitmap()
                .load(path)
                .transform(new BlurTransformation(radius, sampling))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                       handler.accept(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    public static Bitmap invertBitmap(@NonNull Bitmap original) {
        Bitmap inverted = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                -1.0f, 0, 0, 0, 255, // R
                0, -1.0f, 0, 0, 255, // G
                0, 0, -1.0f, 0, 255, // B
                0, 0, 0, 1.0f, 0     // Alpha
        });

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        Canvas canvas = new Canvas(inverted);
        canvas.drawBitmap(original, 0, 0, paint);

        return inverted;
    }
}
