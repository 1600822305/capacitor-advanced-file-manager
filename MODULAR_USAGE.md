# 📦 模块化使用指南

## 🏗️ 架构概述

`capacitor-advanced-file-manager` 采用**单包多模块**架构，既保持了向后兼容性，又提供了灵活的模块化扩展。

### 核心理念
- **核心功能**：保持原有 API 不变，确保向后兼容
- **模块化扩展**：新功能采用独立模块，按需导入
- **类型安全**：完整的 TypeScript 类型定义
- **跨平台**：统一的 API，支持 Android、iOS、Web

## 📋 可用模块

### 🔍 搜索模块 (`/search`)
高级文件搜索功能，支持文件名、内容搜索，正则表达式等。

### 🔄 批量操作模块 (`/batch`)
批量文件操作，支持批量复制、移动、删除，带进度回调。

### 🛠️ 工具模块 (`/utils`) - 开发中
文件类型识别、大小计算、哈希计算等实用工具。

### 👁️ 预览模块 (`/preview`) - 计划中
文件内容预览、缩略图生成等功能。

### 📤 分享模块 (`/share`) - 计划中
文件分享、导出功能。

## 🚀 快速开始

### 1. 安装插件

```bash
npm install capacitor-advanced-file-manager
npx cap sync
```

### 2. 基础使用（保持不变）

```typescript
// 导入核心功能
import { AdvancedFileManager } from 'capacitor-advanced-file-manager';

// 使用基础文件操作
const files = await AdvancedFileManager.listDirectory({
  path: '/storage/emulated/0',
  showHidden: false
});

await AdvancedFileManager.createFile({
  path: '/storage/emulated/0/test.txt',
  content: 'Hello World!'
});
```

### 3. 模块化使用（新功能）

```typescript
// 按需导入模块
import { FileSearch } from 'capacitor-advanced-file-manager/search';
import { BatchOperations } from 'capacitor-advanced-file-manager/batch';
```

## 🔍 搜索模块详细使用

### 基本搜索

```typescript
import { FileSearch } from 'capacitor-advanced-file-manager/search';

// 简单文件名搜索
const results = await FileSearch.search({
  directory: '/storage/emulated/0',
  query: '*.jpg',
  searchType: 'name',
  recursive: true,
  maxResults: 100
});

console.log(`找到 ${results.totalFound} 个文件`);
results.files.forEach(file => {
  console.log(`${file.name} - ${file.size} bytes`);
});
```

### 高级搜索选项

```typescript
// 复杂搜索条件
const advancedResults = await FileSearch.search({
  directory: '/storage/emulated/0/Documents',
  query: 'report',
  searchType: 'both', // 搜索文件名和内容
  recursive: true,
  caseSensitive: false,
  useRegex: false,
  includeHidden: false,
  fileTypes: ['pdf', 'doc', 'docx', 'txt'],
  sizeFilter: {
    min: 1024,      // 最小 1KB
    max: 10485760   // 最大 10MB
  },
  dateFilter: {
    from: Date.now() - (7 * 24 * 60 * 60 * 1000) // 最近7天
  },
  maxResults: 50
});
```

### 异步搜索（带进度）

```typescript
// 异步搜索，适合大目录
const searchHandle = await FileSearch.searchAsync({
  directory: '/storage/emulated/0',
  query: 'photo',
  recursive: true
}, (progress) => {
  console.log(`搜索进度: ${progress.filesSearched} 文件已扫描`);
  console.log(`当前文件: ${progress.currentFile}`);
  console.log(`找到匹配: ${progress.matchesFound}`);
});

// 可以取消搜索
// await searchHandle.cancel();

// 获取最终结果
const finalResults = await FileSearch.getSearchResult(searchHandle.id);
```

### 快速搜索方法

