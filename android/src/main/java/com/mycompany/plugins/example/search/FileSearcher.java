package com.mycompany.plugins.example.search;

import android.content.Context;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.mycompany.plugins.example.core.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文件搜索模块
 * 负责文件名和内容搜索
 */
public class FileSearcher {
    private static final String TAG = "FileSearcher";
    private final Context context;

    public FileSearcher(Context context) {
        this.context = context;
    }

    /**
     * 搜索文件
     */
    public JSObject searchFiles(String directory, String query, String searchType, 
                                 String[] fileTypes, int maxResults, boolean recursive) throws Exception {
        File dir = new File(directory);
        
        if (!dir.exists() || !dir.isDirectory()) {
            throw new Exception("Invalid directory: " + directory);
        }

        List<File> results = new ArrayList<>();
        Pattern pattern = createSearchPattern(query);
        
        searchInDirectory(dir, pattern, searchType, fileTypes, maxResults, recursive, results);

        JSArray filesArray = new JSArray();
        for (File file : results) {
            filesArray.put(FileUtils.createFileInfo(file));
        }

        JSObject result = new JSObject();
        result.put("files", filesArray);
        result.put("totalFound", filesArray.length());

        return result;
    }

    /**
     * 在目录中搜索
     */
    private void searchInDirectory(File directory, Pattern pattern, String searchType,
                                    String[] fileTypes, int maxResults, boolean recursive,
                                    List<File> results) {
        if (results.size() >= maxResults) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (results.size() >= maxResults) {
                break;
            }

            // 跳过隐藏文件
            if (file.getName().startsWith(".")) {
                continue;
            }

            if (file.isDirectory()) {
                if (recursive) {
                    searchInDirectory(file, pattern, searchType, fileTypes, maxResults, true, results);
                }
            } else {
                // 检查文件类型过滤
                if (fileTypes != null && fileTypes.length > 0) {
                    String ext = FileUtils.getFileExtension(file.getName());
                    boolean matches = false;
                    for (String type : fileTypes) {
                        if (type.equalsIgnoreCase(ext) || type.equals("." + ext)) {
                            matches = true;
                            break;
                        }
                    }
                    if (!matches) {
                        continue;
                    }
                }

                // 根据搜索类型匹配
                boolean matched = false;
                switch (searchType) {
                    case "content":
                        matched = searchInFileContent(file, pattern);
                        break;
                    case "both":
                        matched = matchFileName(file, pattern) || searchInFileContent(file, pattern);
                        break;
                    case "name":
                    default:
                        matched = matchFileName(file, pattern);
                        break;
                }

                if (matched) {
                    results.add(file);
                }
            }
        }
    }

    /**
     * 匹配文件名
     */
    private boolean matchFileName(File file, Pattern pattern) {
        return pattern.matcher(file.getName()).find();
    }

    /**
     * 在文件内容中搜索
     */
    private boolean searchInFileContent(File file, Pattern pattern) {
        // 只搜索文本文件
        String mimeType = FileUtils.getMimeType(file.getName());
        if (!mimeType.startsWith("text/") && !isTextFile(file.getName())) {
            return false;
        }

        // 限制文件大小（避免搜索大文件）
        if (file.length() > 10 * 1024 * 1024) { // 10MB
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (pattern.matcher(line).find()) {
                    return true;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to read file for content search: " + file.getPath());
        }

        return false;
    }

    /**
     * 判断是否为文本文件
     */
    private boolean isTextFile(String fileName) {
        String ext = FileUtils.getFileExtension(fileName).toLowerCase();
        return ext.equals("txt") || ext.equals("md") || ext.equals("json") ||
               ext.equals("xml") || ext.equals("html") || ext.equals("css") ||
               ext.equals("js") || ext.equals("ts") || ext.equals("java") ||
               ext.equals("kt") || ext.equals("swift") || ext.equals("py") ||
               ext.equals("rb") || ext.equals("go") || ext.equals("rs") ||
               ext.equals("c") || ext.equals("cpp") || ext.equals("h") ||
               ext.equals("yml") || ext.equals("yaml") || ext.equals("ini") ||
               ext.equals("conf") || ext.equals("log") || ext.equals("sh");
    }

    /**
     * 创建搜索模式
     */
    private Pattern createSearchPattern(String query) {
        // 支持通配符 * 和 ?
        String regex = query
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".");
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
}
