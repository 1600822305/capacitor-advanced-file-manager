// 文件搜索模块类型定义

export interface SearchOptions {
  /** 搜索的根目录 */
  directory: string;
  /** 搜索查询字符串 */
  query: string;
  /** 搜索类型 */
  searchType?: 'name' | 'content' | 'both';
  /** 是否递归搜索子目录 */
  recursive?: boolean;
  /** 文件类型过滤 */
  fileTypes?: string[];
  /** 最大结果数量 */
  maxResults?: number;
  /** 是否区分大小写 */
  caseSensitive?: boolean;
  /** 是否使用正则表达式 */
  useRegex?: boolean;
  /** 是否包含隐藏文件 */
  includeHidden?: boolean;
  /** 文件大小过滤 */
  sizeFilter?: {
    min?: number; // 最小字节数
    max?: number; // 最大字节数
  };
  /** 修改时间过滤 */
  dateFilter?: {
    from?: number; // 时间戳
    to?: number;   // 时间戳
  };
}

export interface SearchResult {
  /** 匹配的文件列表 */
  files: SearchFileInfo[];
  /** 总匹配数量 */
  totalFound: number;
  /** 搜索耗时（毫秒） */
  searchTime: number;
  /** 是否被截断（达到最大结果数） */
  truncated: boolean;
}

export interface SearchFileInfo {
  /** 文件名 */
  name: string;
  /** 完整路径 */
  path: string;
  /** 文件大小 */
  size: number;
  /** 文件类型 */
  type: 'file' | 'directory';
  /** 修改时间 */
  mtime: number;
  /** 创建时间 */
  ctime: number;
  /** 是否隐藏 */
  isHidden: boolean;
  /** 匹配信息 */
  matchInfo: {
    /** 匹配类型 */
    matchType: 'name' | 'content';
    /** 匹配的文本片段 */
    matchedText?: string;
    /** 匹配位置（内容搜索时） */
    matchPosition?: number;
    /** 匹配行号（文本文件内容搜索时） */
    lineNumber?: number;
  };
}

export interface SearchProgress {
  /** 已搜索的文件数 */
  filesSearched: number;
  /** 已搜索的目录数 */
  directoriesSearched: number;
  /** 当前搜索的文件路径 */
  currentFile: string;
  /** 找到的匹配数 */
  matchesFound: number;
}

export interface SearchHandle {
  /** 搜索ID */
  id: string;
  /** 取消搜索 */
  cancel(): Promise<void>;
}

export interface FileSearchPlugin {
  /**
   * 搜索文件
   */
  search(options: SearchOptions): Promise<SearchResult>;

  /**
   * 异步搜索文件（带进度回调）
   */
  searchAsync(
    options: SearchOptions,
    progressCallback?: (progress: SearchProgress) => void
  ): Promise<SearchHandle>;

  /**
   * 获取搜索结果
   */
  getSearchResult(searchId: string): Promise<SearchResult>;

  /**
   * 取消搜索
   */
  cancelSearch(searchId: string): Promise<void>;

  /**
   * 快速文件名搜索（仅搜索文件名，性能更好）
   */
  quickSearch(directory: string, query: string, maxResults?: number): Promise<SearchFileInfo[]>;

  /**
   * 搜索最近修改的文件
   */
  searchRecentFiles(directory: string, days?: number, maxResults?: number): Promise<SearchFileInfo[]>;

  /**
   * 搜索大文件
   */
  searchLargeFiles(directory: string, minSize: number, maxResults?: number): Promise<SearchFileInfo[]>;

  /**
   * 搜索重复文件（基于文件大小和名称）
   */
  findDuplicateFiles(directory: string): Promise<{ [key: string]: SearchFileInfo[] }>;
}
