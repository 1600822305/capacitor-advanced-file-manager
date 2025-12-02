package com.mycompany.plugins.example.picker;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

import java.io.File;

/**
 * 系统文件选择器模块
 * 负责调用系统文件选择器和处理结果
 */
public class SystemFilePicker {
    private final Plugin plugin;
    private final UriResolver uriResolver;

    public SystemFilePicker(Plugin plugin) {
        this.plugin = plugin;
        this.uriResolver = new UriResolver(plugin.getContext());
    }

    /**
     * 打开系统文件选择器
     */
    public void openFilePicker(PluginCall call, String type, boolean multiple, 
                                JSArray acceptArray, String title) {
        Intent intent;

        if ("directory".equals(type)) {
            // 选择目录
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra(DocumentsContract.EXTRA_PROMPT, title);
        } else {
            // 选择文件
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (multiple) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }

            // 设置文件类型过滤
            if (acceptArray != null && acceptArray.length() > 0) {
                try {
                    String[] mimeTypes = new String[acceptArray.length()];
                    for (int i = 0; i < acceptArray.length(); i++) {
                        String ext = acceptArray.getString(i);
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                        mimeTypes[i] = mimeType != null ? mimeType : "*/*";
                    }
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                } catch (Exception e) {
                    intent.setType("*/*");
                }
            } else {
                intent.setType("*/*");
            }
        }

        plugin.startActivityForResult(call, intent, "filePickerResult");
    }

    /**
     * 获取文件信息从 URI
     */
    public JSObject getFileInfoFromUri(Uri uri) {
        JSObject fileInfo = new JSObject();
        ContentResolver contentResolver = plugin.getContext().getContentResolver();

        try {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // 获取文件名
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String fileName = nameIndex >= 0 ? cursor.getString(nameIndex) : "unknown";

                // 获取文件大小
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                long fileSize = sizeIndex >= 0 ? cursor.getLong(sizeIndex) : 0;

                // 获取 MIME 类型
                String mimeType = contentResolver.getType(uri);

                // 判断是否为目录
                boolean isDirectory = DocumentsContract.isDocumentUri(plugin.getContext(), uri) &&
                                    DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);

                // 尝试获取真实路径
                String realPath = uriResolver.getRealPathFromUri(uri);

                fileInfo.put("name", fileName);
                fileInfo.put("path", realPath != null ? realPath : uri.toString());
                fileInfo.put("uri", uri.toString());
                fileInfo.put("size", fileSize);
                fileInfo.put("type", isDirectory ? "directory" : "file");
                fileInfo.put("mimeType", mimeType != null ? mimeType : "application/octet-stream");
                fileInfo.put("mtime", System.currentTimeMillis());
                fileInfo.put("ctime", System.currentTimeMillis());

                cursor.close();
                return fileInfo;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            // 如果查询失败，返回基本信息
            fileInfo.put("name", "unknown");
            fileInfo.put("path", uri.toString());
            fileInfo.put("uri", uri.toString());
            fileInfo.put("size", 0);
            fileInfo.put("type", "file");
            fileInfo.put("mimeType", "application/octet-stream");
            fileInfo.put("mtime", System.currentTimeMillis());
            fileInfo.put("ctime", System.currentTimeMillis());
        }

        return fileInfo;
    }

    /**
     * 获取 URI 解析器
     */
    public UriResolver getUriResolver() {
        return uriResolver;
    }
}
