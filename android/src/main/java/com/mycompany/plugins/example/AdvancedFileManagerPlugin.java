package com.mycompany.plugins.example;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResult;
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

import com.mycompany.plugins.example.core.FileOperations;
import com.mycompany.plugins.example.core.DirectoryOperations;
import com.mycompany.plugins.example.core.FileUtils;
import com.mycompany.plugins.example.permission.PermissionManager;
import com.mycompany.plugins.example.picker.SystemFilePicker;
import com.mycompany.plugins.example.search.FileSearcher;
import com.mycompany.plugins.example.ai.AIEditOperations;

import java.io.File;

/**
 * Advanced File Manager Plugin
 * 模块化架构的文件管理插件
 */
@CapacitorPlugin(
    name = "AdvancedFileManager",
    permissions = {
        @Permission(
            strings = { 
                android.Manifest.permission.READ_EXTERNAL_STORAGE, 
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE 
            },
            alias = "storage"
        )
    }
)
public class AdvancedFileManagerPlugin extends Plugin {

    // 模块实例
    private FileOperations fileOps;
    private DirectoryOperations dirOps;
    private PermissionManager permManager;
    private SystemFilePicker filePicker;
    private FileSearcher fileSearcher;
    private AIEditOperations aiEditOps;

    @Override
    public void load() {
        super.load();
        // 初始化各模块
        fileOps = new FileOperations(getContext());
        dirOps = new DirectoryOperations(getContext());
        permManager = new PermissionManager(this);
        filePicker = new SystemFilePicker(this);
        fileSearcher = new FileSearcher(getContext());
        aiEditOps = new AIEditOperations(getContext());
    }

