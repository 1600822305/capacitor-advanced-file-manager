import Foundation

@objc public class AdvancedFileManager: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
