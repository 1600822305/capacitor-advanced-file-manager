# capacitor-advanced-file-manager

Advanced file manager plugin for Capacitor with comprehensive file system operations including browse, create, edit, delete, move, copy, and search files and directories.

## 🏗️ 架构特性

- **核心功能**：完整的文件系统操作 API
- **模块化扩展**：按需导入高级功能模块
- **跨平台支持**：Android、iOS、Web 统一 API
- **类型安全**：完整的 TypeScript 类型定义

## 📦 可用模块

| 模块 | 功能 | 状态 |
|------|------|------|
| 核心 | 基础文件操作 | ✅ 已完成 |
| `/search` | 高级文件搜索 | ✅ 已完成 |
| `/batch` | 批量文件操作 | ✅ 已完成 |
| `/utils` | 文件工具集 | 🚧 开发中 |
| `/web-enhanced` | Web端增强功能 | ✅ 已完成 |
| `/preview` | 文件预览 | 📋 计划中 |
| `/share` | 文件分享 | 📋 计划中 |

## Install

```bash
npm install capacitor-advanced-file-manager
npx cap sync
```

## 🚀 快速开始

### 基础使用

```typescript
import { AdvancedFileManager } from 'capacitor-advanced-file-manager';

// 列出目录内容
const files = await AdvancedFileManager.listDirectory({
  path: '/storage/emulated/0',
  showHidden: false
});

// 创建文件
await AdvancedFileManager.createFile({
  path: '/storage/emulated/0/test.txt',
  content: 'Hello World!'
});
```

### 模块化使用

```typescript
// 按需导入高级功能模块
import { FileSearch } from 'capacitor-advanced-file-manager/search';
import { BatchOperations } from 'capacitor-advanced-file-manager/batch';
import { WebEnhancedFileManager } from 'capacitor-advanced-file-manager/web-enhanced';

// 文件搜索
const results = await FileSearch.search({
  directory: '/storage/emulated/0',
  query: '*.jpg',
  recursive: true
});

// 批量操作
await BatchOperations.batchDelete([
  '/path/to/file1.txt',
  '/path/to/file2.txt'
]);

// Web端增强功能 - 突破浏览器文件限制
await WebEnhancedFileManager.initializeWebFS({
  useOPFS: true,              // 使用 Origin Private File System
  enableIndexedDBCache: true, // 启用 IndexedDB 缓存
  useWebWorkers: true         // 使用 Web Workers 处理大文件
});
```

📖 **详细使用指南**：
- [模块化使用指南](./MODULAR_USAGE.md)
- [Web端突破限制完全指南](./WEB_BREAKTHROUGH_GUIDE.md) 🌐

## API

<docgen-index>

