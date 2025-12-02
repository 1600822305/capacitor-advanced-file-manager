package com.mycompany.plugins.example.core;

import android.webkit.MimeTypeMap;

import com.getcapacitor.JSObject;

import java.io.File;

/**
 * 文件工具类
 * 提供通用的文件操作工具方法
 */
public class FileUtils {

    /**
     * 递归删除文件或目录
     */
    public static boolean deleteRecursively(File file) {
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

    /**
     * 创建文件信息对象
     */
    public static JSObject createFileInfo(File file) {
        JSObject fileInfo = new JSObject();
        fileInfo.put("name", file.getName());
        fileInfo.put("path", file.getAbsolutePath());
        fileInfo.put("size", file.length());
        fileInfo.put("type", file.isDirectory() ? "directory" : "file");
        fileInfo.put("mtime", file.lastModified());
        fileInfo.put("ctime", file.lastModified()); // Java无法获取创建时间，使用修改时间
        fileInfo.put("isHidden", file.isHidden());
        
        if (!file.isDirectory()) {
            String mimeType = getMimeType(file.getName());
            fileInfo.put("mimeType", mimeType);
        }
        
        return fileInfo;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filePath.length() - 1) {
            return filePath.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 获取 MIME 类型
     */
    public static String getMimeType(String fileName) {
        String extension = getFileExtension(fileName);
        if (extension.isEmpty()) {
            return "application/octet-stream";
        }
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    /**
     * 确保目录存在
     */
    public static boolean ensureDirectoryExists(File directory) {
        if (directory.exists()) {
            return directory.isDirectory();
        }
        return directory.mkdirs();
    }

    /**
     * 获取可读的文件大小
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
