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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件搜索模块
 * 负责文件名和内容搜索
 */
public class FileSearcher {
    private static final String TAG = "FileSearcher";
    private final Context context;
    
    // 默认配置
    private static final int DEFAULT_MAX_FILES = 100;
    private static final int DEFAULT_MAX_FILE_SIZE = 500 * 1024; // 500KB
    private static final int DEFAULT_MAX_MATCHES_PER_FILE = 10;
    private static final int DEFAULT_CONTEXT_LENGTH = 40;
    private static final int DEFAULT_MAX_DEPTH = 5;

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
    
    // ==================== 原生内容搜索 API ====================
    
    /**
     * 原生层内容搜索
     * 在原生层执行搜索，只返回匹配结果，避免 OOM
     */
    public JSObject searchContent(String directory, String keyword,
                                   boolean caseSensitive, String[] fileExtensions,
                                   int maxFiles, int maxFileSize, int maxMatchesPerFile,
                                   int contextLength, int maxDepth, boolean recursive) throws Exception {
        long startTime = System.currentTimeMillis();
        
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new Exception("Invalid directory: " + directory);
        }
        
        // 使用默认值
        if (maxFiles <= 0) maxFiles = DEFAULT_MAX_FILES;
        if (maxFileSize <= 0) maxFileSize = DEFAULT_MAX_FILE_SIZE;
        if (maxMatchesPerFile <= 0) maxMatchesPerFile = DEFAULT_MAX_MATCHES_PER_FILE;
        if (contextLength <= 0) contextLength = DEFAULT_CONTEXT_LENGTH;
        if (maxDepth <= 0) maxDepth = DEFAULT_MAX_DEPTH;
        
