import Foundation
import MobileCoreServices
import UniformTypeIdentifiers

/// 文件工具类
public class FileUtils {
    private static let fileManager = FileManager.default
    
    /// 创建文件信息字典
    public static func createFileInfo(from url: URL, resourceValues: URLResourceValues) -> [String: Any] {
        let isDirectory = resourceValues.isDirectory ?? false
        let size = resourceValues.fileSize ?? 0
        let mtime = resourceValues.contentModificationDate?.timeIntervalSince1970 ?? 0
        let ctime = resourceValues.creationDate?.timeIntervalSince1970 ?? 0
        let isHidden = resourceValues.isHidden ?? url.lastPathComponent.hasPrefix(".")
        
        var info: [String: Any] = [
            "name": url.lastPathComponent,
            "path": url.path,
            "size": size,
            "type": isDirectory ? "directory" : "file",
            "mtime": Int64(mtime * 1000),
            "ctime": Int64(ctime * 1000),
            "isHidden": isHidden,
            "permissions": getPermissions(for: url.path)
        ]
        
        if !isDirectory {
            info["mimeType"] = getMimeType(for: url.lastPathComponent)
        }
        
        return info
    }
    
    /// 获取文件权限
    public static func getPermissions(for path: String) -> String {
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
    
    /// 获取文件扩展名
    public static func getFileExtension(_ fileName: String) -> String {
        return (fileName as NSString).pathExtension.lowercased()
    }
    
    /// 获取 MIME 类型
    public static func getMimeType(for fileName: String) -> String {
        let ext = getFileExtension(fileName)
        
        if #available(iOS 14.0, *) {
            if let utType = UTType(filenameExtension: ext) {
                return utType.preferredMIMEType ?? "application/octet-stream"
            }
        } else {
            if let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, ext as CFString, nil)?.takeRetainedValue() {
                if let mimeType = UTTypeCopyPreferredTagWithClass(uti, kUTTagClassMIMEType)?.takeRetainedValue() {
                    return mimeType as String
                }
            }
        }
        
        return "application/octet-stream"
    }
    
    /// 递归删除
    public static func deleteRecursively(at path: String) throws {
        try fileManager.removeItem(atPath: path)
    }
    
    /// 确保目录存在
    public static func ensureDirectoryExists(at path: String) throws {
        if !fileManager.fileExists(atPath: path) {
            try fileManager.createDirectory(atPath: path, withIntermediateDirectories: true, attributes: nil)
        }
    }
    
    /// 获取可读的文件大小
    public static func getReadableFileSize(_ size: Int64) -> String {
        let units = ["B", "KB", "MB", "GB", "TB"]
        var size = Double(size)
        var unitIndex = 0
        
        while size >= 1024 && unitIndex < units.count - 1 {
            size /= 1024
            unitIndex += 1
        }
        
        return String(format: "%.1f %@", size, units[unitIndex])
    }
}
