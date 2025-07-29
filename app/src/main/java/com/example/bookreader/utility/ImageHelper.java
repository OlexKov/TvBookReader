package com.example.bookreader.utility;

import static com.example.bookreader.constants.Constants.PREVIEWS_DIR;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jp.wasabeef.glide.transformations.BlurTransformation;


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

    public static void getBlurBitmap(Context context, String path, Consumer<Bitmap> handler){
        Glide.with(context)
                .asBitmap()
                .load(path)
                .transform(new BlurTransformation(3, 3))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                       handler.accept(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

}
