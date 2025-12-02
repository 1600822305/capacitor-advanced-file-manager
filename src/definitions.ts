// 文件/目录信息接口
export interface FileInfo {
  name: string;
  path: string;
  size: number;
  type: 'file' | 'directory';
  mtime: number; // 修改时间戳
  ctime: number; // 创建时间戳
  permissions?: string;
  isHidden?: boolean;
}

// 目录列表选项
export interface ListDirectoryOptions {
  path: string;
  showHidden?: boolean;
  sortBy?: 'name' | 'size' | 'mtime' | 'type';
  sortOrder?: 'asc' | 'desc';
}

// 目录列表结果
export interface ListDirectoryResult {
  files: FileInfo[];
  totalCount: number;
}

// 文件操作选项
export interface FileOperationOptions {
  path: string;
}

// 文件创建选项
export interface CreateFileOptions {
  path: string;
  content?: string;
  encoding?: 'utf8' | 'base64';
}

// 文件读取选项
export interface ReadFileOptions {
  path: string;
  encoding?: 'utf8' | 'base64';
}

// 文件读取结果
export interface ReadFileResult {
  content: string;
  encoding: string;
}

// 文件写入选项
export interface WriteFileOptions {
  path: string;
  content: string;
  encoding?: 'utf8' | 'base64';
  append?: boolean;
}

// 文件移动/复制选项
export interface MoveFileOptions {
  sourcePath: string;
  destinationPath: string;
}

export interface CopyFileOptions {
  sourcePath: string;
  destinationPath: string;
  overwrite?: boolean;
}

// 文件重命名选项
export interface RenameFileOptions {
  path: string;
  newName: string;
}

// 目录创建选项
export interface CreateDirectoryOptions {
  path: string;
  recursive?: boolean;
}

// 文件搜索选项
export interface SearchFilesOptions {
  directory: string;
  query: string;
  searchType?: 'name' | 'content' | 'both';
  fileTypes?: string[]; // 文件扩展名过滤
  maxResults?: number;
  recursive?: boolean;
}

// 文件搜索结果
export interface SearchFilesResult {
  files: FileInfo[];
  totalFound: number;
}

// ============ AI 编辑相关接口 ============

// 读取文件行范围选项
export interface ReadFileRangeOptions {
  path: string;
  /** 起始行号 (1-based) */
  startLine: number;
  /** 结束行号 (1-based, 包含) */
  endLine: number;
  /** 编码方式 */
  encoding?: 'utf8' | 'base64';
}

// 读取文件行范围结果
export interface ReadFileRangeResult {
  /** 读取到的内容 */
  content: string;
  /** 文件总行数 */
  totalLines: number;
  /** 实际读取的起始行 */
  startLine: number;
  /** 实际读取的结束行 */
  endLine: number;
  /** 内容哈希（用于冲突检测） */
  rangeHash: string;
}

// 插入内容选项
export interface InsertContentOptions {
  path: string;
  /** 插入位置的行号 (1-based)，内容将插入到该行之前 */
  line: number;
  /** 要插入的内容 */
  content: string;
}

// 查找替换选项
export interface ReplaceInFileOptions {
  path: string;
  /** 要查找的字符串或正则表达式 */
  search: string;
  /** 替换为的内容 */
  replace: string;
  /** 是否使用正则表达式 */
  isRegex?: boolean;
  /** 是否替换所有匹配项 */
  replaceAll?: boolean;
  /** 是否区分大小写 */
  caseSensitive?: boolean;
}

// 查找替换结果
export interface ReplaceInFileResult {
  /** 替换的次数 */
  replacements: number;
  /** 是否有修改 */
  modified: boolean;
}

// 应用 Diff 选项
export interface ApplyDiffOptions {
  path: string;
  /** Unified diff 格式的补丁内容 */
  diff: string;
  /** 是否创建备份 */
  createBackup?: boolean;
}

// 应用 Diff 结果
export interface ApplyDiffResult {
  /** 是否成功应用 */
  success: boolean;
  /** 修改的行数 */
  linesChanged: number;
  /** 添加的行数 */
  linesAdded: number;
  /** 删除的行数 */
  linesDeleted: number;
  /** 备份文件路径（如果创建了备份） */
  backupPath?: string;
}