* [`requestPermissions()`](#requestpermissions)
* [`checkPermissions()`](#checkpermissions)
* [`openSystemFilePicker(...)`](#opensystemfilepicker)
* [`openSystemFileManager(...)`](#opensystemfilemanager)
* [`openFileWithSystemApp(...)`](#openfilewithsystemapp)
* [`listDirectory(...)`](#listdirectory)
* [`createDirectory(...)`](#createdirectory)
* [`deleteDirectory(...)`](#deletedirectory)
* [`createFile(...)`](#createfile)
* [`readFile(...)`](#readfile)
* [`writeFile(...)`](#writefile)
* [`deleteFile(...)`](#deletefile)
* [`moveFile(...)`](#movefile)
* [`copyFile(...)`](#copyfile)
* [`renameFile(...)`](#renamefile)
* [`getFileInfo(...)`](#getfileinfo)
* [`exists(...)`](#exists)
* [`searchFiles(...)`](#searchfiles)
* [`echo(...)`](#echo)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionResult>
```

**Returns:** <code>Promise&lt;<a href="#permissionresult">PermissionResult</a>&gt;</code>

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionResult>
```

**Returns:** <code>Promise&lt;<a href="#permissionresult">PermissionResult</a>&gt;</code>

--------------------


### openSystemFilePicker(...)

```typescript
openSystemFilePicker(options: SystemFilePickerOptions) => Promise<SystemFilePickerResult>
```

| Param         | Type                                                                        |
| ------------- | --------------------------------------------------------------------------- |
| **`options`** | <code><a href="#systemfilepickeroptions">SystemFilePickerOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#systemfilepickerresult">SystemFilePickerResult</a>&gt;</code>

--------------------


### openSystemFileManager(...)

```typescript
openSystemFileManager(path?: string | undefined) => Promise<void>
```

| Param      | Type                |
| ---------- | ------------------- |
| **`path`** | <code>string</code> |

--------------------


### openFileWithSystemApp(...)

```typescript
openFileWithSystemApp(filePath: string, mimeType?: string | undefined) => Promise<void>
```

| Param          | Type                |
| -------------- | ------------------- |
| **`filePath`** | <code>string</code> |
| **`mimeType`** | <code>string</code> |

--------------------


### listDirectory(...)

```typescript
listDirectory(options: ListDirectoryOptions) => Promise<ListDirectoryResult>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#listdirectoryoptions">ListDirectoryOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#listdirectoryresult">ListDirectoryResult</a>&gt;</code>

--------------------


### createDirectory(...)

```typescript
createDirectory(options: CreateDirectoryOptions) => Promise<void>
```

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#createdirectoryoptions">CreateDirectoryOptions</a></code> |

--------------------


### deleteDirectory(...)

```typescript
deleteDirectory(options: FileOperationOptions) => Promise<void>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#fileoperationoptions">FileOperationOptions</a></code> |

--------------------


### createFile(...)

```typescript
createFile(options: CreateFileOptions) => Promise<void>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#createfileoptions">CreateFileOptions</a></code> |

--------------------


### readFile(...)

```typescript
readFile(options: ReadFileOptions) => Promise<ReadFileResult>
```

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#readfileoptions">ReadFileOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#readfileresult">ReadFileResult</a>&gt;</code>

--------------------


### writeFile(...)

```typescript
writeFile(options: WriteFileOptions) => Promise<void>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#writefileoptions">WriteFileOptions</a></code> |

--------------------


### deleteFile(...)

```typescript
deleteFile(options: FileOperationOptions) => Promise<void>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#fileoperationoptions">FileOperationOptions</a></code> |

--------------------


### moveFile(...)

```typescript
moveFile(options: MoveFileOptions) => Promise<void>
```

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#movefileoptions">MoveFileOptions</a></code> |

--------------------


### copyFile(...)

```typescript
copyFile(options: CopyFileOptions) => Promise<void>
```

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#copyfileoptions">CopyFileOptions</a></code> |

--------------------


### renameFile(...)

```typescript
renameFile(options: RenameFileOptions) => Promise<void>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#renamefileoptions">RenameFileOptions</a></code> |

--------------------


### getFileInfo(...)

```typescript
getFileInfo(options: FileOperationOptions) => Promise<FileInfo>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#fileoperationoptions">FileOperationOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#fileinfo">FileInfo</a>&gt;</code>

--------------------


### exists(...)

```typescript
exists(options: FileOperationOptions) => Promise<{ exists: boolean; }>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#fileoperationoptions">FileOperationOptions</a></code> |

**Returns:** <code>Promise&lt;{ exists: boolean; }&gt;</code>

--------------------


### searchFiles(...)

```typescript
searchFiles(options: SearchFilesOptions) => Promise<SearchFilesResult>
```

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#searchfilesoptions">SearchFilesOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#searchfilesresult">SearchFilesResult</a>&gt;</code>

--------------------


### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### Interfaces


#### PermissionResult

| Prop          | Type                 |
| ------------- | -------------------- |
| **`granted`** | <code>boolean</code> |
| **`message`** | <code>string</code>  |


#### SystemFilePickerResult

| Prop              | Type                            | Description |
| ----------------- | ------------------------------- | ----------- |
| **`files`**       | <code>SelectedFileInfo[]</code> | 选择的文件信息列表   |
| **`directories`** | <code>SelectedFileInfo[]</code> | 选择的目录信息列表   |
| **`cancelled`**   | <code>boolean</code>            | 是否被用户取消     |


#### SelectedFileInfo

| Prop           | Type                               | Description       |
| -------------- | ---------------------------------- | ----------------- |
| **`name`**     | <code>string</code>                | 文件名               |
| **`path`**     | <code>string</code>                | 文件路径（可能是真实路径或URI） |
| **`uri`**      | <code>string</code>                | 原始URI（移动端）        |
| **`size`**     | <code>number</code>                | 文件大小              |
| **`type`**     | <code>'file' \| 'directory'</code> | 文件类型              |
| **`mimeType`** | <code>string</code>                | MIME类型            |
| **`mtime`**    | <code>number</code>                | 修改时间              |
| **`ctime`**    | <code>number</code>                | 创建时间              |


#### SystemFilePickerOptions

| Prop                 | Type                                         | Description |
| -------------------- | -------------------------------------------- | ----------- |
| **`type`**           | <code>'file' \| 'directory' \| 'both'</code> | 选择类型        |
| **`multiple`**       | <code>boolean</code>                         | 是否允许多选      |
| **`accept`**         | <code>string[]</code>                        | 文件类型过滤      |
| **`startDirectory`** | <code>string</code>                          | 起始目录        |
| **`title`**          | <code>string</code>                          | 标题          |


#### ListDirectoryResult

| Prop             | Type                    |
| ---------------- | ----------------------- |
| **`files`**      | <code>FileInfo[]</code> |
| **`totalCount`** | <code>number</code>     |


#### FileInfo

| Prop              | Type                               |
| ----------------- | ---------------------------------- |
| **`name`**        | <code>string</code>                |
| **`path`**        | <code>string</code>                |
| **`size`**        | <code>number</code>                |
| **`type`**        | <code>'file' \| 'directory'</code> |
| **`mtime`**       | <code>number</code>                |
| **`ctime`**       | <code>number</code>                |
| **`permissions`** | <code>string</code>                |
| **`isHidden`**    | <code>boolean</code>               |


#### ListDirectoryOptions

| Prop             | Type                                               |
| ---------------- | -------------------------------------------------- |
| **`path`**       | <code>string</code>                                |
| **`showHidden`** | <code>boolean</code>                               |
| **`sortBy`**     | <code>'name' \| 'size' \| 'mtime' \| 'type'</code> |
| **`sortOrder`**  | <code>'asc' \| 'desc'</code>                       |


#### CreateDirectoryOptions

| Prop            | Type                 |
| --------------- | -------------------- |
| **`path`**      | <code>string</code>  |
| **`recursive`** | <code>boolean</code> |


#### FileOperationOptions

| Prop       | Type                |
| ---------- | ------------------- |
| **`path`** | <code>string</code> |


#### CreateFileOptions

| Prop           | Type                            |
| -------------- | ------------------------------- |
| **`path`**     | <code>string</code>             |
| **`content`**  | <code>string</code>             |
| **`encoding`** | <code>'utf8' \| 'base64'</code> |


#### ReadFileResult

| Prop           | Type                |
| -------------- | ------------------- |
| **`content`**  | <code>string</code> |
| **`encoding`** | <code>string</code> |


#### ReadFileOptions

| Prop           | Type                            |
| -------------- | ------------------------------- |
| **`path`**     | <code>string</code>             |
| **`encoding`** | <code>'utf8' \| 'base64'</code> |


#### WriteFileOptions

| Prop           | Type                            |
| -------------- | ------------------------------- |
| **`path`**     | <code>string</code>             |
| **`content`**  | <code>string</code>             |
| **`encoding`** | <code>'utf8' \| 'base64'</code> |
| **`append`**   | <code>boolean</code>            |


#### MoveFileOptions

| Prop                  | Type                |
| --------------------- | ------------------- |
| **`sourcePath`**      | <code>string</code> |
| **`destinationPath`** | <code>string</code> |


#### CopyFileOptions

| Prop                  | Type                 |
| --------------------- | -------------------- |
| **`sourcePath`**      | <code>string</code>  |
| **`destinationPath`** | <code>string</code>  |
| **`overwrite`**       | <code>boolean</code> |


#### RenameFileOptions

| Prop          | Type                |
| ------------- | ------------------- |
| **`path`**    | <code>string</code> |
| **`newName`** | <code>string</code> |


#### SearchFilesResult

| Prop             | Type                    |
| ---------------- | ----------------------- |
| **`files`**      | <code>FileInfo[]</code> |
| **`totalFound`** | <code>number</code>     |


#### SearchFilesOptions

| Prop             | Type                                       |
| ---------------- | ------------------------------------------ |
| **`directory`**  | <code>string</code>                        |
| **`query`**      | <code>string</code>                        |
| **`searchType`** | <code>'name' \| 'content' \| 'both'</code> |
| **`fileTypes`**  | <code>string[]</code>                      |
| **`maxResults`** | <code>number</code>                        |
| **`recursive`**  | <code>boolean</code>                       |

</docgen-api>
