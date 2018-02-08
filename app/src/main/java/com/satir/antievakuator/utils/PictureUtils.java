package com.satir.antievakuator.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PictureUtils {

    public static void setPic(Uri file, ImageView imageView, Activity activity) {
        int targetW = 320;
        int targetH = 320;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        InputStream input;
        Bitmap bitmap;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            try {
                input = activity.getContentResolver().openInputStream(file);
                bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
                input.close();
            } catch (IOException e) {
                return;
            }
        }
        else {
            bitmap = BitmapFactory.decodeFile(file.getPath(), bmOptions);
        }

        ExifInterface ei;
        try {
            String path = file.getPath();
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setMaxHeight(targetH);
        imageView.setMaxWidth(targetW);
        imageView.setImageBitmap(bitmap);
    }

    public static Bitmap rotateImage(Bitmap bitmap, int degrees){
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        bitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }
}
