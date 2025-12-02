import Foundation
import Capacitor
import UIKit
import UniformTypeIdentifiers

/**
 * Advanced File Manager Plugin for Capacitor
 * Provides comprehensive file system operations
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
        CAPPluginMethod(name: "moveFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "copyFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "renameFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getFileInfo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "exists", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openSystemFilePicker", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openSystemFileManager", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openFileWithSystemApp", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = AdvancedFileManager()

    @objc public override func requestPermissions(_ call: CAPPluginCall) {
        // iOS 应用在沙盒内有完整的文件访问权限
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

    @objc func listDirectory(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        let showHidden = call.getBool("showHidden") ?? false
        let sortBy = call.getString("sortBy") ?? "name"
        let sortOrder = call.getString("sortOrder") ?? "asc"

        do {
            let result = try implementation.listDirectory(path: path, showHidden: showHidden, sortBy: sortBy, sortOrder: sortOrder)
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
            try implementation.createDirectory(path: path, recursive: recursive)
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
            try implementation.deleteDirectory(path: path)
            call.resolve()
        } catch {
            call.reject("Failed to delete directory: \(error.localizedDescription)")
        }
    }

    @objc func createFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        let content = call.getString("content") ?? ""
        let encoding = call.getString("encoding") ?? "utf8"

        do {
            try implementation.createFile(path: path, content: content, encoding: encoding)
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
            let result = try implementation.readFile(path: path, encoding: encoding)
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
            try implementation.writeFile(path: path, content: content, encoding: encoding, append: append)
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
            try implementation.deleteFile(path: path)
            call.resolve()
        } catch {
            call.reject("Failed to delete file: \(error.localizedDescription)")
        }
    }

    @objc func moveFile(_ call: CAPPluginCall) {
        guard let sourcePath = call.getString("sourcePath"),
              let destinationPath = call.getString("destinationPath") else {
            call.reject("Source path and destination path are required")
            return
        }

        do {
            try implementation.moveFile(sourcePath: sourcePath, destinationPath: destinationPath)
            call.resolve()
        } catch {
            call.reject("Failed to move file: \(error.localizedDescription)")
        }
    }

    @objc func copyFile(_ call: CAPPluginCall) {
        guard let sourcePath = call.getString("sourcePath"),
              let destinationPath = call.getString("destinationPath") else {
            call.reject("Source path and destination path are required")
            return
        }

        let overwrite = call.getBool("overwrite") ?? false

        do {
            try implementation.copyFile(sourcePath: sourcePath, destinationPath: destinationPath, overwrite: overwrite)
            call.resolve()
        } catch {
            call.reject("Failed to copy file: \(error.localizedDescription)")
        }
    }

    @objc func renameFile(_ call: CAPPluginCall) {
        guard let path = call.getString("path"),
              let newName = call.getString("newName") else {
            call.reject("Path and new name are required")
            return
        }

        do {
            try implementation.renameFile(path: path, newName: newName)
            call.resolve()
        } catch {
            call.reject("Failed to rename file: \(error.localizedDescription)")
        }
    }

    @objc func getFileInfo(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Path is required")
            return
        }

        do {
            let result = try implementation.getFileInfo(path: path)
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

        let exists = implementation.exists(path: path)
        call.resolve(["exists": exists])
    }

    @objc func openSystemFilePicker(_ call: CAPPluginCall) {
        let type = call.getString("type") ?? "file"
        let multiple = call.getBool("multiple") ?? false
        _ = call.getString("title") ?? "选择文件"

        DispatchQueue.main.async {
            if type == "directory" {
                // iOS 14+ 支持目录选择
                if #available(iOS 14.0, *) {
                    let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.folder])
                    documentPicker.allowsMultipleSelection = multiple
                    documentPicker.delegate = self

                    // 存储 call 以便在回调中使用
                    self.pendingCall = call

                    self.bridge?.viewController?.present(documentPicker, animated: true)
                } else {
                    call.reject("Directory selection requires iOS 14+")
                }
            } else {
                // 文件选择
                let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.item])
                documentPicker.allowsMultipleSelection = multiple
                documentPicker.delegate = self

                self.pendingCall = call

                self.bridge?.viewController?.present(documentPicker, animated: true)
            }
        }
    }

    @objc func openSystemFileManager(_ call: CAPPluginCall) {
        _ = call.getString("path")

        DispatchQueue.main.async {
            // iOS 没有独立的文件管理器应用，打开 Files 应用
            if let url = URL(string: "shareddocuments://") {
                if UIApplication.shared.canOpenURL(url) {
                    UIApplication.shared.open(url) { success in
                        if success {
                            call.resolve()
                        } else {
                            call.reject("Failed to open Files app")
                        }
                    }
                } else {
                    call.reject("Files app not available")
                }
            } else {
                call.reject("Invalid URL for Files app")
            }
        }
    }

    @objc func openFileWithSystemApp(_ call: CAPPluginCall) {
        guard let filePath = call.getString("filePath") else {
            call.reject("File path is required")
            return
        }

        let fileURL = URL(fileURLWithPath: filePath)

        DispatchQueue.main.async {
            let documentController = UIDocumentInteractionController(url: fileURL)
            documentController.delegate = self

            if let viewController = self.bridge?.viewController {
                if documentController.presentPreview(animated: true) {
                    call.resolve()
                } else if documentController.presentOpenInMenu(from: viewController.view.bounds, in: viewController.view, animated: true) {
                    call.resolve()
                } else {
                    call.reject("No app available to open this file type")
                }
            } else {
                call.reject("View controller not available")
            }
        }
    }

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }

    // MARK: - Private Properties
    private var pendingCall: CAPPluginCall?
}

// MARK: - UIDocumentPickerDelegate
extension AdvancedFileManagerPlugin: UIDocumentPickerDelegate {
    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let call = pendingCall else { return }

        var files: [[String: Any]] = []
        var directories: [[String: Any]] = []

        for url in urls {
            // 获取文件访问权限
            let accessing = url.startAccessingSecurityScopedResource()
            defer {
                if accessing {
                    url.stopAccessingSecurityScopedResource()
                }
            }

            do {
                let resourceValues = try url.resourceValues(forKeys: [
                    .fileSizeKey,
                    .contentModificationDateKey,
                    .creationDateKey,
                    .isDirectoryKey,
                    .contentTypeKey
                ])

                let isDirectory = resourceValues.isDirectory ?? false
                let fileSize = resourceValues.fileSize ?? 0
                let mtime = resourceValues.contentModificationDate?.timeIntervalSince1970 ?? 0
                let ctime = resourceValues.creationDate?.timeIntervalSince1970 ?? 0
                let contentType = resourceValues.contentType?.identifier ?? "application/octet-stream"

                let fileInfo: [String: Any] = [
                    "name": url.lastPathComponent,
                    "path": url.path,
                    "uri": url.absoluteString,
                    "size": fileSize,
                    "type": isDirectory ? "directory" : "file",
                    "mimeType": contentType,
                    "mtime": Int64(mtime * 1000),
                    "ctime": Int64(ctime * 1000)
                ]

                if isDirectory {
                    directories.append(fileInfo)
                } else {
                    files.append(fileInfo)
                }
            } catch {
                // 如果无法获取资源信息，创建基本信息
                let fileInfo: [String: Any] = [
                    "name": url.lastPathComponent,
                    "path": url.path,
                    "uri": url.absoluteString,
                    "size": 0,
                    "type": "file",
                    "mimeType": "application/octet-stream",
                    "mtime": Int64(Date().timeIntervalSince1970 * 1000),
                    "ctime": Int64(Date().timeIntervalSince1970 * 1000)
                ]
                files.append(fileInfo)
            }
        }

        call.resolve([
            "files": files,
            "directories": directories,
            "cancelled": false
        ])

        pendingCall = nil
    }

    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        guard let call = pendingCall else { return }

        call.resolve([
            "files": [],
            "directories": [],
            "cancelled": true
        ])

        pendingCall = nil
    }
}

// MARK: - UIDocumentInteractionControllerDelegate
extension AdvancedFileManagerPlugin: UIDocumentInteractionControllerDelegate {
    public func documentInteractionControllerViewControllerForPreview(_ controller: UIDocumentInteractionController) -> UIViewController {
        return bridge?.viewController ?? UIViewController()
    }
}
