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

  // 实用功能
  echo(options: { value: string }): Promise<{ value: string }>;
}
