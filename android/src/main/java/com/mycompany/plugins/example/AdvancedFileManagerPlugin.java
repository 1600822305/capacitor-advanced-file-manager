package com.mycompany.plugins.example;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.webkit.MimeTypeMap;
import androidx.activity.result.ActivityResult;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@CapacitorPlugin(
    name = "AdvancedFileManager",
    permissions = {
        @Permission(
            strings = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
            alias = "storage"
        )
    }
)
public class AdvancedFileManagerPlugin extends Plugin {

    private AdvancedFileManager implementation;

    @Override
    public void load() {
        super.load();
        implementation = new AdvancedFileManager(getContext());
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (hasStoragePermissions()) {
            JSObject ret = new JSObject();
            ret.put("granted", true);
            ret.put("message", "Storage permissions already granted");
            call.resolve(ret);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ 需要特殊处理 MANAGE_EXTERNAL_STORAGE
                requestManageExternalStoragePermission(call);
            } else {
                // Android 10 及以下使用传统权限
                requestPermissionForAlias("storage", call, "storagePermissionCallback");
            }
        }
    }

    private void requestManageExternalStoragePermission(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(call, intent, "manageStoragePermissionResult");
        } catch (Exception e) {
            // 如果无法打开设置页面，尝试通用设置
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(call, intent, "manageStoragePermissionResult");
        }
    }

    @ActivityCallback
    private void manageStoragePermissionResult(PluginCall call, ActivityResult result) {
        JSObject ret = new JSObject();
        boolean granted = Environment.isExternalStorageManager();
        ret.put("granted", granted);
        ret.put("message", granted ? "MANAGE_EXTERNAL_STORAGE permission granted" : "MANAGE_EXTERNAL_STORAGE permission denied");
        call.resolve(ret);
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        boolean granted = hasStoragePermissions();
        ret.put("granted", granted);
        ret.put("message", granted ? "Storage permissions granted" : "Storage permissions not granted");
        call.resolve(ret);
    }

    @PermissionCallback
    private void storagePermissionCallback(PluginCall call) {
        JSObject ret = new JSObject();
        boolean granted = hasStoragePermissions();
        ret.put("granted", granted);
        ret.put("message", granted ? "Storage permissions granted" : "Storage permissions denied");
        call.resolve(ret);
    }

    private boolean hasStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 需要 MANAGE_EXTERNAL_STORAGE 权限
            return Environment.isExternalStorageManager();
        }

        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @PluginMethod
    public void listDirectory(PluginCall call) {
        String path = call.getString("path");
        Boolean showHidden = call.getBoolean("showHidden", false);
        String sortBy = call.getString("sortBy", "name");
        String sortOrder = call.getString("sortOrder", "asc");

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        try {
            JSObject result = implementation.listDirectory(path, showHidden, sortBy, sortOrder);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to list directory: " + e.getMessage());
        }
    }

    @PluginMethod
    public void createDirectory(PluginCall call) {
        String path = call.getString("path");
        Boolean recursive = call.getBoolean("recursive", false);

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        try {
            implementation.createDirectory(path, recursive);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to create directory: " + e.getMessage());
        }
    }

    @PluginMethod
    public void deleteDirectory(PluginCall call) {
        String path = call.getString("path");

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        try {
            implementation.deleteDirectory(path);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to delete directory: " + e.getMessage());
        }
    }

    @PluginMethod
    public void createFile(PluginCall call) {
        String path = call.getString("path");
        String content = call.getString("content", "");
        String encoding = call.getString("encoding", "utf8");

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        try {
            implementation.createFile(path, content, encoding);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to create file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void readFile(PluginCall call) {
        String path = call.getString("path");
        String encoding = call.getString("encoding", "utf8");

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        try {
            JSObject result = implementation.readFile(path, encoding);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to read file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void writeFile(PluginCall call) {
        String path = call.getString("path");
        String content = call.getString("content");
        String encoding = call.getString("encoding", "utf8");
        Boolean append = call.getBoolean("append", false);

        if (path == null || content == null) {
            call.reject("Path and content are required");
            return;
        }

        try {
            implementation.writeFile(path, content, encoding, append);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to write file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void deleteFile(PluginCall call) {
        String path = call.getString("path");

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        try {
            implementation.deleteFile(path);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to delete file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void renameFile(PluginCall call) {
        String path = call.getString("path");
        String newName = call.getString("newName");

        if (path == null || newName == null) {
            call.reject("Path and newName are required");
            return;
        }

        try {
            implementation.renameFile(path, newName);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to rename file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void moveFile(PluginCall call) {
        String sourcePath = call.getString("sourcePath");
        String destinationPath = call.getString("destinationPath");

        if (sourcePath == null || destinationPath == null) {
            call.reject("sourcePath and destinationPath are required");
            return;
        }

        try {
            implementation.moveFile(sourcePath, destinationPath);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to move file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void copyFile(PluginCall call) {
        String sourcePath = call.getString("sourcePath");
        String destinationPath = call.getString("destinationPath");
        Boolean overwrite = call.getBoolean("overwrite", false);

        if (sourcePath == null || destinationPath == null) {
            call.reject("sourcePath and destinationPath are required");
            return;
        }

        try {
            implementation.copyFile(sourcePath, destinationPath, overwrite);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to copy file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void openSystemFilePicker(PluginCall call) {
        String type = call.getString("type", "file");
        Boolean multiple = call.getBoolean("multiple", false);
        JSArray acceptArray = call.getArray("accept");
        String title = call.getString("title", "选择文件");

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

        startActivityForResult(call, intent, "filePickerResult");
    }

    @ActivityCallback
    private void filePickerResult(PluginCall call, ActivityResult result) {
        JSObject ret = new JSObject();
        JSArray files = new JSArray();
        JSArray directories = new JSArray();

        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            String requestType = call.getString("type", "file");

            if (data.getClipData() != null) {
                // 多选
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    JSObject fileInfo = getFileInfoFromUri(uri);
                    if (fileInfo != null) {
                        if ("directory".equals(fileInfo.getString("type"))) {
                            directories.put(fileInfo);
                        } else {
                            files.put(fileInfo);
                        }
                    }
                }
            } else if (data.getData() != null) {
                // 单选
                Uri uri = data.getData();
                JSObject fileInfo = getFileInfoFromUri(uri);
                if (fileInfo != null) {
                    if ("directory".equals(requestType) || "directory".equals(fileInfo.getString("type"))) {
                        directories.put(fileInfo);
                    } else {
                        files.put(fileInfo);
                    }
                }
            }

            ret.put("files", files);
            ret.put("directories", directories);
            ret.put("cancelled", false);
        } else {
            ret.put("files", files);
            ret.put("directories", directories);
            ret.put("cancelled", true);
        }

        call.resolve(ret);
    }

    @PluginMethod
    public void openSystemFileManager(PluginCall call) {
        String path = call.getString("path");

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (path != null && !path.isEmpty()) {
                File file = new File(path);
                if (file.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(getContext(),
                            getContext().getPackageName() + ".fileprovider", file);
                    } else {
                        uri = Uri.fromFile(file);
                    }
                    intent.setDataAndType(uri, "resource/folder");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            } else {
                // 打开默认文件管理器
                intent = getContext().getPackageManager().getLaunchIntentForPackage("com.android.documentsui");
                if (intent == null) {
                    // 备用方案
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setType("resource/folder");
                }
            }

            getActivity().startActivity(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to open file manager: " + e.getMessage());
        }
    }

    @PluginMethod
    public void openFileWithSystemApp(PluginCall call) {
        String filePath = call.getString("filePath");
        String mimeType = call.getString("mimeType");

        if (filePath == null) {
            call.reject("File path is required");
            return;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                call.reject("File does not exist: " + filePath);
                return;
            }

            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".fileprovider", file);
            } else {
                uri = Uri.fromFile(file);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (mimeType == null || mimeType.isEmpty()) {
                // 自动检测 MIME 类型
                String extension = getFileExtension(filePath);
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mimeType == null) {
                    mimeType = "*/*";
                }
            }

            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            getActivity().startActivity(Intent.createChooser(intent, "打开文件"));
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to open file: " + e.getMessage());
        }
    }

    private JSObject getFileInfoFromUri(Uri uri) {
        JSObject fileInfo = new JSObject();
        ContentResolver contentResolver = getContext().getContentResolver();

        try {
            // 获取文件基本信息
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
                boolean isDirectory = DocumentsContract.isDocumentUri(getContext(), uri) &&
                                    DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);

                // 尝试获取真实路径
                String realPath = getRealPathFromUri(uri);

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

    private String getRealPathFromUri(Uri uri) {
        try {
            // 对于 content:// URI，尝试获取真实路径
            if ("content".equals(uri.getScheme())) {
                if (DocumentsContract.isDocumentUri(getContext(), uri)) {
                    // 处理 Documents Provider
                    String docId = DocumentsContract.getDocumentId(uri);

                    if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                        // 外部存储 - 这是最常见的情况
                        return handleExternalStorageDocument(docId);
                    } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                        // 下载目录
                        return handleDownloadsDocument(docId);
                    } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                        // 媒体文件
                        return handleMediaDocument(docId);
                    }
                } else {
                    // 普通 content URI
                    return getDataColumn(uri, null, null);
                }
            } else if ("file".equals(uri.getScheme())) {
                // 文件 URI
                return uri.getPath();
            }
        } catch (Exception e) {
            // 忽略错误，返回 null
        }

        return null;
    }

    private String handleExternalStorageDocument(String docId) {
        String[] split = docId.split(":");
        if (split.length >= 2) {
            String type = split[0];
            String path = split[1];

            if ("primary".equalsIgnoreCase(type)) {
                // 主要存储 (内部存储)
                if (path.isEmpty()) {
                    return "/storage/emulated/0";
                } else {
                    return "/storage/emulated/0/" + path;
                }
            } else {
                // 外部 SD 卡或其他存储
                // 尝试一些常见的路径
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

                // 如果都不存在，返回最可能的路径
                return "/storage/" + type + "/" + path;
            }
        }
        return null;
    }

    private String handleDownloadsDocument(String docId) {
        if (docId.startsWith("raw:")) {
            return docId.replaceFirst("raw:", "");
        }

        // 对于数字 ID，尝试查询下载数据库
        try {
            Uri contentUri = Uri.parse("content://downloads/public_downloads");
            Uri uri = Uri.withAppendedPath(contentUri, docId);
            return getDataColumn(uri, null, null);
        } catch (Exception e) {
            // 如果查询失败，返回默认下载路径
            return "/storage/emulated/0/Download";
        }
    }

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

    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, null);
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

    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filePath.length() - 1) {
            return filePath.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }
}
