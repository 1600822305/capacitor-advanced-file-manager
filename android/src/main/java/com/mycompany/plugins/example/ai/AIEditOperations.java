package com.mycompany.plugins.example.ai;

import android.content.Context;
import android.util.Log;

import com.getcapacitor.JSObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 编辑操作模块
 * 提供 AI 代码编辑所需的高级文件操作功能
 */
public class AIEditOperations {
    private static final String TAG = "AIEditOperations";
    private final Context context;

    public AIEditOperations(Context context) {
        this.context = context;
    }

    /**
     * 读取文件指定行范围
     */
    public JSObject readFileRange(String path, int startLine, int endLine) throws Exception {
        File file = new File(path);
        
        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        List<String> allLines = readAllLines(file);
        int totalLines = allLines.size();

        // 边界检查
        startLine = Math.max(1, startLine);
        endLine = Math.min(totalLines, endLine);

        if (startLine > endLine || startLine > totalLines) {
            throw new Exception("Invalid line range: " + startLine + "-" + endLine);
        }

        // 提取指定范围的行 (转换为 0-based index)
        StringBuilder content = new StringBuilder();
        for (int i = startLine - 1; i < endLine; i++) {
            content.append(allLines.get(i));
            if (i < endLine - 1) {
                content.append("\n");
            }
        }

        String contentStr = content.toString();
        String rangeHash = calculateHash(contentStr, "md5");

        JSObject result = new JSObject();
        result.put("content", contentStr);
        result.put("totalLines", totalLines);
        result.put("startLine", startLine);
        result.put("endLine", endLine);
        result.put("rangeHash", rangeHash);

        return result;
    }

    /**
     * 在指定行插入内容
     */
    public void insertContent(String path, int line, String content) throws Exception {
        File file = new File(path);
        
        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        List<String> lines = readAllLines(file);
        
        // 边界检查
        int insertIndex = Math.max(0, Math.min(lines.size(), line - 1));

        // 将要插入的内容按行分割
        String[] newLines = content.split("\n", -1);
        
        // 插入新行
        for (int i = 0; i < newLines.length; i++) {
            lines.add(insertIndex + i, newLines[i]);
        }

        // 写回文件
        writeAllLines(file, lines);
    }

    /**
     * 查找并替换文件内容
     */
    public JSObject replaceInFile(String path, String search, String replace, 
                                   boolean isRegex, boolean replaceAll, 
                                   boolean caseSensitive) throws Exception {
        File file = new File(path);
        
        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        String content = readFileContent(file);
        String newContent;
        int replacements = 0;

        if (isRegex) {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(search, flags);
            Matcher matcher = pattern.matcher(content);
            
            if (replaceAll) {
                // 计算替换次数
                Matcher countMatcher = pattern.matcher(content);
                while (countMatcher.find()) {
                    replacements++;
                }
                newContent = matcher.replaceAll(replace);
            } else {
                if (matcher.find()) {
                    replacements = 1;
                }
                newContent = matcher.replaceFirst(replace);
            }
        } else {
            if (replaceAll) {
                if (caseSensitive) {
                    // 计算替换次数
                    int idx = 0;
                    while ((idx = content.indexOf(search, idx)) != -1) {
                        replacements++;
                        idx += search.length();
                    }
                    newContent = content.replace(search, replace);
                } else {
                    String lowerContent = content.toLowerCase();
                    String lowerSearch = search.toLowerCase();
                    int idx = 0;
                    while ((idx = lowerContent.indexOf(lowerSearch, idx)) != -1) {
                        replacements++;
                        idx += search.length();
                    }
                    newContent = content.replaceAll("(?i)" + Pattern.quote(search), 
                                                     Matcher.quoteReplacement(replace));
                }
            } else {
                int idx = caseSensitive ? content.indexOf(search) : 
                          content.toLowerCase().indexOf(search.toLowerCase());
                if (idx != -1) {
                    replacements = 1;
                    newContent = content.substring(0, idx) + replace + 
                                content.substring(idx + search.length());
                } else {
                    newContent = content;
                }
            }
        }

        boolean modified = !content.equals(newContent);
        if (modified) {
            writeFileContent(file, newContent);
        }

        JSObject result = new JSObject();
        result.put("replacements", replacements);
        result.put("modified", modified);

        return result;
    }

