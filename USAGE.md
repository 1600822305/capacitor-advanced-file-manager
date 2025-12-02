# Advanced File Manager Plugin 使用指南

## 概述

这是一个为 Capacitor 7.2+ 开发的高级文件管理插件，提供了类似 MT 管理器的完整文件系统操作功能。

## 功能特性

### ✅ 已实现的核心功能

- **权限管理**: 检查和请求文件系统访问权限
- **目录操作**: 列出目录内容、创建目录、删除目录
- **文件操作**: 创建、读取、写入、删除文件
- **文件管理**: 移动、复制、重命名文件
- **文件信息**: 获取文件详细信息、检查文件存在性
- **跨平台支持**: Android、iOS、Web 三个平台

### 🚧 计划中的高级功能

- 文件搜索功能
- 批量操作
- 文件内容预览
- 压缩/解压缩

## 安装和配置

### 1. 安装插件

```bash
# 在你的 Capacitor 项目中安装
npm install ./capacitor-advanced-file-manager
npx cap sync
```

### 2. Android 配置

在 `android/app/src/main/AndroidManifest.xml` 中添加权限：

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 3. iOS 配置

iOS 应用在沙盒内自动拥有文件访问权限，无需额外配置。

## 基本使用

### 导入插件

```typescript
import { AdvancedFileManager } from 'capacitor-advanced-file-manager';
```

### 权限管理

```typescript
// 检查权限
const permissionStatus = await AdvancedFileManager.checkPermissions();
console.log('权限状态:', permissionStatus);

// 请求权限
const permissionResult = await AdvancedFileManager.requestPermissions();
console.log('权限请求结果:', permissionResult);
```

### 目录操作

```typescript
// 列出目录内容
const directoryContent = await AdvancedFileManager.listDirectory({
  path: '/storage/emulated/0/Download',
  showHidden: false,
  sortBy: 'name',
  sortOrder: 'asc'
});

// 创建目录
await AdvancedFileManager.createDirectory({
  path: '/storage/emulated/0/MyApp',
  recursive: true
});

// 删除目录
await AdvancedFileManager.deleteDirectory({
  path: '/storage/emulated/0/MyApp'
});
```

### 文件操作

```typescript
// 创建文件
await AdvancedFileManager.createFile({
  path: '/storage/emulated/0/test.txt',
  content: 'Hello World!',
  encoding: 'utf8'
});

// 读取文件
const fileContent = await AdvancedFileManager.readFile({
  path: '/storage/emulated/0/test.txt',
  encoding: 'utf8'
});

// 写入文件
await AdvancedFileManager.writeFile({
  path: '/storage/emulated/0/test.txt',
  content: 'Updated content',
  encoding: 'utf8',
  append: false
});

// 删除文件
await AdvancedFileManager.deleteFile({
  path: '/storage/emulated/0/test.txt'
});
```

### 文件管理

```typescript
// 移动文件
await AdvancedFileManager.moveFile({
  sourcePath: '/storage/emulated/0/old.txt',
  destinationPath: '/storage/emulated/0/new.txt'
});

// 复制文件
await AdvancedFileManager.copyFile({
  sourcePath: '/storage/emulated/0/source.txt',
  destinationPath: '/storage/emulated/0/copy.txt',
  overwrite: true
});

// 重命名文件
await AdvancedFileManager.renameFile({
  path: '/storage/emulated/0/old-name.txt',
  newName: 'new-name.txt'
});
```

### 文件信息

```typescript
// 获取文件信息
const fileInfo = await AdvancedFileManager.getFileInfo({
  path: '/storage/emulated/0/test.txt'
});

// 检查文件是否存在
const exists = await AdvancedFileManager.exists({
  path: '/storage/emulated/0/test.txt'
});
```

## 平台差异

### Android
- 支持完整的文件系统访问
- 需要存储权限
- 支持外部存储访问

### iOS
- 限制在应用沙盒内
- 无需额外权限配置
- 支持应用文档目录

### Web
- 基于 File System Access API
- 功能受浏览器安全策略限制
- 需要用户交互来选择文件/目录

## 错误处理

```typescript
try {
  await AdvancedFileManager.readFile({
    path: '/path/to/file.txt',
    encoding: 'utf8'
  });
} catch (error) {
  console.error('文件操作失败:', error.message);
}
```

## 测试

插件包含了一个测试页面 `test-plugin.html`，可以用来测试各种功能。

## 开发状态

当前版本: **0.0.1**

### ✅ 已完成
- [√] 插件基础框架
- [√] TypeScript 接口定义
- [√] Web 平台实现
- [√] Android 原生实现
- [√] iOS 原生实现
- [√] 基本文件和目录操作
- [√] 权限管理
- [√] 构建和打包

### 🚧 开发中
- [ ] 文件搜索功能
- [ ] 批量操作
- [ ] 高级错误处理
- [ ] 性能优化

### 📋 计划中
- [ ] 文件内容预览
- [ ] 压缩/解压缩
- [ ] 文件监听
- [ ] 云存储集成

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个插件！
