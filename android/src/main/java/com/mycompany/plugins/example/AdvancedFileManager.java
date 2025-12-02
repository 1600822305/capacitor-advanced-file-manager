package com.mycompany.plugins.example;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdvancedFileManager {
    private Context context;

    public AdvancedFileManager(Context context) {
        this.context = context;
    }

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    public JSObject listDirectory(String path, boolean showHidden, String sortBy, String sortOrder) throws Exception {
        // 处理 content:// URI
        if (path.startsWith("content://")) {
            return listDirectoryFromUri(path, showHidden, sortBy, sortOrder);
        }

        File directory = new File(path);

        Log.d("AdvancedFileManager", "Listing directory: " + path);
        Log.d("AdvancedFileManager", "Directory exists: " + directory.exists());
        Log.d("AdvancedFileManager", "Is directory: " + directory.isDirectory());
        Log.d("AdvancedFileManager", "Can read: " + directory.canRead());
        Log.d("AdvancedFileManager", "Can write: " + directory.canWrite());

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
        Log.d("AdvancedFileManager", "listFiles() returned: " + (files != null ? files.length + " files" : "null"));

        if (files == null) {
            // 不要抛出异常，返回空列表
            Log.w("AdvancedFileManager", "listFiles() returned null for: " + path + ". Returning empty list.");
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
            JSObject fileInfo = createFileInfo(file);
            filesArray.put(fileInfo);
        }

        JSObject result = new JSObject();
        result.put("files", filesArray);
        result.put("totalCount", filesArray.length());

        return result;
    }

    private JSObject listDirectoryFromUri(String uriString, boolean showHidden, String sortBy, String sortOrder) throws Exception {
        Uri uri = Uri.parse(uriString);
        ContentResolver contentResolver = context.getContentResolver();

        if (!DocumentsContract.isDocumentUri(context, uri)) {
            throw new Exception("URI is not a document URI: " + uriString);
        }

        try {
            // 获取目录的子文档
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));

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
                fileInfo.put("permissions", "rw-"); // 假设有读写权限

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
            default: // name
                comparator = (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName());
                break;
        }

        if ("desc".equals(sortOrder)) {
            comparator = comparator.reversed();
        }

        Collections.sort(files, comparator);
    }

    private JSObject createFileInfo(File file) {
        JSObject fileInfo = new JSObject();
        fileInfo.put("name", file.getName());
        fileInfo.put("path", file.getAbsolutePath());
        fileInfo.put("size", file.length());
        fileInfo.put("type", file.isDirectory() ? "directory" : "file");
        fileInfo.put("mtime", file.lastModified());
        fileInfo.put("ctime", file.lastModified()); // Android doesn't provide creation time
        fileInfo.put("isHidden", file.getName().startsWith("."));

        // 添加权限信息
        StringBuilder permissions = new StringBuilder();
        permissions.append(file.canRead() ? "r" : "-");
        permissions.append(file.canWrite() ? "w" : "-");
        permissions.append(file.canExecute() ? "x" : "-");
        fileInfo.put("permissions", permissions.toString());

        return fileInfo;
    }

    public void createDirectory(String path, boolean recursive) throws Exception {
        File directory = new File(path);

        boolean success;
        if (recursive) {
            success = directory.mkdirs();
        } else {
            success = directory.mkdir();
        }

        if (!success && !directory.exists()) {
            throw new Exception("Failed to create directory: " + path);
        }
    }

    public void deleteDirectory(String path) throws Exception {
        File directory = new File(path);

        if (!directory.exists()) {
            throw new Exception("Directory does not exist: " + path);
        }

        if (!directory.isDirectory()) {
            throw new Exception("Path is not a directory: " + path);
        }

        if (!deleteRecursively(directory)) {
            throw new Exception("Failed to delete directory: " + path);
        }
    }

    private boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    public void createFile(String path, String content, String encoding) throws Exception {
        File file = new File(path);

        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new Exception("Failed to create parent directories for: " + path);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data;
            if ("base64".equals(encoding)) {
                data = Base64.decode(content, Base64.DEFAULT);
            } else {
                data = content.getBytes(StandardCharsets.UTF_8);
            }
            fos.write(data);
        } catch (IOException e) {
            throw new Exception("Failed to create file: " + e.getMessage());
        }
    }

    public JSObject readFile(String path, String encoding) throws Exception {
        File file = new File(path);

        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        if (!file.isFile()) {
            throw new Exception("Path is not a file: " + path);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);

            String content;
            if ("base64".equals(encoding)) {
                content = Base64.encodeToString(data, Base64.DEFAULT);
            } else {
                content = new String(data, StandardCharsets.UTF_8);
            }

            JSObject result = new JSObject();
            result.put("content", content);
            result.put("encoding", encoding);

            return result;
        } catch (IOException e) {
            throw new Exception("Failed to read file: " + e.getMessage());
        }
    }

    public void writeFile(String path, String content, String encoding, boolean append) throws Exception {
        File file = new File(path);

        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new Exception("Failed to create parent directories for: " + path);
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file, append)) {
            byte[] data;
            if ("base64".equals(encoding)) {
                data = Base64.decode(content, Base64.DEFAULT);
            } else {
                data = content.getBytes(StandardCharsets.UTF_8);
            }
            fos.write(data);
        } catch (IOException e) {
            throw new Exception("Failed to write file: " + e.getMessage());
        }
    }

    public void deleteFile(String path) throws Exception {
        File file = new File(path);

        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        if (!file.isFile()) {
            throw new Exception("Path is not a file: " + path);
        }

        if (!file.delete()) {
            throw new Exception("Failed to delete file: " + path);
        }
    }

    public void renameFile(String path, String newName) throws Exception {
        File file = new File(path);

        if (!file.exists()) {
            throw new Exception("File or directory does not exist: " + path);
        }

        // 获取父目录
        File parentDir = file.getParentFile();
        if (parentDir == null) {
            throw new Exception("Cannot get parent directory for: " + path);
        }

        // 创建新文件路径
        File newFile = new File(parentDir, newName);

        // 检查目标是否已存在
        if (newFile.exists()) {
            throw new Exception("A file or directory with that name already exists: " + newFile.getPath());
        }

        // 执行重命名
        if (!file.renameTo(newFile)) {
            throw new Exception("Failed to rename: " + path + " to " + newName);
        }
    }

    public void moveFile(String sourcePath, String destinationPath) throws Exception {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath);

        if (!sourceFile.exists()) {
            throw new Exception("Source file does not exist: " + sourcePath);
        }

        // 确保目标父目录存在
        File destParent = destFile.getParentFile();
        if (destParent != null && !destParent.exists()) {
            if (!destParent.mkdirs()) {
                throw new Exception("Failed to create destination directory");
            }
        }

        // 尝试直接重命名（同一文件系统内更快）
        if (!sourceFile.renameTo(destFile)) {
            // 如果重命名失败，尝试复制后删除
            copyFileInternal(sourceFile, destFile);
            if (!deleteRecursively(sourceFile)) {
                throw new Exception("Failed to delete source after copy");
            }
        }
    }

    public void copyFile(String sourcePath, String destinationPath, boolean overwrite) throws Exception {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath);

        if (!sourceFile.exists()) {
            throw new Exception("Source file does not exist: " + sourcePath);
        }

        if (destFile.exists() && !overwrite) {
            throw new Exception("Destination already exists: " + destinationPath);
        }

        // 确保目标父目录存在
        File destParent = destFile.getParentFile();
        if (destParent != null && !destParent.exists()) {
            if (!destParent.mkdirs()) {
                throw new Exception("Failed to create destination directory");
            }
        }

        copyFileInternal(sourceFile, destFile);
    }

    private void copyFileInternal(File source, File dest) throws Exception {
        if (source.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdirs();
            }
            File[] children = source.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyFileInternal(child, new File(dest, child.getName()));
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(dest)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
        }
    }
}