    /**
     * 应用 Unified Diff 补丁
     */
    public JSObject applyDiff(String path, String diff, boolean createBackup) throws Exception {
        File file = new File(path);
        
        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        String backupPath = null;
        if (createBackup) {
            backupPath = path + ".bak";
            copyFile(file, new File(backupPath));
        }

        List<String> lines = readAllLines(file);
        List<String> diffLines = parseDiffLines(diff);

        int linesAdded = 0;
        int linesDeleted = 0;
        int linesChanged = 0;

        // 解析并应用 diff
        int currentLine = 0;
        int offset = 0;

        for (int i = 0; i < diffLines.size(); i++) {
            String diffLine = diffLines.get(i);

            if (diffLine.startsWith("@@")) {
                // 解析 hunk header: @@ -start,count +start,count @@
                Pattern pattern = Pattern.compile("@@ -(\\d+)(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@");
                Matcher matcher = pattern.matcher(diffLine);
                if (matcher.find()) {
                    currentLine = Integer.parseInt(matcher.group(1)) - 1 + offset;
                }
            } else if (diffLine.startsWith("-") && !diffLine.startsWith("---")) {
                // 删除行
                if (currentLine < lines.size()) {
                    lines.remove(currentLine);
                    linesDeleted++;
                    offset--;
                }
            } else if (diffLine.startsWith("+") && !diffLine.startsWith("+++")) {
                // 添加行
                String newLine = diffLine.substring(1);
                lines.add(currentLine, newLine);
                linesAdded++;
                currentLine++;
                offset++;
            } else if (!diffLine.startsWith("\\") && !diffLine.startsWith("---") && 
                       !diffLine.startsWith("+++") && !diffLine.startsWith("diff")) {
                // 上下文行，移动到下一行
                currentLine++;
            }
        }

        linesChanged = linesAdded + linesDeleted;

        // 写回文件
        writeAllLines(file, lines);

        JSObject result = new JSObject();
        result.put("success", true);
        result.put("linesChanged", linesChanged);
        result.put("linesAdded", linesAdded);
        result.put("linesDeleted", linesDeleted);
        if (backupPath != null) {
            result.put("backupPath", backupPath);
        }

        return result;
    }

    /**
     * 获取文件哈希值
     */
    public JSObject getFileHash(String path, String algorithm) throws Exception {
        File file = new File(path);
        
        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        String content = readFileContent(file);
        String hash = calculateHash(content, algorithm);

        JSObject result = new JSObject();
        result.put("hash", hash);
        result.put("algorithm", algorithm);

        return result;
    }

    /**
     * 获取文件行数
     */
    public JSObject getLineCount(String path) throws Exception {
        File file = new File(path);
        
        if (!file.exists()) {
            throw new Exception("File does not exist: " + path);
        }

        List<String> lines = readAllLines(file);

        JSObject result = new JSObject();
        result.put("lines", lines.size());

        return result;
    }

    // ============ 辅助方法 ============

    private List<String> readAllLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void writeAllLines(File file, List<String> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                if (i < lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
        }
        return content.toString();
    }

    private void writeFileContent(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }

    private String calculateHash(String content, String algorithm) throws Exception {
        try {
            MessageDigest md;
            if ("sha256".equalsIgnoreCase(algorithm)) {
                md = MessageDigest.getInstance("SHA-256");
            } else {
                md = MessageDigest.getInstance("MD5");
            }
            
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Hash algorithm not supported: " + algorithm);
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    private List<String> parseDiffLines(String diff) {
        List<String> lines = new ArrayList<>();
        for (String line : diff.split("\n")) {
            lines.add(line);
        }
        return lines;
    }
}