// 获取文件哈希选项
export interface GetFileHashOptions {
  path: string;
  /** 哈希算法 */
  algorithm?: 'md5' | 'sha256';
}

// 获取文件哈希结果
export interface GetFileHashResult {
  /** 文件哈希值 */
  hash: string;
  /** 使用的算法 */
  algorithm: string;
}

// 获取文件行数结果
export interface GetLineCountResult {
  /** 文件行数 */
  lines: number;
}

// 权限检查结果
export interface PermissionResult {
  granted: boolean;
  message?: string;
}

// 系统文件管理器选项
export interface SystemFilePickerOptions {
  /** 选择类型 */
  type: 'file' | 'directory' | 'both';
  /** 是否允许多选 */
  multiple?: boolean;
  /** 文件类型过滤 */
  accept?: string[];
  /** 起始目录 */
  startDirectory?: string;
  /** 标题 */
  title?: string;
}

// 选择的文件信息
export interface SelectedFileInfo {
  /** 文件名 */
  name: string;
  /** 文件路径（可能是真实路径或URI） */
  path: string;
  /** 原始URI（移动端） */
  uri?: string;
  /** 文件大小 */
  size: number;
  /** 文件类型 */
  type: 'file' | 'directory';
  /** MIME类型 */
  mimeType: string;
  /** 修改时间 */
  mtime: number;
  /** 创建时间 */
  ctime: number;
}

// 系统文件管理器结果
export interface SystemFilePickerResult {
  /** 选择的文件信息列表 */
  files: SelectedFileInfo[];
  /** 选择的目录信息列表 */
  directories: SelectedFileInfo[];
  /** 是否被用户取消 */
  cancelled: boolean;
}

// 主插件接口
export interface AdvancedFileManagerPlugin {
  // 权限管理
  requestPermissions(): Promise<PermissionResult>;
  checkPermissions(): Promise<PermissionResult>;

  // 系统文件管理器集成
  openSystemFilePicker(options: SystemFilePickerOptions): Promise<SystemFilePickerResult>;
  openSystemFileManager(path?: string): Promise<void>;
  openFileWithSystemApp(filePath: string, mimeType?: string): Promise<void>;

  // 目录操作
  listDirectory(options: ListDirectoryOptions): Promise<ListDirectoryResult>;
  createDirectory(options: CreateDirectoryOptions): Promise<void>;
  deleteDirectory(options: FileOperationOptions): Promise<void>;

  // 文件操作
  createFile(options: CreateFileOptions): Promise<void>;
  readFile(options: ReadFileOptions): Promise<ReadFileResult>;
  writeFile(options: WriteFileOptions): Promise<void>;
  deleteFile(options: FileOperationOptions): Promise<void>;

  // 文件移动和复制
  moveFile(options: MoveFileOptions): Promise<void>;
  copyFile(options: CopyFileOptions): Promise<void>;
  renameFile(options: RenameFileOptions): Promise<void>;

  // 文件信息
  getFileInfo(options: FileOperationOptions): Promise<FileInfo>;
  exists(options: FileOperationOptions): Promise<{ exists: boolean }>;

  // 搜索功能
  searchFiles(options: SearchFilesOptions): Promise<SearchFilesResult>;

  // ============ AI 编辑相关功能 ============
  
  /** 读取文件指定行范围 */
  readFileRange(options: ReadFileRangeOptions): Promise<ReadFileRangeResult>;
  
  /** 在指定行插入内容 */
  insertContent(options: InsertContentOptions): Promise<void>;
  
  /** 查找并替换文件内容 */
  replaceInFile(options: ReplaceInFileOptions): Promise<ReplaceInFileResult>;
  
  /** 应用 diff 补丁 */
  applyDiff(options: ApplyDiffOptions): Promise<ApplyDiffResult>;
  
  /** 获取文件哈希值 */
  getFileHash(options: GetFileHashOptions): Promise<GetFileHashResult>;
  
  /** 获取文件行数 */
  getLineCount(options: FileOperationOptions): Promise<GetLineCountResult>;

  // 实用功能
  echo(options: { value: string }): Promise<{ value: string }>;
}
