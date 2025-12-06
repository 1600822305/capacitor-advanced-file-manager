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
    
    // MARK: - 原生内容搜索 API
    
    /// 原生层内容搜索，避免 OOM
    public func searchContent(directory: String, keyword: String, caseSensitive: Bool,
                              fileExtensions: [String]?, maxFiles: Int, maxFileSize: Int,
                              maxMatchesPerFile: Int, contextLength: Int, maxDepth: Int,
                              recursive: Bool) throws -> [String: Any] {
        let startTime = Date()
        let url = URL(fileURLWithPath: directory)
        
        guard fileManager.fileExists(atPath: directory) else {
            throw NSError(domain: "FileSearcher", code: 404, 
                         userInfo: [NSLocalizedDescriptionKey: "Directory does not exist: \(directory)"])
        }
        
        // 使用默认值
        let effectiveMaxFiles = maxFiles > 0 ? maxFiles : 100
        let effectiveMaxFileSize = maxFileSize > 0 ? maxFileSize : 500 * 1024
        let effectiveMaxMatchesPerFile = maxMatchesPerFile > 0 ? maxMatchesPerFile : 10
        let effectiveContextLength = contextLength > 0 ? contextLength : 40
        let effectiveMaxDepth = maxDepth > 0 ? maxDepth : 5
        
        // 创建搜索模式
        let options: NSRegularExpression.Options = caseSensitive ? [] : .caseInsensitive
        let pattern = try NSRegularExpression(pattern: NSRegularExpression.escapedPattern(for: keyword), options: options)
        
        var results: [[String: Any]] = []
        var skippedCount = 0
        var totalMatches = 0
        
        try searchContentInDirectory(
            url: url, keyword: keyword, pattern: pattern, fileExtensions: fileExtensions,
            maxFiles: effectiveMaxFiles, maxFileSize: effectiveMaxFileSize,
            maxMatchesPerFile: effectiveMaxMatchesPerFile, contextLength: effectiveContextLength,
            maxDepth: effectiveMaxDepth, recursive: recursive, currentDepth: 0,
            results: &results, skippedCount: &skippedCount, totalMatches: &totalMatches
        )
        
        // 按评分排序
        results.sort { ($0["score"] as? Int ?? 0) > ($1["score"] as? Int ?? 0) }
        
        let duration = Int(Date().timeIntervalSince(startTime) * 1000)
        
        return [
            "results": results,
            "totalFiles": results.count,
            "totalMatches": totalMatches,
            "duration": duration,
            "skippedFiles": skippedCount
        ]
    }
    
    private func searchContentInDirectory(url: URL, keyword: String, pattern: NSRegularExpression,
                                          fileExtensions: [String]?, maxFiles: Int, maxFileSize: Int,
                                          maxMatchesPerFile: Int, contextLength: Int, maxDepth: Int,
                                          recursive: Bool, currentDepth: Int,
                                          results: inout [[String: Any]], skippedCount: inout Int,
                                          totalMatches: inout Int) throws {
        // 检查深度限制
        if currentDepth >= maxDepth {
            return
        }
        
        // 检查结果数量限制
        if results.count >= maxFiles {
            return
        }
        
        let contents = try fileManager.contentsOfDirectory(at: url, includingPropertiesForKeys: [
            .fileSizeKey, .isDirectoryKey
        ], options: [.skipsHiddenFiles])
        
        for fileURL in contents {
            if results.count >= maxFiles {
                break
            }
            
            let resourceValues = try fileURL.resourceValues(forKeys: [.fileSizeKey, .isDirectoryKey])
            let isDirectory = resourceValues.isDirectory ?? false
            
            if isDirectory {
                if recursive {
                    try searchContentInDirectory(
                        url: fileURL, keyword: keyword, pattern: pattern, fileExtensions: fileExtensions,
                        maxFiles: maxFiles, maxFileSize: maxFileSize, maxMatchesPerFile: maxMatchesPerFile,
                        contextLength: contextLength, maxDepth: maxDepth, recursive: recursive,
                        currentDepth: currentDepth + 1, results: &results, skippedCount: &skippedCount,
                        totalMatches: &totalMatches
                    )
                }
            } else {
                // 检查文件扩展名
                if let extensions = fileExtensions, !extensions.isEmpty {
                    let ext = FileUtils.getFileExtension(fileURL.lastPathComponent)
                    let matches = extensions.contains { filterExt in
                        let cleanExt = filterExt.hasPrefix(".") ? String(filterExt.dropFirst()) : filterExt
                        return cleanExt.lowercased() == ext.lowercased()
                    }
                    if !matches {
                        continue
                    }
                }
                
                // 检查是否为文本文件
                if !isTextFile(fileURL.lastPathComponent) {
                    continue
                }
                
                // 检查文件大小
                let fileSize = resourceValues.fileSize ?? 0
                if fileSize > maxFileSize {
                    skippedCount += 1
                    continue
                }
                
                // 搜索文件内容
                if let fileResult = searchInSingleFile(fileURL: fileURL, keyword: keyword, pattern: pattern,
                                                       maxMatchesPerFile: maxMatchesPerFile,
                                                       contextLength: contextLength) {
                    results.append(fileResult)
                    totalMatches += fileResult["matchCount"] as? Int ?? 0
                }
            }
        }
    }
    
    private func searchInSingleFile(fileURL: URL, keyword: String, pattern: NSRegularExpression,
                                    maxMatchesPerFile: Int, contextLength: Int) -> [String: Any]? {
        let fileName = fileURL.lastPathComponent
        let nameMatch = fileName.lowercased().contains(keyword.lowercased())
        var matches: [[String: Any]] = []
        
        do {
            let content = try String(contentsOf: fileURL, encoding: .utf8)
            let lines = content.components(separatedBy: .newlines)
            
            for (index, line) in lines.enumerated() {
                if matches.count >= maxMatchesPerFile {
                    break
                }
                
                let range = NSRange(line.startIndex..., in: line)
                let matchResults = pattern.matches(in: line, options: [], range: range)
                
                for matchResult in matchResults {
                    if matches.count >= maxMatchesPerFile {
                        break
                    }
                    
                    guard let matchRange = Range(matchResult.range, in: line) else { continue }
                    
                    let matchStart = line.distance(from: line.startIndex, to: matchRange.lowerBound)
                    let matchEnd = line.distance(from: line.startIndex, to: matchRange.upperBound)
                    
                    // 构建上下文
                    let contextStart = max(0, matchStart - 2)
                    let contextEnd = min(line.count, matchEnd + contextLength)
                    
                    let startIndex = line.index(line.startIndex, offsetBy: contextStart)
                    let endIndex = line.index(line.startIndex, offsetBy: contextEnd)
                    
                    let prefix = contextStart > 0 ? "..." : ""
                    let context = prefix + String(line[startIndex..<endIndex])
                    
                    let adjustedStart = matchStart - contextStart + prefix.count
                    let adjustedEnd = adjustedStart + (matchEnd - matchStart)
                    
                    let lineContent = line.count > 200 ? String(line.prefix(200)) + "..." : line
                    
                    matches.append([
                        "lineNumber": index + 1,
                        "lineContent": lineContent,
                        "context": context,
                        "matchStart": adjustedStart,
                        "matchEnd": adjustedEnd
                    ])
                }
            }
        } catch {
            return nil
        }
        
        // 如果没有匹配且文件名也不匹配
        if matches.isEmpty && !nameMatch {
            return nil
        }
        
        // 计算评分
        let score = calculateScore(fileName: fileName, keyword: keyword, matchCount: matches.count, nameMatch: nameMatch)
        
        let matchType: String
        if nameMatch && !matches.isEmpty {
            matchType = "both"
        } else if nameMatch {
            matchType = "filename"
        } else {
            matchType = "content"
        }
        
        return [
            "path": fileURL.path,
            "name": fileName,
            "matchType": matchType,
            "score": score,
            "matchCount": matches.count,
            "matches": matches
        ]
    }
    
    private func calculateScore(fileName: String, keyword: String, matchCount: Int, nameMatch: Bool) -> Int {
        var score = 0
        let lowerName = fileName.lowercased()
        let lowerKeyword = keyword.lowercased()
        
        // 完全匹配文件名
        if lowerName == lowerKeyword || lowerName == "\(lowerKeyword).md" {
            score += 200
        } else if lowerName.contains(lowerKeyword) {
            score += 100
        }
        
        // 内容匹配数量
        score += min(matchCount * 2, 50)
        
        // 既匹配文件名又匹配内容
        if nameMatch && matchCount > 0 {
            score += 50
        }
        
        return score
    }
}
