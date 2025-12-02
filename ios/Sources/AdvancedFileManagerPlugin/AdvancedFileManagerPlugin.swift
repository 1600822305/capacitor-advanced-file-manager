import Foundation
import Capacitor
import UIKit
import UniformTypeIdentifiers

/**
 * Advanced File Manager Plugin for Capacitor
 * 模块化架构的文件管理插件
 */
@objc(AdvancedFileManagerPlugin)
public class AdvancedFileManagerPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "AdvancedFileManagerPlugin"
    public let jsName = "AdvancedFileManager"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "requestPermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "checkPermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "listDirectory", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "createDirectory", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "deleteDirectory", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "createFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "readFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "writeFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "deleteFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "renameFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "moveFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "copyFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getFileInfo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "exists", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "searchFiles", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openSystemFilePicker", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openSystemFileManager", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openFileWithSystemApp", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    
    // 模块实例
    private let fileOps = FileOperations()
    private let dirOps = DirectoryOperations()
    private let fileSearcher = FileSearcher()

    // MARK: - 权限管理

    @objc public override func requestPermissions(_ call: CAPPluginCall) {
        call.resolve([
            "granted": true,
            "message": "File access permissions granted within app sandbox"
        ])
    }

    @objc public override func checkPermissions(_ call: CAPPluginCall) {
        call.resolve([
            "granted": true,
            "message": "File access permissions granted within app sandbox"
        ])
    }

    // MARK: - 目录操作

    @objc func listDirectory(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        let showHidden = call.getBool("showHidden") ?? false
        let sortBy = call.getString("sortBy") ?? "name"
        let sortOrder = call.getString("sortOrder") ?? "asc"

        do {
            let result = try dirOps.listDirectory(path: path, showHidden: showHidden, sortBy: sortBy, sortOrder: sortOrder)
            call.resolve(result)
        } catch {
            call.reject("Failed to list directory: \(error.localizedDescription)")
        }
    }

    @objc func createDirectory(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        let recursive = call.getBool("recursive") ?? false

        do {
            try dirOps.createDirectory(path: path, recursive: recursive)
            call.resolve()
        } catch {
            call.reject("Failed to create directory: \(error.localizedDescription)")
        }
    }

    @objc func deleteDirectory(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        do {
            try dirOps.deleteDirectory(path: path)
            call.resolve()
        } catch {
            call.reject("Failed to delete directory: \(error.localizedDescription)")
        }
    }

    // MARK: - 文件操作

    @objc func createFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        let content = call.getString("content") ?? ""
        let encoding = call.getString("encoding") ?? "utf8"

        do {
            try fileOps.createFile(path: path, content: content, encoding: encoding)
            call.resolve()
        } catch {
            call.reject("Failed to create file: \(error.localizedDescription)")
        }
    }

    @objc func readFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        let encoding = call.getString("encoding") ?? "utf8"

        do {
            let result = try fileOps.readFile(path: path, encoding: encoding)
            call.resolve(result)
        } catch {
            call.reject("Failed to read file: \(error.localizedDescription)")
        }
    }

    @objc func writeFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path"),
              let content = call.getString("content") else {
            call.reject("Path and content are required")
            return
        }

        let encoding = call.getString("encoding") ?? "utf8"
        let append = call.getBool("append") ?? false

        do {
            try fileOps.writeFile(path: path, content: content, encoding: encoding, append: append)
            call.resolve()
        } catch {
            call.reject("Failed to write file: \(error.localizedDescription)")
        }
    }

    @objc func deleteFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        do {
            try fileOps.deleteFile(path: path)
            call.resolve()
        } catch {
            call.reject("Failed to delete file: \(error.localizedDescription)")
        }
    }

    @objc func renameFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path"),
              let newName = call.getString("newName") else {
            call.reject("Path and new name are required")
            return
        }

        do {
            try fileOps.renameFile(path: path, newName: newName)
            call.resolve()
        } catch {
            call.reject("Failed to rename file: \(error.localizedDescription)")
        }
    }

    @objc func moveFile(_ call: CAPPluginCall) {
        guard let sourcePath = call.getString("sourcePath"),
              let destinationPath = call.getString("destinationPath") else {
            call.reject("sourcePath and destinationPath are required")
            return
        }

        do {
            try fileOps.moveFile(sourcePath: sourcePath, destinationPath: destinationPath)
            call.resolve()
        } catch {
            call.reject("Failed to move file: \(error.localizedDescription)")
        }
    }

    @objc func copyFile(_ call: CAPPluginCall) {
        guard let sourcePath = call.getString("sourcePath"),
              let destinationPath = call.getString("destinationPath") else {
            call.reject("sourcePath and destinationPath are required")
            return
        }

        let overwrite = call.getBool("overwrite") ?? false

        do {
            try fileOps.copyFile(sourcePath: sourcePath, destinationPath: destinationPath, overwrite: overwrite)
            call.resolve()
        } catch {
            call.reject("Failed to copy file: \(error.localizedDescription)")
        }
    }

    @objc func getFileInfo(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        do {
            let result = try fileOps.getFileInfo(path: path)
            call.resolve(result)
        } catch {
            call.reject("Failed to get file info: \(error.localizedDescription)")
        }
    }

    @objc func exists(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        call.resolve(["exists": fileOps.exists(path: path)])
    }

    // MARK: - 搜索

    @objc func searchFiles(_ call: CAPPluginCall) {
        guard let directory = call.getString("directory"),
              let query = call.getString("query") else {
            call.reject("Directory and query are required")
            return
        }

        let searchType = call.getString("searchType") ?? "name"
        let fileTypes = call.getArray("fileTypes")?.compactMap { $0 as? String }
        let maxResults = call.getInt("maxResults") ?? 100
        let recursive = call.getBool("recursive") ?? true

        do {
            let result = try fileSearcher.searchFiles(directory: directory, query: query,
                                                       searchType: searchType, fileTypes: fileTypes,
                                                       maxResults: maxResults, recursive: recursive)
            call.resolve(result)
        } catch {
            call.reject("Failed to search files: \(error.localizedDescription)")
        }
    }

    // MARK: - 系统文件选择器

    @objc func openSystemFilePicker(_ call: CAPPluginCall) {
        let type = call.getString("type") ?? "file"
        let multiple = call.getBool("multiple") ?? false

        DispatchQueue.main.async {
            if type == "directory" {
                self.openDirectoryPicker(call: call)
            } else {
                self.openFilePicker(call: call, multiple: multiple)
            }
        }
    }

    private func openFilePicker(call: CAPPluginCall, multiple: Bool) {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.item], asCopy: true)
        picker.allowsMultipleSelection = multiple
        picker.delegate = self
        
        bridge?.saveCall(call)
        bridge?.viewController?.present(picker, animated: true, completion: nil)
    }

    private func openDirectoryPicker(call: CAPPluginCall) {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.folder])
        picker.delegate = self
        
        bridge?.saveCall(call)
        bridge?.viewController?.present(picker, animated: true, completion: nil)
    }

    @objc func openSystemFileManager(_ call: CAPPluginCall) {
        // iOS 没有直接打开文件管理器的方式
        call.resolve()
    }

    @objc func openFileWithSystemApp(_ call: CAPPluginCall) {
        guard let filePath = call.getString("path") else {
            call.reject("File path is required")
            return
        }

        let url = URL(fileURLWithPath: filePath)

        guard FileManager.default.fileExists(atPath: filePath) else {
            call.reject("File does not exist: \(filePath)")
            return
        }

        DispatchQueue.main.async {
            let documentController = UIDocumentInteractionController(url: url)
            documentController.delegate = self
            
            if let viewController = self.bridge?.viewController {
                if !documentController.presentPreview(animated: true) {
                    documentController.presentOpenInMenu(from: viewController.view.bounds, in: viewController.view, animated: true)
                }
            }
            
            call.resolve()
        }
    }

    // MARK: - 工具方法

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve(["value": value])
    }
}

