package com.mycompany.plugins.example.core;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 目录操作模块
 * 负责目录的列表、创建、删除等操作
 */
public class DirectoryOperations {
    private static final String TAG = "DirectoryOperations";
    private final Context context;

    public DirectoryOperations(Context context) {
        this.context = context;
    }

    /**
     * 列出目录内容
     */
    public JSObject listDirectory(String path, boolean showHidden, String sortBy, String sortOrder) throws Exception {
        // 处理 content:// URI
        if (path.startsWith("content://")) {
            return listDirectoryFromUri(path, showHidden, sortBy, sortOrder);
        }

        File directory = new File(path);

        Log.d(TAG, "Listing directory: " + path);
        Log.d(TAG, "Directory exists: " + directory.exists());
        Log.d(TAG, "Is directory: " + directory.isDirectory());
        Log.d(TAG, "Can read: " + directory.canRead());

        if (!directory.exists()) {
            throw new Exception("Directory does not exist: " + path);
        }

        if (!directory.isDirectory()) {
            throw new Exception("Path is not a directory: " + path);
        }

        if (!directory.canRead()) {
            throw new Exception("No read permission for directory: " + path);
        }

        File[] files = directory.listFiles();
        Log.d(TAG, "listFiles() returned: " + (files != null ? files.length + " files" : "null"));

        if (files == null) {
            Log.w(TAG, "listFiles() returned null for: " + path + ". Returning empty list.");
            JSObject result = new JSObject();
            result.put("files", new JSArray());
            result.put("totalCount", 0);
            return result;
        }

        List<File> fileList = new ArrayList<>(Arrays.asList(files));

        // 过滤隐藏文件
        if (!showHidden) {
            fileList.removeIf(file -> file.getName().startsWith("."));
        }

        // 排序
        sortFiles(fileList, sortBy, sortOrder);

        JSArray filesArray = new JSArray();
        for (File file : fileList) {
            JSObject fileInfo = FileUtils.createFileInfo(file);
            filesArray.put(fileInfo);
        }

        JSObject result = new JSObject();
        result.put("files", filesArray);
        result.put("totalCount", filesArray.length());

        return result;
    }

    /**
     * 创建目录
     */
    public void createDirectory(String path, boolean recursive) throws Exception {
        File directory = new File(path);

        if (directory.exists()) {
            if (directory.isDirectory()) {
                return; // 目录已存在
            }
            throw new Exception("Path exists but is not a directory: " + path);
        }

        boolean success;
        if (recursive) {
            success = directory.mkdirs();
        } else {
            success = directory.mkdir();
        }

        if (!success) {
            throw new Exception("Failed to create directory: " + path);
        }
    }

    /**
     * 删除目录
     */
    public void deleteDirectory(String path) throws Exception {
        File directory = new File(path);

        if (!directory.exists()) {
            throw new Exception("Directory does not exist: " + path);
        }

        if (!directory.isDirectory()) {
            throw new Exception("Path is not a directory: " + path);
        }

        if (!FileUtils.deleteRecursively(directory)) {
            throw new Exception("Failed to delete directory: " + path);
        }
    }

    /**
     * 从 content:// URI 列出目录内容（与模块化前完全一致）
     */
    private JSObject listDirectoryFromUri(String uriString, boolean showHidden, String sortBy, String sortOrder) throws Exception {
        Uri uri = Uri.parse(uriString);
        ContentResolver contentResolver = context.getContentResolver();

        if (!DocumentsContract.isDocumentUri(context, uri)) {
            // 对于 Tree URI，尝试使用 getTreeDocumentId
            try {
                String treeDocId = DocumentsContract.getTreeDocumentId(uri);
                if (treeDocId != null) {
                    // 这是一个 Tree URI，继续处理
                    Log.d(TAG, "Processing as Tree URI with docId: " + treeDocId);
                }
            } catch (Exception e) {
                throw new Exception("URI is not a valid document or tree URI: " + uriString);
            }
        }

        try {
            // 获取目录的子文档
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));

            Cursor cursor = contentResolver.query(
                childrenUri,
                new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                },
                null,
                null,
                null
            );

            if (cursor == null) {
                throw new Exception("Cannot access directory: " + uriString);
            }

            JSArray filesArray = new JSArray();

            while (cursor.moveToNext()) {
                String documentId = cursor.getString(0);
                String displayName = cursor.getString(1);
                long size = cursor.getLong(2);
                String mimeType = cursor.getString(3);
                long lastModified = cursor.getLong(4);

                // 过滤隐藏文件
                if (!showHidden && displayName.startsWith(".")) {
                    continue;
                }

                boolean isDirectory = DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);

                JSObject fileInfo = new JSObject();
                fileInfo.put("name", displayName);
                fileInfo.put("path", DocumentsContract.buildDocumentUriUsingTree(uri, documentId).toString());
                fileInfo.put("size", size);
                fileInfo.put("type", isDirectory ? "directory" : "file");
                fileInfo.put("mtime", lastModified);
                fileInfo.put("ctime", lastModified);
                fileInfo.put("isHidden", displayName.startsWith("."));
                fileInfo.put("permissions", "rw-");

                filesArray.put(fileInfo);
            }

            cursor.close();

            JSObject result = new JSObject();
            result.put("files", filesArray);
            result.put("totalCount", filesArray.length());

            return result;

        } catch (Exception e) {
            throw new Exception("Failed to list directory from URI: " + e.getMessage());
        }
    }

    /**
     * 排序文件列表
     */
    private void sortFiles(List<File> files, String sortBy, String sortOrder) {
        Comparator<File> comparator;

        switch (sortBy) {
            case "size":
                comparator = Comparator.comparingLong(File::length);
                break;
            case "mtime":
                comparator = Comparator.comparingLong(File::lastModified);
                break;
            case "type":
                comparator = (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                };
                break;
            case "name":
            default:
                comparator = (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName());
                break;
        }

        if ("desc".equals(sortOrder)) {
            comparator = comparator.reversed();
        }

        // 保持目录在前
        Comparator<File> finalComparator = comparator;
        files.sort((f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return finalComparator.compare(f1, f2);
        });
    }
}
