import Foundation

@objc public class AdvancedFileManager: NSObject {
    private let fileManager = FileManager.default

    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }

    // MARK: - Directory Operations

    public func listDirectory(path: String, showHidden: Bool, sortBy: String, sortOrder: String) throws -> [String: Any] {
        let url = URL(fileURLWithPath: path)

        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "Directory does not exist: \(path)"])
        }

        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)

        guard isDirectory.boolValue else {
            throw NSError(domain: "AdvancedFileManager", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is not a directory: \(path)"])
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

            let fileInfo = createFileInfo(from: fileURL, resourceValues: resourceValues)
            files.append(fileInfo)
        }

        // Sort files
        sortFiles(&files, by: sortBy, order: sortOrder)

        return [
            "files": files,
            "totalCount": files.count
        ]
    }

    private func createFileInfo(from url: URL, resourceValues: URLResourceValues) -> [String: Any] {
        let isDirectory = resourceValues.isDirectory ?? false
        let size = resourceValues.fileSize ?? 0
        let mtime = resourceValues.contentModificationDate?.timeIntervalSince1970 ?? 0
        let ctime = resourceValues.creationDate?.timeIntervalSince1970 ?? 0
        let isHidden = resourceValues.isHidden ?? url.lastPathComponent.hasPrefix(".")

        return [
            "name": url.lastPathComponent,
            "path": url.path,
            "size": size,
            "type": isDirectory ? "directory" : "file",
            "mtime": Int64(mtime * 1000), // Convert to milliseconds
            "ctime": Int64(ctime * 1000),
            "isHidden": isHidden,
            "permissions": getPermissions(for: url.path)
        ]
    }

    private func getPermissions(for path: String) -> String {
        var permissions = ""

        if fileManager.isReadableFile(atPath: path) {
            permissions += "r"
        } else {
            permissions += "-"
        }

        if fileManager.isWritableFile(atPath: path) {
            permissions += "w"
        } else {
            permissions += "-"
        }

        if fileManager.isExecutableFile(atPath: path) {
            permissions += "x"
        } else {
            permissions += "-"
        }

        return permissions
    }

    private func sortFiles(_ files: inout [[String: Any]], by sortBy: String, order sortOrder: String) {
        files.sort { file1, file2 in
            var result = false

            switch sortBy {
            case "size":
                let size1 = file1["size"] as? Int ?? 0
                let size2 = file2["size"] as? Int ?? 0
                result = size1 < size2
            case "mtime":
                let mtime1 = file1["mtime"] as? Int64 ?? 0
                let mtime2 = file2["mtime"] as? Int64 ?? 0
                result = mtime1 < mtime2
            case "type":
                let type1 = file1["type"] as? String ?? ""
                let type2 = file2["type"] as? String ?? ""
                if type1 == type2 {
                    let name1 = file1["name"] as? String ?? ""
                    let name2 = file2["name"] as? String ?? ""
                    result = name1.localizedCaseInsensitiveCompare(name2) == .orderedAscending
                } else {
                    result = type1 == "directory" // Directories first
                }
            default: // name
                let name1 = file1["name"] as? String ?? ""
                let name2 = file2["name"] as? String ?? ""
                result = name1.localizedCaseInsensitiveCompare(name2) == .orderedAscending
            }

            return sortOrder == "desc" ? !result : result
        }
    }

    public func createDirectory(path: String, recursive: Bool) throws {
        let url = URL(fileURLWithPath: path)

        if recursive {
            try fileManager.createDirectory(at: url, withIntermediateDirectories: true, attributes: nil)
        } else {
            try fileManager.createDirectory(at: url, withIntermediateDirectories: false, attributes: nil)
        }
    }

    public func deleteDirectory(path: String) throws {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "Directory does not exist: \(path)"])
        }

        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)

        guard isDirectory.boolValue else {
            throw NSError(domain: "AdvancedFileManager", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is not a directory: \(path)"])
        }

        try fileManager.removeItem(atPath: path)
    }

    // MARK: - File Operations

    public func createFile(path: String, content: String, encoding: String) throws {
        let url = URL(fileURLWithPath: path)

        // Create parent directories if needed
        let parentURL = url.deletingLastPathComponent()
        if !fileManager.fileExists(atPath: parentURL.path) {
            try fileManager.createDirectory(at: parentURL, withIntermediateDirectories: true, attributes: nil)
        }

        var data: Data
        if encoding == "base64" {
            guard let decodedData = Data(base64Encoded: content) else {
                throw NSError(domain: "AdvancedFileManager", code: 400, userInfo: [NSLocalizedDescriptionKey: "Invalid base64 content"])
            }
            data = decodedData
        } else {
            data = content.data(using: .utf8) ?? Data()
        }

        try data.write(to: url)
    }

    public func readFile(path: String, encoding: String) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }

        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)

        guard !isDirectory.boolValue else {
            throw NSError(domain: "AdvancedFileManager", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is not a file: \(path)"])
        }

        let url = URL(fileURLWithPath: path)
        let data = try Data(contentsOf: url)

        let content: String
        if encoding == "base64" {
            content = data.base64EncodedString()
        } else {
            content = String(data: data, encoding: .utf8) ?? ""
        }

        return [
            "content": content,
            "encoding": encoding
        ]
    }

    public func writeFile(path: String, content: String, encoding: String, append: Bool) throws {
        let url = URL(fileURLWithPath: path)

        // Create parent directories if needed
        let parentURL = url.deletingLastPathComponent()
        if !fileManager.fileExists(atPath: parentURL.path) {
            try fileManager.createDirectory(at: parentURL, withIntermediateDirectories: true, attributes: nil)
        }

        var data: Data
        if encoding == "base64" {
            guard let decodedData = Data(base64Encoded: content) else {
                throw NSError(domain: "AdvancedFileManager", code: 400, userInfo: [NSLocalizedDescriptionKey: "Invalid base64 content"])
            }
            data = decodedData
        } else {
            data = content.data(using: .utf8) ?? Data()
        }

        if append && fileManager.fileExists(atPath: path) {
            let fileHandle = try FileHandle(forWritingTo: url)
            defer { fileHandle.closeFile() }
            fileHandle.seekToEndOfFile()
            fileHandle.write(data)
        } else {
            try data.write(to: url)
        }
    }

    public func deleteFile(path: String) throws {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }

        var isDirectory: ObjCBool = false
        fileManager.fileExists(atPath: path, isDirectory: &isDirectory)

        guard !isDirectory.boolValue else {
            throw NSError(domain: "AdvancedFileManager", code: 400, userInfo: [NSLocalizedDescriptionKey: "Path is not a file: \(path)"])
        }

        try fileManager.removeItem(atPath: path)
    }

    public func moveFile(sourcePath: String, destinationPath: String) throws {
        guard fileManager.fileExists(atPath: sourcePath) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "Source file does not exist: \(sourcePath)"])
        }

        let sourceURL = URL(fileURLWithPath: sourcePath)
        let destinationURL = URL(fileURLWithPath: destinationPath)

        // Create parent directories if needed
        let parentURL = destinationURL.deletingLastPathComponent()
        if !fileManager.fileExists(atPath: parentURL.path) {
            try fileManager.createDirectory(at: parentURL, withIntermediateDirectories: true, attributes: nil)
        }

        try fileManager.moveItem(at: sourceURL, to: destinationURL)
    }

    public func copyFile(sourcePath: String, destinationPath: String, overwrite: Bool) throws {
        guard fileManager.fileExists(atPath: sourcePath) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "Source file does not exist: \(sourcePath)"])
        }

        if fileManager.fileExists(atPath: destinationPath) && !overwrite {
            throw NSError(domain: "AdvancedFileManager", code: 409, userInfo: [NSLocalizedDescriptionKey: "Destination file already exists: \(destinationPath)"])
        }

        let sourceURL = URL(fileURLWithPath: sourcePath)
        let destinationURL = URL(fileURLWithPath: destinationPath)

        // Create parent directories if needed
        let parentURL = destinationURL.deletingLastPathComponent()
        if !fileManager.fileExists(atPath: parentURL.path) {
            try fileManager.createDirectory(at: parentURL, withIntermediateDirectories: true, attributes: nil)
        }

        // Remove destination if it exists and overwrite is true
        if fileManager.fileExists(atPath: destinationPath) && overwrite {
            try fileManager.removeItem(at: destinationURL)
        }

        try fileManager.copyItem(at: sourceURL, to: destinationURL)
    }

    public func renameFile(path: String, newName: String) throws {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }

        let sourceURL = URL(fileURLWithPath: path)
        let destinationURL = sourceURL.deletingLastPathComponent().appendingPathComponent(newName)

        try fileManager.moveItem(at: sourceURL, to: destinationURL)
    }

    public func getFileInfo(path: String) throws -> [String: Any] {
        guard fileManager.fileExists(atPath: path) else {
            throw NSError(domain: "AdvancedFileManager", code: 404, userInfo: [NSLocalizedDescriptionKey: "File does not exist: \(path)"])
        }

        let url = URL(fileURLWithPath: path)
        let resourceValues = try url.resourceValues(forKeys: [
            .fileSizeKey,
            .contentModificationDateKey,
            .creationDateKey,
            .isDirectoryKey,
            .isHiddenKey
        ])

        return createFileInfo(from: url, resourceValues: resourceValues)
    }

    public func exists(path: String) -> Bool {
        return fileManager.fileExists(atPath: path)
    }
}
