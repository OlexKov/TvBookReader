package com.example.bookreader.utility;

import static com.example.bookreader.constants.Constants.READER_PAGE_DEFAULT_BRIGHTNESS;
import static com.example.bookreader.constants.Constants.READER_PAGE_DEFAULT_CONTRAST;
import static com.example.bookreader.constants.Constants.READER_PAGE_MAX_BRIGHTNESS;
import static com.example.bookreader.constants.Constants.READER_PAGE_MAX_CONTRAST;
import static com.example.bookreader.constants.Constants.READER_PAGE_MIN_BRIGHTNESS;
import static com.example.bookreader.constants.Constants.READER_PAGE_MIN_CONTRAST;

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

    public static Bitmap processBitmap(
            @NonNull Bitmap original,
            boolean invert,
            float contrast,   // 1.0f = без змін
            float brightness  // 0 = без змін, >0 = світліше, <0 = темніше
    ) {
        if(!invert && contrast == READER_PAGE_DEFAULT_CONTRAST && brightness == READER_PAGE_DEFAULT_BRIGHTNESS) return original;
        brightness = Math.max(READER_PAGE_MIN_BRIGHTNESS, Math.min(READER_PAGE_MAX_BRIGHTNESS, brightness));
        contrast = Math.max(READER_PAGE_MIN_CONTRAST, Math.min(READER_PAGE_MAX_CONTRAST, contrast));
        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        // Початкова матриця (identity)
        ColorMatrix finalMatrix = new ColorMatrix();



        // 🔹 Контрастність
        // contrast = 1.0 → без змін;  >1.0 → сильніше; 0.5 → слабше
        float scale = contrast;
        float translate = (-0.5f * scale + 0.5f) * 255f;
        ColorMatrix contrastMatrix = new ColorMatrix(new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        finalMatrix.postConcat(contrastMatrix);

        // 🔹 Яскравість
        if (brightness != 0f) {
            ColorMatrix brightnessMatrix = new ColorMatrix(new float[]{
                    1, 0, 0, 0, brightness,
                    0, 1, 0, 0, brightness,
                    0, 0, 1, 0, brightness,
                    0, 0, 0, 1, 0
            });
            finalMatrix.postConcat(brightnessMatrix);
        }

        // 🔹 Інверсія
        if (invert) {
            ColorMatrix invertMatrix = new ColorMatrix(new float[]{
                    -1.0f, 0, 0, 0, 255,
                    0, -1.0f, 0, 0, 255,
                    0, 0, -1.0f, 0, 255,
                    0, 0, 0, 1.0f, 0
            });
            finalMatrix.postConcat(invertMatrix);
        }

        // Малюємо з фільтром
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(finalMatrix));
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(original, 0, 0, paint);

        return result;
    }
}