        // 创建搜索模式
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), flags);
        
        // 收集结果
        List<JSObject> results = new ArrayList<>();
        int[] skippedCount = {0};
        int[] totalMatches = {0};
        
        // 执行搜索
        searchContentInDirectory(dir, keyword, pattern, fileExtensions, 
                                  maxFiles, maxFileSize, maxMatchesPerFile, 
                                  contextLength, maxDepth, recursive, 0,
                                  results, skippedCount, totalMatches);
        
        // 按评分排序
        Collections.sort(results, new Comparator<JSObject>() {
            @Override
            public int compare(JSObject a, JSObject b) {
                return Integer.compare(b.optInt("score", 0), a.optInt("score", 0));
            }
        });
        
        long duration = System.currentTimeMillis() - startTime;
        
        // 构建结果
        JSArray resultsArray = new JSArray();
        for (JSObject r : results) {
            resultsArray.put(r);
        }
        
        JSObject result = new JSObject();
        result.put("results", resultsArray);
        result.put("totalFiles", results.size());
        result.put("totalMatches", totalMatches[0]);
        result.put("duration", duration);
        result.put("skippedFiles", skippedCount[0]);
        
        Log.d(TAG, "searchContent completed: " + results.size() + " files, " + 
              totalMatches[0] + " matches in " + duration + "ms");
        
        return result;
    }
    
    /**
     * 在目录中递归搜索内容
     */
    private void searchContentInDirectory(File directory, String keyword, Pattern pattern,
                                           String[] fileExtensions, int maxFiles, int maxFileSize,
                                           int maxMatchesPerFile, int contextLength, int maxDepth,
                                           boolean recursive, int currentDepth,
                                           List<JSObject> results, int[] skippedCount, int[] totalMatches) {
        // 检查深度限制
        if (currentDepth >= maxDepth) {
            return;
        }
        
        // 检查结果数量限制
        if (results.size() >= maxFiles) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (results.size() >= maxFiles) {
                break;
            }
            
            // 跳过隐藏文件
            if (file.getName().startsWith(".")) {
                continue;
            }
            
            if (file.isDirectory()) {
                if (recursive) {
                    searchContentInDirectory(file, keyword, pattern, fileExtensions,
                                              maxFiles, maxFileSize, maxMatchesPerFile,
                                              contextLength, maxDepth, recursive, currentDepth + 1,
                                              results, skippedCount, totalMatches);
                }
            } else {
                // 检查文件扩展名
                if (fileExtensions != null && fileExtensions.length > 0) {
                    String ext = FileUtils.getFileExtension(file.getName());
                    boolean matches = false;
                    for (String filterExt : fileExtensions) {
                        String cleanExt = filterExt.startsWith(".") ? filterExt.substring(1) : filterExt;
                        if (cleanExt.equalsIgnoreCase(ext)) {
                            matches = true;
                            break;
                        }
                    }
                    if (!matches) {
                        continue;
                    }
                }
                
                // 检查是否为文本文件
                if (!isTextFile(file.getName())) {
                    continue;
                }
                
                // 检查文件大小
                if (file.length() > maxFileSize) {
                    skippedCount[0]++;
                    Log.d(TAG, "Skipped large file: " + file.getPath() + " (" + file.length() + " bytes)");
                    continue;
                }
                
                // 搜索文件内容
                JSObject fileResult = searchInSingleFile(file, keyword, pattern, 
                                                          maxMatchesPerFile, contextLength);
                if (fileResult != null) {
                    results.add(fileResult);
                    totalMatches[0] += fileResult.optInt("matchCount", 0);
                }
            }
        }
    }
    
    /**
     * 在单个文件中搜索
     */
    private JSObject searchInSingleFile(File file, String keyword, Pattern pattern,
                                         int maxMatchesPerFile, int contextLength) {
        boolean nameMatch = file.getName().toLowerCase().contains(keyword.toLowerCase());
        List<JSObject> matches = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null && matches.size() < maxMatchesPerFile) {
                lineNumber++;
                Matcher matcher = pattern.matcher(line);
                
                while (matcher.find() && matches.size() < maxMatchesPerFile) {
                    int matchStart = matcher.start();
                    int matchEnd = matcher.end();
                    
                    // 构建上下文
                    int contextStart = Math.max(0, matchStart - 2);
                    int contextEnd = Math.min(line.length(), matchEnd + contextLength);
                    
                    String prefix = contextStart > 0 ? "..." : "";
                    String context = prefix + line.substring(contextStart, contextEnd);
                    
                    // 调整匹配位置（考虑前缀）
                    int adjustedStart = matchStart - contextStart + prefix.length();
                    int adjustedEnd = adjustedStart + (matchEnd - matchStart);
                    
                    JSObject match = new JSObject();
                    match.put("lineNumber", lineNumber);
                    match.put("lineContent", line.length() > 200 ? line.substring(0, 200) + "..." : line);
                    match.put("context", context);
                    match.put("matchStart", adjustedStart);
                    match.put("matchEnd", adjustedEnd);
                    
                    matches.add(match);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to search in file: " + file.getPath() + " - " + e.getMessage());
            return null;
        }
        
        // 如果没有匹配，但文件名匹配
        if (matches.isEmpty() && !nameMatch) {
            return null;
        }
        
        // 计算评分
        int score = calculateScore(file.getName(), keyword, matches.size(), nameMatch);
        
        // 构建结果
        JSObject result = new JSObject();
        result.put("path", file.getAbsolutePath());
        result.put("name", file.getName());
        result.put("matchType", nameMatch && !matches.isEmpty() ? "both" : 
                               (nameMatch ? "filename" : "content"));
        result.put("score", score);
        result.put("matchCount", matches.size());
        
        JSArray matchesArray = new JSArray();
        for (JSObject m : matches) {
            matchesArray.put(m);
        }
        result.put("matches", matchesArray);
        
        return result;
    }
    
    /**
     * 计算相关性评分
     */
    private int calculateScore(String fileName, String keyword, int matchCount, boolean nameMatch) {
        int score = 0;
        String lowerName = fileName.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        
        // 完全匹配文件名（最高权重）
        if (lowerName.equals(lowerKeyword) || lowerName.equals(lowerKeyword + ".md")) {
            score += 200;
        }
        // 文件名包含关键词
        else if (lowerName.contains(lowerKeyword)) {
            score += 100;
        }
        
        // 内容匹配数量
        score += Math.min(matchCount * 2, 50);
        
        // 既匹配文件名又匹配内容
        if (nameMatch && matchCount > 0) {
            score += 50;
        }
        
        return score;
    }
}