```typescript
// 快速文件名搜索（性能更好）
const quickResults = await FileSearch.quickSearch(
  '/storage/emulated/0/Pictures',
  'IMG_',
  20
);

// 搜索最近修改的文件
const recentFiles = await FileSearch.searchRecentFiles(
  '/storage/emulated/0',
  7, // 最近7天
  50 // 最多50个结果
);

// 搜索大文件
const largeFiles = await FileSearch.searchLargeFiles(
  '/storage/emulated/0',
  100 * 1024 * 1024, // 大于100MB
  10
);

// 查找重复文件
const duplicates = await FileSearch.findDuplicateFiles('/storage/emulated/0');
Object.entries(duplicates).forEach(([key, files]) => {
  console.log(`重复文件组 ${key}:`);
  files.forEach(file => console.log(`  - ${file.path}`));
});
```

## 🔄 批量操作模块详细使用

### 批量复制

```typescript
import { BatchOperations } from 'capacitor-advanced-file-manager/batch';

// 批量复制文件
const copyResult = await BatchOperations.batchCopy([
  {
    sourcePath: '/storage/emulated/0/photo1.jpg',
    destinationPath: '/storage/emulated/0/backup/photo1.jpg'
  },
  {
    sourcePath: '/storage/emulated/0/photo2.jpg',
    destinationPath: '/storage/emulated/0/backup/photo2.jpg'
  }
], {
  overwrite: true,
  continueOnError: true
});

console.log(`复制完成: ${copyResult.successful}/${copyResult.total}`);
console.log(`失败: ${copyResult.failed}, 跳过: ${copyResult.skipped}`);
```

### 批量移动

```typescript
// 批量移动文件
const moveResult = await BatchOperations.batchMove([
  {
    sourcePath: '/storage/emulated/0/temp/file1.txt',
    destinationPath: '/storage/emulated/0/documents/file1.txt'
  },
  {
    sourcePath: '/storage/emulated/0/temp/file2.txt',
    destinationPath: '/storage/emulated/0/documents/file2.txt'
  }
]);
```

### 批量删除

```typescript
// 批量删除文件
const deleteResult = await BatchOperations.batchDelete([
  '/storage/emulated/0/temp/old1.txt',
  '/storage/emulated/0/temp/old2.txt',
  '/storage/emulated/0/temp/old3.txt'
], {
  continueOnError: true,
  skipHidden: true
});
```

### 异步批量操作（带进度）

```typescript
// 异步批量操作，适合大量文件
const batchHandle = await BatchOperations.batchOperateAsync([
  { id: '1', type: 'copy', sourcePath: '/path/to/source1', destinationPath: '/path/to/dest1' },
  { id: '2', type: 'move', sourcePath: '/path/to/source2', destinationPath: '/path/to/dest2' },
  { id: '3', type: 'delete', sourcePath: '/path/to/delete' }
], {
  overwrite: true,
  continueOnError: true,
  concurrency: 3 // 并发执行3个操作
}, (progress) => {
  console.log(`进度: ${progress.percentage.toFixed(1)}%`);
  console.log(`当前操作: ${progress.currentOperation} - ${progress.currentFile}`);
  console.log(`完成: ${progress.completed}, 失败: ${progress.failed}`);
  
  if (progress.estimatedTimeRemaining) {
    console.log(`预计剩余时间: ${Math.round(progress.estimatedTimeRemaining / 1000)}秒`);
  }
});

// 可以暂停、恢复、取消操作
await batchHandle.pause();
await batchHandle.resume();
// await batchHandle.cancel();

// 获取最终结果
const batchResult = await BatchOperations.getBatchResult(batchHandle.id);
```

### 高级批量操作

```typescript
// 批量重命名（按模式）
const renameResult = await BatchOperations.batchRename(
  '/storage/emulated/0/photos',
  'IMG_*.jpg',           // 匹配模式
  'Photo_$1.jpg',        // 替换模式
  {
    recursive: true,
    fileTypes: ['jpg', 'jpeg', 'png']
  }
);

// 批量修改权限
const permissionResult = await BatchOperations.batchChangePermissions([
  '/storage/emulated/0/scripts/script1.sh',
  '/storage/emulated/0/scripts/script2.sh'
], 'rwxr--r--');

// 批量修改时间戳
const timestampResult = await BatchOperations.batchChangeTimestamp([
  '/storage/emulated/0/file1.txt',
  '/storage/emulated/0/file2.txt'
], Date.now());
```

## 🛠️ 工具模块使用（开发中）

