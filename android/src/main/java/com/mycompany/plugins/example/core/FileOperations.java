package com.mycompany.plugins.example.core;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 文件操作模块
 * 负责文件的 CRUD 操作：创建、读取、写入、删除、重命名、移动、复制
 */
public class FileOperations {
    private static final String TAG = "FileOperations";
    private final Context context;

    public FileOperations(Context context) {
        this.context = context;
    }

    /**
     * 创建文件
     */
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

    /**
     * 读取文件
     */
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

    /**
     * 写入文件
     */
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

    /**
     * 删除文件
     */
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

    /**
     * 重命名文件或目录
     */
    public void renameFile(String path, String newName) throws Exception {
        File file = new File(path);

        if (!file.exists()) {
            throw new Exception("File or directory does not exist: " + path);
        }

        File parentDir = file.getParentFile();
        if (parentDir == null) {
            throw new Exception("Cannot get parent directory for: " + path);
        }

        File newFile = new File(parentDir, newName);

        if (newFile.exists()) {
            throw new Exception("A file or directory with that name already exists: " + newFile.getPath());
        }

        if (!file.renameTo(newFile)) {
            throw new Exception("Failed to rename: " + path + " to " + newName);
        }
    }

    /**
     * 移动文件
     */
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
            if (!FileUtils.deleteRecursively(sourceFile)) {
                throw new Exception("Failed to delete source after copy");
            }
        }
    }

    /**
     * 复制文件
     */
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

    /**
     * 获取文件信息
     */
    public JSObject getFileInfo(String path) throws Exception {
        File file = new File(path);

        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        return FileUtils.createFileInfo(file);
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 内部复制方法
     */
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
