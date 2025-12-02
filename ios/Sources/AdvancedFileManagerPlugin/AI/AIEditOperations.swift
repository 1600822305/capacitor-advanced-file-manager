import Foundation
import CommonCrypto

/// AI 编辑操作模块
/// 提供 AI 代码编辑所需的高级文件操作功能
public class AIEditOperations {
    private let fileManager = FileManager.default
    
    // MARK: - Read File Range
    
    /// 读取文件指定行范围
    public func readFileRange(path: String, startLine: Int, endLine: Int) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AIEditOperations", code: 404, 
                         userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        let allLines = try readAllLines(path: path)
        let totalLines = allLines.count
        
        // 边界检查
        let safeStartLine = max(1, startLine)
        let safeEndLine = min(totalLines, endLine)
        
        guard safeStartLine <= safeEndLine && safeStartLine <= totalLines else {
            throw NSError(domain: "AIEditOperations", code: 400,
                         userInfo: [NSLocalizedDescriptionKey: "Invalid line range: \(startLine)-\(endLine)"])
        }
        
        // 提取指定范围的行 (转换为 0-based index)
        let rangeLines = Array(allLines[(safeStartLine - 1)..<safeEndLine])
        let content = rangeLines.joined(separator: "\n")
        let rangeHash = calculateHash(content: content, algorithm: "md5")
        