```typescript
import { FileUtils } from 'capacitor-advanced-file-manager/utils';

// 获取文件类型信息
const fileType = await FileUtils.getFileTypeInfo('/path/to/file.jpg');
console.log(`类型: ${fileType.category}, MIME: ${fileType.mimeType}`);

// 计算目录大小
const dirSize = await FileUtils.calculateDirectorySize('/storage/emulated/0/Pictures');
console.log(`目录大小: ${FileUtils.formatFileSize(dirSize.totalSize)}`);
console.log(`文件数量: ${dirSize.fileCount}`);

// 异步计算（带进度）
const sizeInfo = await FileUtils.calculateDirectorySizeAsync(
  '/storage/emulated/0',
  (progress) => {
    console.log(`扫描: ${progress.currentPath}`);
    console.log(`已扫描: ${progress.filesScanned} 文件`);
  }
);

// 计算文件哈希
const hash = await FileUtils.calculateFileHash('/path/to/file.txt', 'sha256');
console.log(`SHA256: ${hash.hash}`);

// 查找重复文件
const duplicates = await FileUtils.findDuplicateFiles('/storage/emulated/0', {
  recursive: true,
  minSize: 1024,
  useHash: true
});
```

## 🔧 配置和选项

### 搜索配置

```typescript
interface SearchOptions {
  directory: string;           // 搜索根目录
  query: string;              // 搜索查询
  searchType?: 'name' | 'content' | 'both';
  recursive?: boolean;        // 递归搜索
  caseSensitive?: boolean;    // 区分大小写
  useRegex?: boolean;         // 使用正则表达式
  includeHidden?: boolean;    // 包含隐藏文件
  fileTypes?: string[];       // 文件类型过滤
  maxResults?: number;        // 最大结果数
  sizeFilter?: {              // 大小过滤
    min?: number;
    max?: number;
  };
  dateFilter?: {              // 日期过滤
    from?: number;
    to?: number;
  };
}
```

### 批量操作配置

```typescript
interface BatchOptions {
  overwrite?: boolean;        // 覆盖已存在文件
  continueOnError?: boolean;  // 遇到错误继续
  skipHidden?: boolean;       // 跳过隐藏文件
  concurrency?: number;       // 并发数
  confirmCallback?: (operation: BatchOperation) => Promise<boolean>;
}
```

## 🚨 错误处理

```typescript
try {
  const results = await FileSearch.search({
    directory: '/invalid/path',
    query: 'test'
  });
} catch (error) {
  if (error.message.includes('permission')) {
    console.log('权限不足，请检查文件访问权限');
  } else if (error.message.includes('not found')) {
    console.log('目录不存在');
  } else {
    console.log('搜索失败:', error.message);
  }
}
```

## 📱 平台差异

### Android
- 完整功能支持
- 需要存储权限
- 支持外部存储访问

### iOS  
- 限制在应用沙盒内
- 无需额外权限配置
- 支持应用文档目录

### Web
- 基于 File System Access API
- 功能受浏览器限制
- 需要用户交互选择文件/目录

## 🔄 版本规划

### v0.1.0 - 搜索和批量操作
- ✅ 文件搜索模块
- ✅ 批量操作模块
- 🚧 工具模块

### v0.2.0 - 预览和分享
- 📋 文件预览模块
- 📋 文件分享模块
- 📋 压缩解压模块

### v0.3.0 - 高级功能
- 📋 文件监听模块
- 📋 书签收藏模块
- 📋 回收站模块

## 💡 最佳实践

1. **按需导入**：只导入需要的模块，减少包大小
2. **错误处理**：始终使用 try-catch 处理异步操作
3. **进度回调**：大量文件操作时使用进度回调提升用户体验
4. **权限检查**：操作前检查必要的权限
5. **路径验证**：使用 FileUtils.validatePath 验证路径有效性

## 🤝 贡献

欢迎贡献代码、报告问题或提出功能建议！

- GitHub: https://github.com/your-username/capacitor-advanced-file-manager
- Issues: https://github.com/your-username/capacitor-advanced-file-manager/issues
- NPM: https://www.npmjs.com/package/capacitor-advanced-file-manager
