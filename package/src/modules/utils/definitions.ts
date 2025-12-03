// 文件工具模块类型定义

export interface FileTypeInfo {
  /** 文件扩展名 */
  extension: string;
  /** MIME 类型 */
  mimeType: string;
  /** 文件类型分类 */
  category: 'image' | 'video' | 'audio' | 'document' | 'archive' | 'executable' | 'code' | 'other';
  /** 图标名称或路径 */
  icon: string;
  /** 是否可预览 */
  previewable: boolean;
  /** 是否可编辑 */
  editable: boolean;
}

export interface DirectorySizeInfo {
  /** 目录路径 */
  path: string;
  /** 总大小（字节） */
  totalSize: number;
  /** 文件数量 */
  fileCount: number;
  /** 目录数量 */
  directoryCount: number;
  /** 最大文件大小 */
  largestFileSize: number;
  /** 最大文件路径 */
  largestFilePath: string;
  /** 计算耗时（毫秒） */
  calculationTime: number;
  /** 按文件类型分组的大小统计 */
  sizeByType: { [category: string]: number };
}

export interface DirectorySizeProgress {
  /** 当前扫描的路径 */
  currentPath: string;
  /** 已扫描的文件数 */
  filesScanned: number;
  /** 已扫描的目录数 */
  directoriesScanned: number;
  /** 当前累计大小 */
  currentSize: number;
  /** 扫描进度百分比（如果可估算） */
  percentage?: number;
}

export interface FileHashResult {
  /** 文件路径 */
  path: string;
  /** 哈希算法 */
  algorithm: 'md5' | 'sha1' | 'sha256';
  /** 哈希值 */
  hash: string;
  /** 计算耗时（毫秒） */
  calculationTime: number;
}

export interface DuplicateFileGroup {
  /** 文件大小 */
  size: number;
  /** 哈希值 */
  hash?: string;
  /** 重复文件列表 */
  files: string[];
  /** 可节省的空间（字节） */
  wastedSpace: number;
}

export interface FileComparisonResult {
  /** 是否相同 */
  identical: boolean;
  /** 文件1路径 */
  file1: string;
  /** 文件2路径 */
  file2: string;
  /** 差异类型 */
  differences: {
    size: boolean;
    content: boolean;
    timestamp: boolean;
  };
  /** 相似度百分比（0-100） */
  similarity: number;
}

export interface StorageInfo {
  /** 总空间（字节） */
  totalSpace: number;
  /** 可用空间（字节） */
  availableSpace: number;
  /** 已用空间（字节） */
  usedSpace: number;
  /** 使用率百分比 */
  usagePercentage: number;
  /** 存储路径 */
  path: string;
  /** 文件系统类型 */
  fileSystem?: string;
}

export interface FileUtilsPlugin {
  /**
   * 获取文件类型信息
   */
  getFileTypeInfo(filePath: string): Promise<FileTypeInfo>;

  /**
   * 根据扩展名获取文件类型信息
   */
  getFileTypeByExtension(extension: string): Promise<FileTypeInfo>;

  /**
   * 计算目录大小
   */
  calculateDirectorySize(path: string): Promise<DirectorySizeInfo>;

  /**
   * 异步计算目录大小（带进度回调）
   */
  calculateDirectorySizeAsync(
    path: string,
    progressCallback?: (progress: DirectorySizeProgress) => void
  ): Promise<DirectorySizeInfo>;

  /**
   * 计算文件哈希值
   */
  calculateFileHash(
    path: string,
    algorithm?: 'md5' | 'sha1' | 'sha256'
  ): Promise<FileHashResult>;

  /**
   * 查找重复文件
   */
  findDuplicateFiles(
    directory: string,
    options?: {
      recursive?: boolean;
      minSize?: number;
      useHash?: boolean;
    }
  ): Promise<DuplicateFileGroup[]>;

  /**
   * 比较两个文件
   */
  compareFiles(file1: string, file2: string): Promise<FileComparisonResult>;

  /**
   * 获取存储空间信息
   */
  getStorageInfo(path?: string): Promise<StorageInfo>;

  /**
   * 格式化文件大小
   */
  formatFileSize(bytes: number, precision?: number): Promise<string>;

  /**
   * 解析文件大小字符串
   */
  parseFileSize(sizeString: string): Promise<number>;

  /**
   * 获取文件扩展名
   */
  getFileExtension(filePath: string): Promise<string>;

  /**
   * 获取文件名（不含扩展名）
   */
  getFileNameWithoutExtension(filePath: string): Promise<string>;

  /**
   * 生成唯一文件名
   */
  generateUniqueFileName(directory: string, baseName: string): Promise<string>;

  /**
   * 验证文件路径
   */
  validatePath(path: string): Promise<{ valid: boolean; reason?: string }>;

  /**
   * 清理临时文件
   */
  cleanupTempFiles(directory: string, olderThanDays?: number): Promise<{
    deletedCount: number;
    freedSpace: number;
  }>;

  /**
   * 获取文件MIME类型
   */
  getMimeType(filePath: string): Promise<string>;

  /**
   * 检查文件是否为文本文件
   */
  isTextFile(filePath: string): Promise<boolean>;

  /**
   * 检查文件是否为图片文件
   */
  isImageFile(filePath: string): Promise<boolean>;

  /**
   * 检查文件是否为视频文件
   */
  isVideoFile(filePath: string): Promise<boolean>;

  /**
   * 检查文件是否为音频文件
   */
  isAudioFile(filePath: string): Promise<boolean>;
}
