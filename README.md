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
* [`searchContent(...)`](#searchcontent)
* [`readFileRange(...)`](#readfilerange)
* [`insertContent(...)`](#insertcontent)
* [`replaceInFile(...)`](#replaceinfile)
* [`applyDiff(...)`](#applydiff)
* [`getFileHash(...)`](#getfilehash)
* [`getLineCount(...)`](#getlinecount)
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


### searchContent(...)

```typescript
searchContent(options: SearchContentOptions) => Promise<SearchContentResult>
```

原生层内容搜索（避免 OOM）
在原生层执行搜索，只返回匹配结果，不返回完整文件内容

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#searchcontentoptions">SearchContentOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#searchcontentresult">SearchContentResult</a>&gt;</code>

--------------------


### readFileRange(...)

```typescript
readFileRange(options: ReadFileRangeOptions) => Promise<ReadFileRangeResult>
```

读取文件指定行范围

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#readfilerangeoptions">ReadFileRangeOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#readfilerangeresult">ReadFileRangeResult</a>&gt;</code>

--------------------


### insertContent(...)

```typescript
insertContent(options: InsertContentOptions) => Promise<void>
```

在指定行插入内容

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#insertcontentoptions">InsertContentOptions</a></code> |

--------------------


### replaceInFile(...)

```typescript
replaceInFile(options: ReplaceInFileOptions) => Promise<ReplaceInFileResult>
```

查找并替换文件内容

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#replaceinfileoptions">ReplaceInFileOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#replaceinfileresult">ReplaceInFileResult</a>&gt;</code>

--------------------


### applyDiff(...)

```typescript
applyDiff(options: ApplyDiffOptions) => Promise<ApplyDiffResult>
```

应用 diff 补丁

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#applydiffoptions">ApplyDiffOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#applydiffresult">ApplyDiffResult</a>&gt;</code>

--------------------


### getFileHash(...)

```typescript
getFileHash(options: GetFileHashOptions) => Promise<GetFileHashResult>
```

获取文件哈希值

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#getfilehashoptions">GetFileHashOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#getfilehashresult">GetFileHashResult</a>&gt;</code>

--------------------


### getLineCount(...)

```typescript
getLineCount(options: FileOperationOptions) => Promise<GetLineCountResult>
```

获取文件行数

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#fileoperationoptions">FileOperationOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#getlinecountresult">GetLineCountResult</a>&gt;</code>

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


#### SearchContentResult

内容搜索结果

| Prop               | Type                                   | Description       |
| ------------------ | -------------------------------------- | ----------------- |
| **`results`**      | <code>ContentSearchFileResult[]</code> | 搜索结果列表            |
| **`totalFiles`**   | <code>number</code>                    | 总匹配文件数            |
| **`totalMatches`** | <code>number</code>                    | 总匹配数              |
| **`duration`**     | <code>number</code>                    | 搜索耗时（毫秒）          |
| **`skippedFiles`** | <code>number</code>                    | 被跳过的文件数（因文件过大等原因） |


#### ContentSearchFileResult

单个文件的内容搜索结果

| Prop            | Type                                           | Description |
| --------------- | ---------------------------------------------- | ----------- |
| **`path`**      | <code>string</code>                            | 文件路径        |
| **`name`**      | <code>string</code>                            | 文件名         |
| **`matchType`** | <code>'content' \| 'both' \| 'filename'</code> | 匹配类型        |
| **`matches`**   | <code>ContentMatch[]</code>                    | 匹配列表        |
| **`score`**     | <code>number</code>                            | 相关性评分       |


#### ContentMatch

内容搜索匹配项

| Prop              | Type                | Description         |
| ----------------- | ------------------- | ------------------- |
| **`lineNumber`**  | <code>number</code> | 匹配的行号 (1-based)     |
| **`lineContent`** | <code>string</code> | 匹配的行内容              |
| **`context`**     | <code>string</code> | 匹配的上下文（带高亮标记位置）     |
| **`matchStart`**  | <code>number</code> | 匹配开始位置（在 context 中） |
| **`matchEnd`**    | <code>number</code> | 匹配结束位置（在 context 中） |


#### SearchContentOptions

内容搜索选项

| Prop                    | Type                  | Description                |
| ----------------------- | --------------------- | -------------------------- |
| **`directory`**         | <code>string</code>   | 搜索目录                       |
| **`keyword`**           | <code>string</code>   | 搜索关键词                      |
| **`caseSensitive`**     | <code>boolean</code>  | 是否区分大小写                    |
| **`fileExtensions`**    | <code>string[]</code> | 文件扩展名过滤（如 ['.md', '.txt']） |
| **`maxFiles`**          | <code>number</code>   | 最大搜索文件数                    |
| **`maxFileSize`**       | <code>number</code>   | 最大文件大小（字节），超过的文件将被跳过       |
| **`maxMatchesPerFile`** | <code>number</code>   | 每个文件最大匹配数                  |
| **`contextLength`**     | <code>number</code>   | 上下文长度（匹配前后的字符数）            |
| **`maxDepth`**          | <code>number</code>   | 最大递归深度                     |
| **`recursive`**         | <code>boolean</code>  | 是否递归搜索子目录                  |


#### ReadFileRangeResult

| Prop             | Type                | Description  |
| ---------------- | ------------------- | ------------ |
| **`content`**    | <code>string</code> | 读取到的内容       |
| **`totalLines`** | <code>number</code> | 文件总行数        |
| **`startLine`**  | <code>number</code> | 实际读取的起始行     |
| **`endLine`**    | <code>number</code> | 实际读取的结束行     |
| **`rangeHash`**  | <code>string</code> | 内容哈希（用于冲突检测） |


#### ReadFileRangeOptions

| Prop            | Type                            | Description        |
| --------------- | ------------------------------- | ------------------ |
| **`path`**      | <code>string</code>             |                    |
| **`startLine`** | <code>number</code>             | 起始行号 (1-based)     |
| **`endLine`**   | <code>number</code>             | 结束行号 (1-based, 包含) |
| **`encoding`**  | <code>'utf8' \| 'base64'</code> | 编码方式               |


#### InsertContentOptions

| Prop          | Type                | Description                  |
| ------------- | ------------------- | ---------------------------- |
| **`path`**    | <code>string</code> |                              |
| **`line`**    | <code>number</code> | 插入位置的行号 (1-based)，内容将插入到该行之前 |
| **`content`** | <code>string</code> | 要插入的内容                       |


#### ReplaceInFileResult

| Prop               | Type                 | Description |
| ------------------ | -------------------- | ----------- |
| **`replacements`** | <code>number</code>  | 替换的次数       |
| **`modified`**     | <code>boolean</code> | 是否有修改       |


#### ReplaceInFileOptions

| Prop                | Type                 | Description   |
| ------------------- | -------------------- | ------------- |
| **`path`**          | <code>string</code>  |               |
| **`search`**        | <code>string</code>  | 要查找的字符串或正则表达式 |
| **`replace`**       | <code>string</code>  | 替换为的内容        |
| **`isRegex`**       | <code>boolean</code> | 是否使用正则表达式     |
| **`replaceAll`**    | <code>boolean</code> | 是否替换所有匹配项     |
| **`caseSensitive`** | <code>boolean</code> | 是否区分大小写       |


#### ApplyDiffResult

| Prop               | Type                 | Description     |
| ------------------ | -------------------- | --------------- |
| **`success`**      | <code>boolean</code> | 是否成功应用          |
| **`linesChanged`** | <code>number</code>  | 修改的行数           |
| **`linesAdded`**   | <code>number</code>  | 添加的行数           |
| **`linesDeleted`** | <code>number</code>  | 删除的行数           |
| **`backupPath`**   | <code>string</code>  | 备份文件路径（如果创建了备份） |


#### ApplyDiffOptions

| Prop               | Type                 | Description          |
| ------------------ | -------------------- | -------------------- |
| **`path`**         | <code>string</code>  |                      |
| **`diff`**         | <code>string</code>  | Unified diff 格式的补丁内容 |
| **`createBackup`** | <code>boolean</code> | 是否创建备份               |


#### GetFileHashResult

| Prop            | Type                | Description |
| --------------- | ------------------- | ----------- |
| **`hash`**      | <code>string</code> | 文件哈希值       |
| **`algorithm`** | <code>string</code> | 使用的算法       |


#### GetFileHashOptions

| Prop            | Type                           | Description |
| --------------- | ------------------------------ | ----------- |
| **`path`**      | <code>string</code>            |             |
| **`algorithm`** | <code>'md5' \| 'sha256'</code> | 哈希算法        |


#### GetLineCountResult

| Prop        | Type                | Description |
| ----------- | ------------------- | ----------- |
| **`lines`** | <code>number</code> | 文件行数        |

</docgen-api>
