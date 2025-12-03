package com.mycompany.plugins.example.picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;

/**
 * URI 解析器
 * 负责将 content:// URI 转换为真实文件路径
 */
public class UriResolver {
    private final Context context;

    public UriResolver(Context context) {
        this.context = context;
    }

    /**
     * 从 URI 获取真实路径
     */
    public String getRealPathFromUri(Uri uri) {
        try {
            if ("content".equals(uri.getScheme())) {
                String docId = null;
                
                // 先尝试作为 Document URI 处理
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                } else {
                    // 尝试作为 Tree URI 处理（选择目录时返回的）
                    try {
                        docId = DocumentsContract.getTreeDocumentId(uri);
                    } catch (Exception e) {
                        // 不是 Tree URI，尝试其他方式
                        return getDataColumn(uri, null, null);
                    }
                }

                if (docId != null) {
                    if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                        return handleExternalStorageDocument(docId);
                    } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                        return handleDownloadsDocument(docId);
                    } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                        return handleMediaDocument(docId);
                    }
                }
            } else if ("file".equals(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            // 忽略错误，返回 null
        }

        return null;
    }

    /**
     * 处理外部存储文档
     */
    private String handleExternalStorageDocument(String docId) {
        String[] split = docId.split(":");
        if (split.length >= 2) {
            String type = split[0];
            String path = split[1];

            if ("primary".equalsIgnoreCase(type)) {
                if (path.isEmpty()) {
                    return "/storage/emulated/0";
                } else {
                    return "/storage/emulated/0/" + path;
                }
            } else {
                // 外部 SD 卡或其他存储
                String[] possiblePaths = {
                    "/storage/" + type + "/" + path,
                    "/mnt/media_rw/" + type + "/" + path,
                    "/storage/sdcard1/" + path
                };

                for (String possiblePath : possiblePaths) {
                    File file = new File(possiblePath);
                    if (file.exists()) {
                        return possiblePath;
                    }
                }

                return "/storage/" + type + "/" + path;
            }
        }
        return null;
    }

    /**
     * 处理下载文档
     */
    private String handleDownloadsDocument(String docId) {
        if (docId.startsWith("raw:")) {
            return docId.replaceFirst("raw:", "");
        }

        try {
            Uri contentUri = Uri.parse("content://downloads/public_downloads");
            Uri uri = Uri.withAppendedPath(contentUri, docId);
            return getDataColumn(uri, null, null);
        } catch (Exception e) {
            return "/storage/emulated/0/Download";
        }
    }

    /**
     * 处理媒体文档
     */
    private String handleMediaDocument(String docId) {
        String[] split = docId.split(":");
        if (split.length >= 2) {
            String type = split[0];
            String id = split[1];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            if (contentUri != null) {
                String selection = "_id=?";
                String[] selectionArgs = new String[]{id};
                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        return null;
    }

    /**
     * 获取数据列
     */
    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            // 忽略错误
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