    // ==================== 权限管理 ====================

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (permManager.hasStoragePermissions()) {
            call.resolve(permManager.createPermissionResult(true, "Storage permissions already granted"));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                permManager.requestManageExternalStoragePermission(call);
            } else {
                requestPermissionForAlias("storage", call, "storagePermissionCallback");
            }
        }
    }

    @ActivityCallback
    private void manageStoragePermissionResult(PluginCall call, ActivityResult result) {
        boolean granted = Environment.isExternalStorageManager();
        call.resolve(permManager.createPermissionResult(granted, 
            granted ? "MANAGE_EXTERNAL_STORAGE permission granted" : "MANAGE_EXTERNAL_STORAGE permission denied"));
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        permManager.checkPermissions(call);
    }

    @PermissionCallback
    private void storagePermissionCallback(PluginCall call) {
        boolean granted = permManager.hasStoragePermissions();
        call.resolve(permManager.createPermissionResult(granted,
            granted ? "Storage permissions granted" : "Storage permissions denied"));
    }

    // ==================== 目录操作 ====================

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
            JSObject result = dirOps.listDirectory(path, showHidden, sortBy, sortOrder);
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
            dirOps.createDirectory(path, recursive);
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
            dirOps.deleteDirectory(path);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to delete directory: " + e.getMessage());
        }
    }

    // ==================== 文件操作 ====================

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
            fileOps.createFile(path, content, encoding);
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
            JSObject result = fileOps.readFile(path, encoding);
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
            fileOps.writeFile(path, content, encoding, append);
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
            fileOps.deleteFile(path);
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
            fileOps.renameFile(path, newName);
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
            fileOps.moveFile(sourcePath, destinationPath);
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
            fileOps.copyFile(sourcePath, destinationPath, overwrite);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to copy file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getFileInfo(PluginCall call) {
        String path = call.getString("path");

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        try {
            JSObject result = fileOps.getFileInfo(path);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to get file info: " + e.getMessage());
        }
    }

    @PluginMethod
    public void exists(PluginCall call) {
        String path = call.getString("path");

        if (path == null) {
            call.reject("Path is required");
            return;
        }

        JSObject result = new JSObject();
        result.put("exists", fileOps.exists(path));
        call.resolve(result);
    }

    // ==================== 搜索 ====================

    @PluginMethod
    public void searchFiles(PluginCall call) {
        String directory = call.getString("directory");
        String query = call.getString("query");
        String searchType = call.getString("searchType", "name");
        JSArray fileTypesArray = call.getArray("fileTypes");
        Integer maxResults = call.getInt("maxResults", 100);
        Boolean recursive = call.getBoolean("recursive", true);

        if (directory == null || query == null) {
            call.reject("Directory and query are required");
            return;
        }

        try {
            String[] fileTypes = null;
            if (fileTypesArray != null && fileTypesArray.length() > 0) {
                fileTypes = new String[fileTypesArray.length()];
                for (int i = 0; i < fileTypesArray.length(); i++) {
                    fileTypes[i] = fileTypesArray.getString(i);
                }
            }

            JSObject result = fileSearcher.searchFiles(directory, query, searchType, 
                                                        fileTypes, maxResults, recursive);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to search files: " + e.getMessage());
        }
    }

    // ==================== 系统文件选择器 ====================

    @PluginMethod
    public void openSystemFilePicker(PluginCall call) {
        String type = call.getString("type", "file");
        Boolean multiple = call.getBoolean("multiple", false);
        JSArray acceptArray = call.getArray("accept");
        String title = call.getString("title", "选择文件");

        filePicker.openFilePicker(call, type, multiple, acceptArray, title);
    }

    @ActivityCallback
    private void filePickerResult(PluginCall call, ActivityResult result) {
        JSObject ret = new JSObject();
        JSArray files = new JSArray();
        JSArray directories = new JSArray();

        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            String requestType = call.getString("type", "file");

            // 处理多选
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    JSObject fileInfo = filePicker.getFileInfoFromUri(uri);
                    if (fileInfo != null) {
                        if ("directory".equals(fileInfo.getString("type"))) {
                            directories.put(fileInfo);
                        } else {
                            files.put(fileInfo);
                        }
                    }
                }
            } else if (data.getData() != null) {
                // 处理单选
                Uri uri = data.getData();
                JSObject fileInfo = filePicker.getFileInfoFromUri(uri);
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
        String path = call.getString("path", "/storage/emulated/0");

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(path);
            intent.setDataAndType(uri, "resource/folder");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                getActivity().startActivity(intent);
            } else {
                Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fallbackIntent.setType("*/*");
                fallbackIntent.addCategory(Intent.CATEGORY_OPENABLE);
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(Intent.createChooser(fallbackIntent, "打开文件管理器"));
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to open system file manager: " + e.getMessage());
        }
    }

    @PluginMethod
    public void openFileWithSystemApp(PluginCall call) {
        String filePath = call.getString("path");
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
            try {
                uri = FileProvider.getUriForFile(getContext(),
                        getContext().getPackageName() + ".fileprovider", file);
            } catch (Exception e) {
                uri = Uri.fromFile(file);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = FileUtils.getMimeType(filePath);
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

    // ==================== AI 编辑操作 ====================

    @PluginMethod
    public void readFileRange(PluginCall call) {
        String path = call.getString("path");
        Integer startLine = call.getInt("startLine");
        Integer endLine = call.getInt("endLine");

        if (path == null || startLine == null || endLine == null) {
            call.reject("path, startLine and endLine are required");
            return;
        }

        try {
            JSObject result = aiEditOps.readFileRange(path, startLine, endLine);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to read file range: " + e.getMessage());
        }
    }

    @PluginMethod
    public void insertContent(PluginCall call) {
        String path = call.getString("path");
        Integer line = call.getInt("line");
        String content = call.getString("content");

        if (path == null || line == null || content == null) {
            call.reject("path, line and content are required");
            return;
        }

        try {
            aiEditOps.insertContent(path, line, content);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to insert content: " + e.getMessage());
        }
    }

    @PluginMethod
    public void replaceInFile(PluginCall call) {
        String path = call.getString("path");
        String search = call.getString("search");
        String replace = call.getString("replace");
        Boolean isRegex = call.getBoolean("isRegex", false);
        Boolean replaceAll = call.getBoolean("replaceAll", true);
        Boolean caseSensitive = call.getBoolean("caseSensitive", true);

        if (path == null || search == null || replace == null) {
            call.reject("path, search and replace are required");
            return;
        }

        try {
            JSObject result = aiEditOps.replaceInFile(path, search, replace, 
                                                       isRegex, replaceAll, caseSensitive);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to replace in file: " + e.getMessage());
        }
    }

    @PluginMethod
    public void applyDiff(PluginCall call) {
        String path = call.getString("path");
        String diff = call.getString("diff");
        Boolean createBackup = call.getBoolean("createBackup", false);

        if (path == null || diff == null) {
            call.reject("path and diff are required");
            return;
        }

        try {
            JSObject result = aiEditOps.applyDiff(path, diff, createBackup);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to apply diff: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getFileHash(PluginCall call) {
        String path = call.getString("path");
        String algorithm = call.getString("algorithm", "md5");

        if (path == null) {
            call.reject("path is required");
            return;
        }

        try {
            JSObject result = aiEditOps.getFileHash(path, algorithm);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to get file hash: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getLineCount(PluginCall call) {
        String path = call.getString("path");

        if (path == null) {
            call.reject("path is required");
            return;
        }

        try {
            JSObject result = aiEditOps.getLineCount(path);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to get line count: " + e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", value);
        call.resolve(ret);
    }
}