// MARK: - UIDocumentPickerDelegate

extension AdvancedFileManagerPlugin: UIDocumentPickerDelegate {
    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let call = bridge?.savedCall(withID: bridge?.savedCalls?.first?.key ?? "") else {
            return
        }
        
        var files: [[String: Any]] = []
        var directories: [[String: Any]] = []
        
        for url in urls {
            var isDirectory: ObjCBool = false
            FileManager.default.fileExists(atPath: url.path, isDirectory: &isDirectory)
            
            do {
                let resourceValues = try url.resourceValues(forKeys: [
                    .fileSizeKey,
                    .contentModificationDateKey,
                    .creationDateKey,
                    .isDirectoryKey
                ])
                
                let fileInfo = FileUtils.createFileInfo(from: url, resourceValues: resourceValues)
                
                if isDirectory.boolValue {
                    directories.append(fileInfo)
                } else {
                    files.append(fileInfo)
                }
            } catch {
                // 基本信息
                let info: [String: Any] = [
                    "name": url.lastPathComponent,
                    "path": url.path,
                    "type": isDirectory.boolValue ? "directory" : "file"
                ]
                
                if isDirectory.boolValue {
                    directories.append(info)
                } else {
                    files.append(info)
                }
            }
        }
        
        call.resolve([
            "files": files,
            "directories": directories,
            "cancelled": false
        ])
        
        bridge?.releaseCall(call)
    }
    
    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        guard let call = bridge?.savedCall(withID: bridge?.savedCalls?.first?.key ?? "") else {
            return
        }
        
        call.resolve([
            "files": [],
            "directories": [],
            "cancelled": true
        ])
        
        bridge?.releaseCall(call)
    }
}

// MARK: - UIDocumentInteractionControllerDelegate

extension AdvancedFileManagerPlugin: UIDocumentInteractionControllerDelegate {
    public func documentInteractionControllerViewControllerForPreview(_ controller: UIDocumentInteractionController) -> UIViewController {
        return bridge?.viewController ?? UIViewController()
    }
}
