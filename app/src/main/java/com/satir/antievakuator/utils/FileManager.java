package com.satir.antievakuator.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileManager {
    private Context mContext;

    public FileManager(Context context){
        mContext = context;
    }

    public Uri generateFile() {
        //Строки закомменчены, т.к. на 6 версии обнаружились вылеты с SecureException. Необходимо протестировать новый способ, если рабочий-удалить закомменченный код
//        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//            return null;
//
//        File path = new File(Environment.getExternalStorageDirectory(), "AntievakuatorPhoto");
//        if (!path.exists()) {
//            if (!path.mkdirs()) {
//                return null;
//            }
//        }

        File file = new File(mContext.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "evakuatorPhoto.jpg");
//        File file = new File("evakuatorPhoto.jpg");
//        Uri photoUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
        Uri uri;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
        }else {
            uri = Uri.fromFile(file);
        }
//        File newFile = new File(path.getPath() + File.separator + "evakuatorPhoto" + ".jpg");
        return uri;
    }

    public void copyPhoto(Uri uri) {
        //Получаем картинку
        InputStream is = null;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        //Если надо разворачиваем
        Cursor cursor = mContext.getContentResolver().query(uri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
        if (cursor.getCount() != 1) {
            cursor.close();
        }
        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        cursor = null;
        if(orientation != 0){
            bitmap = PictureUtils.rotateImage(bitmap, orientation);
        }
        //копируем байты в массив
        byte[] bytes;
        ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytesStream);
        bytes = bytesStream.toByteArray();
        try {
            bytesStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Созраняем массив в новый файл
        try {
            savePhotoInFile(bytes, generateFile());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void savePhotoInFile(byte[] data, Uri pictureFile) throws Exception {

        if (pictureFile == null) {
            throw new Exception();
        }

        OutputStream os = mContext.getContentResolver().openOutputStream(pictureFile, "rwt");
        if (os != null) {
            os.write(data);
            os.close();
        }
    }
}
