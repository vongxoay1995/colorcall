package com.colorcall.callerscreen.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    public static void saveBitmap(String path, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void saveBitmap(String path,String name, Bitmap bitmap) {
        File myDir = new File(path);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File file = new File (myDir,name);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "ColorPhone_" + System.currentTimeMillis(), null);
        return Uri.parse(path);
    }
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        if (contentUri != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return getFilePathForN(context, contentUri);
            } else {
                Cursor cursor = null;
                try {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                    if (cursor != null) {
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        return cursor.getString(column_index);
                    } else {
                        return "";
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        } else
            return "";
    }
    public static File getInternalFileDirMyImage(Context context) {
        File file = new File(context.getFilesDir(), Constant.TEMP_DIR);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
    private static String getFilePathForN(Context context, Uri uri) {
        File file;
        Cursor returnCursor = null;
        try {
            returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = (returnCursor.getString(nameIndex));
            file = new File(context.getFilesDir(), name);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        } finally {
            if (returnCursor != null) {
                returnCursor.close();
            }
        }
        return "";
    }

    public static File createImageFile(Context context) throws IOException {
        File storageDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("ColorCall_image_default",".jpg", storageDir);
        return image;
    }
    public interface CreateImageInputInterface {
        void onImageCreateSuccess(String videoInputPath);
        void onImageCreateFailed();
    }
    public static File getInternalFileDir(Context context) {
        File file = new File(context.getFilesDir(), Constant.THUMB_DIR);
        if (!file.exists()) {
            boolean created = file.mkdir();
        }
        return file;
    }
    public static void createImagefromPath(Context context, Uri mUri,String name, CreateImageInputInterface listener) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        Handler handler = new Handler();
        Thread thread = new Thread(() -> {
            File inputFile = new File(getInternalFileDir(context), name);
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(mUri);
                OutputStream outputStream = new FileOutputStream(inputFile);
                byte[] buf = new byte[2048];
                int length;
                while ((length = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, length);
                }
                outputStream.close();
                inputStream.close();
                handler.post(() -> {
                    progressDialog.dismiss();
                    listener.onImageCreateSuccess(inputFile.getAbsolutePath());
                });
            } catch (IOException e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    listener.onImageCreateFailed();
                });
            }
        });
        thread.start();
    }
}
