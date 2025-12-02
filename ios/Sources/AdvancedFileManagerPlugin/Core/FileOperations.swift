import Foundation

/// 文件操作模块
/// 负责文件的 CRUD 操作
public class FileOperations {
    private let fileManager = FileManager.default
    
    // MARK: - Create File
    
    public func createFile(path: String, content: String, encoding: String) throws {
        let url = URL(fileURLWithPath: path)
        
        // 确保父目录存在
        let parentDir = url.deletingLastPathComponent()
        if !fileManager.fileExists(atPath: parentDir.path) {
            try fileManager.createDirectory(at: parentDir, withIntermediateDirectories: true, attributes: nil)
        }
        
        let data: Data
        if encoding == "base64" {
            guard let decodedData = Data(base64Encoded: content) else {
                throw NSError(domain: "FileOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Invalid base64 content"])
            }
            data = decodedData
        } else {
            guard let stringData = content.data(using: .utf8) else {
                throw NSError(domain: "FileOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Failed to encode content as UTF-8"])
            }
            data = stringData
        }
        
        try data.write(to: url)
    }
    
    // MARK: - Read File
    
    public func readFile(path: String, encoding: String) throws -> [String: Any] {
        let url = URL(fileURLWithPath: path)
        
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "FileOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)
        
        guard !isDirectory.boolValue else {
            throw NSError(domain: "FileOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is a directory: \(path)"])
        }
        
        let data = try Data(contentsOf: url)
        
        let content: String
        if encoding == "base64" {
            content = data.base64EncodedString()
        } else {
            guard let stringContent = String(data: data, encoding: .utf8) else {
                throw NSError(domain: "FileOperations", code: 500, userInfo: [NSLocalizedDescriptionKey: "Failed to decode file as UTF-8"])
            }
            content = stringContent
        }
        
        return [
            "content": content,
            "encoding": encoding
        ]
    }
    
    // MARK: - Write File
    
    public func writeFile(path: String, content: String, encoding: String, append: Bool) throws {
        let url = URL(fileURLWithPath: path)
        
        // 确保父目录存在
        let parentDir = url.deletingLastPathComponent()
        if !fileManager.fileExists(atPath: parentDir.path) {
            try fileManager.createDirectory(at: parentDir, withIntermediateDirectories: true, attributes: nil)
        }
        
        let data: Data
        if encoding == "base64" {
            guard let decodedData = Data(base64Encoded: content) else {
                throw NSError(domain: "FileOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Invalid base64 content"])
            }
            data = decodedData
        } else {
            guard let stringData = content.data(using: .utf8) else {
                throw NSError(domain: "FileOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Failed to encode content as UTF-8"])
            }
            data = stringData
        }
        
        if append && fileManager.fileExists(atPath: path) {
            let fileHandle = try FileHandle(forWritingTo: url)
            fileHandle.seekToEndOfFile()
            fileHandle.write(data)
            fileHandle.closeFile()
        } else {
            try data.write(to: url)
        }
    }
    
    // MARK: - Delete File
    
    public func deleteFile(path: String) throws {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "FileOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)
        
        guard !isDirectory.boolValue else {
            throw NSError(domain: "FileOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is a directory, use deleteDirectory: \(path)"])
        }
        
        try fileManager.removeItem(atPath: path)
    }
    
    // MARK: - Rename File
    
    public func renameFile(path: String, newName: String) throws {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "FileOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        let url = URL(fileURLWithPath: path)
        let newURL = url.deletingLastPathComponent().appendingPathComponent(newName)
        
        guard !fileManager.fileExists(atPath: newURL.path) else {
            throw NSError(domain: "FileOperations", code: 409, userInfo: [NSLocalizedDescriptionKey: "A file with that name already exists: \(newURL.path)"])
        }
        
        try fileManager.moveItem(at: url, to: newURL)
    }
    
    // MARK: - Move File
    
    public func moveFile(sourcePath: String, destinationPath: String) throws {
        guard fileManager.fileExists(atPath: sourcePath) else {
            throw NSError(domain: "FileOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "Source file does not exist: \(sourcePath)"])
        }
        
        let destURL = URL(fileURLWithPath: destinationPath)
        let parentDir = destURL.deletingLastPathComponent()
        
        if !fileManager.fileExists(atPath: parentDir.path) {
            try fileManager.createDirectory(at: parentDir, withIntermediateDirectories: true, attributes: nil)
        }
        
        try fileManager.moveItem(atPath: sourcePath, toPath: destinationPath)
    }
    
    // MARK: - Copy File
    
    public func copyFile(sourcePath: String, destinationPath: String, overwrite: Bool) throws {
        guard fileManager.fileExists(atPath: sourcePath) else {
            throw NSError(domain: "FileOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "Source file does not exist: \(sourcePath)"])
        }
        
        let destURL = URL(fileURLWithPath: destinationPath)
        let parentDir = destURL.deletingLastPathComponent()
        
        if !fileManager.fileExists(atPath: parentDir.path) {
            try fileManager.createDirectory(at: parentDir, withIntermediateDirectories: true, attributes: nil)
        }
        
        if fileManager.fileExists(atPath: destinationPath) {
            if overwrite {
                try fileManager.removeItem(atPath: destinationPath)
            } else {
                throw NSError(domain: "FileOperations", code: 409, userInfo: [NSLocalizedDescriptionKey: "Destination already exists: \(destinationPath)"])
            }
        }
        
        try fileManager.copyItem(atPath: sourcePath, toPath: destinationPath)
    }
    
    // MARK: - Get File Info
    
    public func getFileInfo(path: String) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "FileOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }
        
        let url = URL(fileURLWithPath: path)
        let resourceValues = try url.resourceValues(forKeys: [
            .fileSizeKey,
            .contentModificationDateKey,
            .creationDateKey,
            .isDirectoryKey,
            .isHiddenKey
        ])
        
        return FileUtils.createFileInfo(from: url, resourceValues: resourceValues)
    }
    
    // MARK: - Exists
    
    public func exists(path: String) -> Bool {
        return fileManager.fileExists(atPath: path)
    }
}
