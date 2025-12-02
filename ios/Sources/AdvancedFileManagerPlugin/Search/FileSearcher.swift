import Foundation

/// 文件搜索模块
public class FileSearcher {
    private let fileManager = FileManager.default
    
    /// 搜索文件
    public func searchFiles(directory: String, query: String, searchType: String,
                           fileTypes: [String]?, maxResults: Int, recursive: Bool) throws -> [String: Any] {
        let url = URL(fileURLWithPath: directory)
        
        guard fileManager.fileExists(atPath: directory) else {
            throw NSError(domain: "FileSearcher", code: 404, userInfo: [NSLocalizedDescriptionKey: "Directory does not exist: \(directory)"])
        }
        
        var results: [[String: Any]] = []
        let pattern = createSearchPattern(from: query)
        
        try searchInDirectory(url: url, pattern: pattern, searchType: searchType,
                             fileTypes: fileTypes, maxResults: maxResults, 
                             recursive: recursive, results: &results)
        
        return [
            "files": results,
            "totalFound": results.count
        ]
    }
    
    private func searchInDirectory(url: URL, pattern: NSRegularExpression, searchType: String,
                                   fileTypes: [String]?, maxResults: Int, recursive: Bool,
                                   results: inout [[String: Any]]) throws {
        if results.count >= maxResults {
            return
        }
        
        let contents = try fileManager.contentsOfDirectory(at: url, includingPropertiesForKeys: [
            .fileSizeKey,
            .contentModificationDateKey,
            .creationDateKey,
            .isDirectoryKey,
            .isHiddenKey
        ], options: [.skipsHiddenFiles])
        
        for fileURL in contents {
            if results.count >= maxResults {
                break
            }
            
            let resourceValues = try fileURL.resourceValues(forKeys: [
                .fileSizeKey,
                .contentModificationDateKey,
                .creationDateKey,
                .isDirectoryKey,
                .isHiddenKey
            ])
            
            let isDirectory = resourceValues.isDirectory ?? false
            
            if isDirectory {
                if recursive {
                    try searchInDirectory(url: fileURL, pattern: pattern, searchType: searchType,
                                         fileTypes: fileTypes, maxResults: maxResults,
                                         recursive: true, results: &results)
                }
            } else {
                // 检查文件类型过滤
                if let types = fileTypes, !types.isEmpty {
                    let ext = FileUtils.getFileExtension(fileURL.lastPathComponent)
                    let matches = types.contains { type in
                        type.lowercased() == ext || type.lowercased() == ".\(ext)"
                    }
                    if !matches {
                        continue
                    }
                }
                
                // 根据搜索类型匹配
                var matched = false
                switch searchType {
                case "content":
                    matched = searchInFileContent(fileURL: fileURL, pattern: pattern)
                case "both":
                    matched = matchFileName(fileURL: fileURL, pattern: pattern) || 
                              searchInFileContent(fileURL: fileURL, pattern: pattern)
                default: // "name"
                    matched = matchFileName(fileURL: fileURL, pattern: pattern)
                }
                
                if matched {
                    let fileInfo = FileUtils.createFileInfo(from: fileURL, resourceValues: resourceValues)
                    results.append(fileInfo)
                }
            }
        }
    }
    
    private func matchFileName(fileURL: URL, pattern: NSRegularExpression) -> Bool {
        let fileName = fileURL.lastPathComponent
        let range = NSRange(fileName.startIndex..., in: fileName)
        return pattern.firstMatch(in: fileName, options: [], range: range) != nil
    }
    
    private func searchInFileContent(fileURL: URL, pattern: NSRegularExpression) -> Bool {
        // 只搜索文本文件
        let mimeType = FileUtils.getMimeType(for: fileURL.lastPathComponent)
        guard mimeType.hasPrefix("text/") || isTextFile(fileURL.lastPathComponent) else {
            return false
        }
        
        // 限制文件大小
        do {
            let attributes = try fileManager.attributesOfItem(atPath: fileURL.path)
            let fileSize = attributes[.size] as? Int64 ?? 0
            if fileSize > 10 * 1024 * 1024 { // 10MB
                return false
            }
            
            let content = try String(contentsOf: fileURL, encoding: .utf8)
            let range = NSRange(content.startIndex..., in: content)
            return pattern.firstMatch(in: content, options: [], range: range) != nil
        } catch {
            return false
        }
    }
    
    private func isTextFile(_ fileName: String) -> Bool {
        let ext = FileUtils.getFileExtension(fileName)
        let textExtensions = ["txt", "md", "json", "xml", "html", "css", "js", "ts",
                             "java", "kt", "swift", "py", "rb", "go", "rs", "c", "cpp",
                             "h", "yml", "yaml", "ini", "conf", "log", "sh"]
        return textExtensions.contains(ext)
    }
    
    private func createSearchPattern(from query: String) -> NSRegularExpression {
        // 转换通配符为正则表达式
        var pattern = NSRegularExpression.escapedPattern(for: query)
        pattern = pattern.replacingOccurrences(of: "\\*", with: ".*")
        pattern = pattern.replacingOccurrences(of: "\\?", with: ".")
        
        do {
            return try NSRegularExpression(pattern: pattern, options: .caseInsensitive)
        } catch {
            // 如果正则表达式无效，使用简单的包含匹配
            let escapedQuery = NSRegularExpression.escapedPattern(for: query)
            return try! NSRegularExpression(pattern: escapedQuery, options: .caseInsensitive)
        }
    }
}
