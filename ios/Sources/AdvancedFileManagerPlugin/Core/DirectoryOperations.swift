import Foundation

/// 目录操作模块
public class DirectoryOperations {
    private let fileManager = FileManager.default
    
    // MARK: - List Directory
    
    public func listDirectory(path: String, showHidden: Bool, sortBy: String, sortOrder: String) throws -> [String: Any] {
        let url = URL(fileURLWithPath: path)
        
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "DirectoryOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "Directory does not exist: \(path)"])
        }
        
        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)
        
        guard isDirectory.boolValue else {
            throw NSError(domain: "DirectoryOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is not a directory: \(path)"])
        }
        
        let contents = try fileManager.contentsOfDirectory(at: url, includingPropertiesForKeys: [
            .fileSizeKey,
            .contentModificationDateKey,
            .creationDateKey,
            .isDirectoryKey,
            .isHiddenKey
        ], options: [])
        
        var files: [[String: Any]] = []
        
        for fileURL in contents {
            let resourceValues = try fileURL.resourceValues(forKeys: [
                .fileSizeKey,
                .contentModificationDateKey,
                .creationDateKey,
                .isDirectoryKey,
                .isHiddenKey
            ])
            
            let isHidden = resourceValues.isHidden ?? fileURL.lastPathComponent.hasPrefix(".")
            
            if !showHidden && isHidden {
                continue
            }
            
            let fileInfo = FileUtils.createFileInfo(from: fileURL, resourceValues: resourceValues)
            files.append(fileInfo)
        }
        
        // Sort files
        sortFiles(&files, by: sortBy, order: sortOrder)
        
        return [
            "files": files,
            "totalCount": files.count
        ]
    }
    
    // MARK: - Create Directory
    
    public func createDirectory(path: String, recursive: Bool) throws {
        if fileManager.fileExists(atPath: path) {
            var isDirectory: ObjCBool = false
            fileManager.fileExists(atPath: path, isDirectory: &isDirectory)
            if isDirectory.boolValue {
                return // 目录已存在
            }
            throw NSError(domain: "DirectoryOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path exists but is not a directory: \(path)"])
        }
        
        try fileManager.createDirectory(atPath: path, withIntermediateDirectories: recursive, attributes: nil)
    }
    
    // MARK: - Delete Directory
    
    public func deleteDirectory(path: String) throws {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "DirectoryOperations", code: 404, userInfo: [NSLocalizedDescriptionKey: "Directory does not exist: \(path)"])
        }
        
        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)
        
        guard isDirectory.boolValue else {
            throw NSError(domain: "DirectoryOperations", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is not a directory: \(path)"])
        }
        
        try fileManager.removeItem(atPath: path)
    }
    
    // MARK: - Sorting
    
    private func sortFiles(_ files: inout [[String: Any]], by sortBy: String, order sortOrder: String) {
        files.sort { file1, file2 in
            let isDir1 = (file1["type"] as? String) == "directory"
            let isDir2 = (file2["type"] as? String) == "directory"
            
            // 目录优先
            if isDir1 != isDir2 {
                return isDir1
            }
            
            let ascending = sortOrder != "desc"
            
            switch sortBy {
            case "size":
                let size1 = file1["size"] as? Int ?? 0
                let size2 = file2["size"] as? Int ?? 0
                return ascending ? size1 < size2 : size1 > size2
                
            case "mtime":
                let mtime1 = file1["mtime"] as? Int64 ?? 0
                let mtime2 = file2["mtime"] as? Int64 ?? 0
                return ascending ? mtime1 < mtime2 : mtime1 > mtime2
                
            case "type":
                let name1 = (file1["name"] as? String ?? "").lowercased()
                let name2 = (file2["name"] as? String ?? "").lowercased()
                return ascending ? name1 < name2 : name1 > name2
                
            default: // "name"
                let name1 = (file1["name"] as? String ?? "").lowercased()
                let name2 = (file2["name"] as? String ?? "").lowercased()
                return ascending ? name1 < name2 : name1 > name2
            }
        }
    }
}