        return [
            "content": content,
            "totalLines": totalLines,
            "startLine": safeStartLine,
            "endLine": safeEndLine,
            "rangeHash": rangeHash
        ]
    }
    
    // MARK: - Insert Content
    
    /// 在指定行插入内容
    public func insertContent(path: String, line: Int, content: String) throws {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AIEditOperations", code: 404,
                         userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        var lines = try readAllLines(path: path)
        
        // 边界检查
        let insertIndex = max(0, min(lines.count, line - 1))
        
        // 将要插入的内容按行分割
        let newLines = content.components(separatedBy: "\n")
        
        // 插入新行
        for (i, newLine) in newLines.enumerated() {
            lines.insert(newLine, at: insertIndex + i)
        }
        
        // 写回文件
        try writeAllLines(path: path, lines: lines)
    }
    
    // MARK: - Replace In File
    
    /// 查找并替换文件内容
    public func replaceInFile(path: String, search: String, replace: String,
                              isRegex: Bool, replaceAll: Bool, caseSensitive: Bool) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AIEditOperations", code: 404,
                         userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        var content = try String(contentsOfFile: path, encoding: .utf8)
        var newContent: String
        var replacements = 0
        
        if isRegex {
            var options: NSRegularExpression.Options = []
            if !caseSensitive {
                options.insert(.caseInsensitive)
            }
            
            let regex = try NSRegularExpression(pattern: search, options: options)
            let range = NSRange(content.startIndex..., in: content)
            
            // 计算替换次数
            replacements = regex.numberOfMatches(in: content, options: [], range: range)
            
            if replaceAll {
                newContent = regex.stringByReplacingMatches(in: content, options: [], 
                                                            range: range, withTemplate: replace)
            } else {
                if let firstMatch = regex.firstMatch(in: content, options: [], range: range) {
                    let matchRange = Range(firstMatch.range, in: content)!
                    newContent = content.replacingCharacters(in: matchRange, with: replace)
                    replacements = 1
                } else {
                    newContent = content
                    replacements = 0
                }
            }
        } else {
            if replaceAll {
                if caseSensitive {
                    replacements = content.components(separatedBy: search).count - 1
                    newContent = content.replacingOccurrences(of: search, with: replace)
                } else {
                    replacements = content.lowercased().components(separatedBy: search.lowercased()).count - 1
                    newContent = content.replacingOccurrences(of: search, with: replace, 
                                                              options: .caseInsensitive)
                }
            } else {
                if let range = caseSensitive ? 
                    content.range(of: search) : 
                    content.range(of: search, options: .caseInsensitive) {
                    newContent = content.replacingCharacters(in: range, with: replace)
                    replacements = 1
                } else {
                    newContent = content
                    replacements = 0
                }
            }
        }
        
        let modified = content != newContent
        if modified {
            try newContent.write(toFile: path, atomically: true, encoding: .utf8)
        }
        
        return [
            "replacements": replacements,
            "modified": modified
        ]
    }
    
    // MARK: - Apply Diff
    
    /// 应用 Unified Diff 补丁
    public func applyDiff(path: String, diff: String, createBackup: Bool) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AIEditOperations", code: 404,
                         userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        var backupPath: String? = nil
        if createBackup {
            backupPath = path + ".bak"
            try fileManager.copyItem(atPath: path, toPath: backupPath!)
        }
        
        var lines = try readAllLines(path: path)
        let diffLines = diff.components(separatedBy: "\n")
        
        var linesAdded = 0
        var linesDeleted = 0
        var currentLine = 0
        var offset = 0
        
        for diffLine in diffLines {
            if diffLine.hasPrefix("@@") {
                // 解析 hunk header
                let pattern = "@@ -(\\d+)(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@"
                if let regex = try? NSRegularExpression(pattern: pattern),
                   let match = regex.firstMatch(in: diffLine, 
                                                range: NSRange(diffLine.startIndex..., in: diffLine)) {
                    if let range = Range(match.range(at: 1), in: diffLine) {
                        currentLine = (Int(diffLine[range]) ?? 1) - 1 + offset
                    }
                }
            } else if diffLine.hasPrefix("-") && !diffLine.hasPrefix("---") {
                // 删除行
                if currentLine < lines.count {
                    lines.remove(at: currentLine)
                    linesDeleted += 1
                    offset -= 1
                }
            } else if diffLine.hasPrefix("+") && !diffLine.hasPrefix("+++") {
                // 添加行
                let newLine = String(diffLine.dropFirst())
                lines.insert(newLine, at: currentLine)
                linesAdded += 1
                currentLine += 1
                offset += 1
            } else if !diffLine.hasPrefix("\\") && !diffLine.hasPrefix("---") && 
                      !diffLine.hasPrefix("+++") && !diffLine.hasPrefix("diff") {
                // 上下文行
                currentLine += 1
            }
        }
        
        // 写回文件
        try writeAllLines(path: path, lines: lines)
        
        var result: [String: Any] = [
            "success": true,
            "linesChanged": linesAdded + linesDeleted,
            "linesAdded": linesAdded,
            "linesDeleted": linesDeleted
        ]
        
        if let backup = backupPath {
            result["backupPath"] = backup
        }
        
        return result
    }
    
    // MARK: - Get File Hash
    
    /// 获取文件哈希值
    public func getFileHash(path: String, algorithm: String) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AIEditOperations", code: 404,
                         userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        let content = try String(contentsOfFile: path, encoding: .utf8)
        let hash = calculateHash(content: content, algorithm: algorithm)
        
        return [
            "hash": hash,
            "algorithm": algorithm
        ]
    }
    
    // MARK: - Get Line Count
    
    /// 获取文件行数
    public func getLineCount(path: String) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AIEditOperations", code: 404,
                         userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        let lines = try readAllLines(path: path)
        
        return [
            "lines": lines.count
        ]
    }
    
    // MARK: - Helper Methods
    
    private func readAllLines(path: String) throws -> [String] {
        let content = try String(contentsOfFile: path, encoding: .utf8)
        return content.components(separatedBy: "\n")
    }
    
    private func writeAllLines(path: String, lines: [String]) throws {
        let content = lines.joined(separator: "\n")
        try content.write(toFile: path, atomically: true, encoding: .utf8)
    }
    
    private func calculateHash(content: String, algorithm: String) -> String {
        guard let data = content.data(using: .utf8) else {
            return ""
        }
        
        if algorithm.lowercased() == "sha256" {
            var hash = [UInt8](repeating: 0, count: Int(CC_SHA256_DIGEST_LENGTH))
            data.withUnsafeBytes {
                _ = CC_SHA256($0.baseAddress, CC_LONG(data.count), &hash)
            }
            return hash.map { String(format: "%02x", $0) }.joined()
        } else {
            // MD5
            var hash = [UInt8](repeating: 0, count: Int(CC_MD5_DIGEST_LENGTH))
            data.withUnsafeBytes {
                _ = CC_MD5($0.baseAddress, CC_LONG(data.count), &hash)
            }
            return hash.map { String(format: "%02x", $0) }.joined()
        }
    }
}
