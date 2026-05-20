package com.fileguardpro.scanner.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    public static String getFileName(Context context, Uri uri) {
        String name = "Unknown";
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            String path = uri.getPath();
            if (path != null) {
                name = path.substring(path.lastIndexOf('/') + 1);
            }
        }
        return name;
    }

    public static long getFileSize(Context context, Uri uri) {
        long size = 0;
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0 && cursor.moveToFirst()) {
                    size = cursor.getLong(sizeIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            // Ignore
        }
        return size;
    }

    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    public static File copyToCache(Context context, Uri uri, String fileName) {
        try {
            File cacheFile = new File(context.getCacheDir(), fileName);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                return cacheFile;
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    public static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        else return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}
