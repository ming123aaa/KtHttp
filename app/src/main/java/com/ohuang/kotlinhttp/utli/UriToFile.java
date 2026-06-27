package com.ohuang.kotlinhttp.utli;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UriToFile {


    static class FileDescriptorInputStream extends FileInputStream{


        ParcelFileDescriptor parcelFileDescriptor;  // 为了引用这个对象,避免被系统回收

        public FileDescriptorInputStream(ParcelFileDescriptor parcelFileDescriptor) {
            super(parcelFileDescriptor.getFileDescriptor());
            this.parcelFileDescriptor=parcelFileDescriptor;
        }
    }

    @Nullable
    public static String getFileName(Uri uri,Context context){
       return getFileName(context.getContentResolver(),uri);
    }
    @Nullable
    public static FileInputStream uriToFileInputStream(Uri uri, Context context) {
        FileInputStream fileInputStream = null;
        try {
            ParcelFileDescriptor r = context.getContentResolver().openFileDescriptor(uri, "r");
            if (r != null) {
                fileInputStream = new FileDescriptorInputStream(r);
            }
        } catch (Throwable e) {

        }
        if (fileInputStream == null) {
            try {
                File file = uriToFile(uri, context);
                if (file != null) {
                    fileInputStream = new FileInputStream(file);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return fileInputStream;
    }


    /**
     * 统一的 URI 转 File 方法，兼容所有 Android 版本
     */
    @Nullable
    public static File uriToFile(Uri uri, Context context) {
        if (uri == null) return null;
        String scheme = uri.getScheme();
        if (scheme == null) return null;

        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) return file;
            }
            return null;
        }

        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            return contentUriToFile(uri, context);
        }

        return null;
    }

    /**
     * 将 content:// URI 转换为本地文件
     * 优先尝试直接获取文件路径，失败则复制到缓存目录
     */
    @Nullable
    private static File contentUriToFile(Uri uri, Context context) {
        // 尝试通过路径获取
        String path = getPath(context, uri);
        if (path != null) {
            File file = new File(path);
            if (file.exists() && file.length() > 0) {
                return file;
            }
        }

        // 降级方案：复制到缓存目录
        return copyToCache(uri, context);
    }

    /**
     * 将 content:// URI 的内容复制到缓存目录
     */
    @Nullable
    private static File copyToCache(Uri uri, Context context) {
        ContentResolver resolver = context.getContentResolver();
        String fileName = getFileName(resolver, uri);
        if (fileName == null) {
            String ext = getExtensionFromUri(resolver, uri);
            fileName = System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
            if (ext != null) {
                fileName += "." + ext;
            }
        }

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = resolver.openInputStream(uri);
            if (is == null) return null;

            File cacheDir = new File(context.getCacheDir(), "upload_cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File destFile = new File(cacheDir, fileName);

            fos = new FileOutputStream(destFile);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            return destFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeQuietly(is);
            closeQuietly(fos);
        }
    }

    public static File copyChecheDir(Context context) {
        return new File(context.getCacheDir(), "upload_cache");
    }

    /**
     * 从 content:// URI 中获取文件名
     */
    @Nullable
    private static String getFileName(ContentResolver resolver, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            closeQuietly(cursor);
        }
        return null;
    }

    /**
     * 从 URI 的 MIME type 推断文件扩展名
     */
    @Nullable
    private static String getExtensionFromUri(ContentResolver resolver, Uri uri) {
        try {
            String mimeType = resolver.getType(uri);
            if (mimeType != null) {
                return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 通过 content:// URI 获取文件的真实路径
     * 注意：Android 10+ 大部分 content:// URI 无法直接获取路径，返回 null
     */
    @Nullable
    public static String getPath(Context context, Uri uri) {
        if (uri == null || uri.getScheme() == null) return null;

        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            return getDocumentPath(context, uri);
        }

        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            return getContentPath(context, uri);
        }

        return null;
    }

    /**
     * 处理 Document URI（4.4+）
     */
    @Nullable
    private static String getDocumentPath(Context context, Uri uri) {
        String authority = uri.getAuthority();
        if (authority == null) return null;

        if (isExternalStorageDocument(uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] parts = docId.split(":");
            if (parts.length < 2) return null;
            if ("primary".equalsIgnoreCase(parts[0])) {
                return Environment.getExternalStorageDirectory() + "/" + parts[1];
            }
        } else if (isDownloadsDocument(uri)) {
            String id = DocumentsContract.getDocumentId(uri);
            if (id == null) return null;
            try {
                // 尝试直接解析为数字 ID
                long longId = Long.parseLong(id);
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), longId);
                return getDataColumn(context, contentUri, null, null);
            } catch (NumberFormatException e) {
                // 某些下载管理器返回的是 content:// URI 路径
                if (id.startsWith("raw:")) {
                    return id.substring(4);
                }
                if (id.startsWith("content://")) {
                    return getDataColumn(context, Uri.parse(id), null, null);
                }
            }
        } else if (isMediaDocument(uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if (docId == null) return null;
            String[] parts = docId.split(":");
            if (parts.length < 2) return null;
            Uri contentUri = getMediaContentUri(parts[0]);
            if (contentUri != null) {
                return getDataColumn(context, contentUri, "_id=?", new String[]{parts[1]});
            }
        }
        return null;
    }

    /**
     * 处理普通 content:// URI
     */
    @Nullable
    private static String getContentPath(Context context, Uri uri) {
        if (isGooglePhotosUri(uri)) {
            return uri.getLastPathSegment();
        }
        return getDataColumn(context, uri, null, null);
    }

    @Nullable
    private static Uri getMediaContentUri(String type) {
        switch (type) {
            case "image":
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            case "video":
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case "audio":
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            default:
                return null;
        }
    }

    @Nullable
    private static String getDataColumn(Context context, Uri uri,
                                        String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("_data");
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            closeQuietly(cursor);
        }
        return null;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
