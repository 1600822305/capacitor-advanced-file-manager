// Web端增强文件访问模块

export interface WebEnhancedOptions {
  /** 是否使用 OPFS (Origin Private File System) */
  useOPFS?: boolean;
  /** 是否启用 IndexedDB 文件缓存 */
  enableIndexedDBCache?: boolean;
  /** 是否使用 Web Workers 进行文件处理 */
  useWebWorkers?: boolean;
  /** 是否启用文件流处理 */
  enableStreaming?: boolean;
  /** 最大缓存大小（字节） */
  maxCacheSize?: number;
}

export interface VirtualFileSystem {
  /** 虚拟文件系统根目录 */
  root: string;
  /** 文件映射表 */
  fileMap: Map<string, VirtualFile>;
  /** 目录映射表 */
  directoryMap: Map<string, VirtualDirectory>;
}

export interface VirtualFile {
  /** 文件路径 */
  path: string;
  /** 文件名 */
  name: string;
  /** 文件内容（可能是 Blob 或 ArrayBuffer） */
  content: Blob | ArrayBuffer | string;
  /** 文件大小 */
  size: number;
  /** MIME 类型 */
  mimeType: string;
  /** 创建时间 */
  createdAt: number;
  /** 修改时间 */
  modifiedAt: number;
  /** 是否在 OPFS 中 */
  inOPFS?: boolean;
  /** 是否在 IndexedDB 中 */
  inIndexedDB?: boolean;
  /** 文件句柄（File System Access API） */
  handle?: FileSystemFileHandle;
}

export interface VirtualDirectory {
  /** 目录路径 */
  path: string;
  /** 目录名 */
  name: string;
  /** 子文件列表 */
  files: string[];
  /** 子目录列表 */
  directories: string[];
  /** 创建时间 */
  createdAt: number;
  /** 修改时间 */
  modifiedAt: number;
  /** 目录句柄（File System Access API） */
  handle?: FileSystemDirectoryHandle;
}

export interface WebFileAccessResult {
  /** 是否成功 */
  success: boolean;
  /** 访问方式 */
  method: 'file-system-access' | 'opfs' | 'indexeddb' | 'memory' | 'drag-drop' | 'input-element';
  /** 错误信息 */
  error?: string;
  /** 数据 */
  data?: any;
}

export interface DragDropOptions {
  /** 允许的文件类型 */
  allowedTypes?: string[];
  /** 最大文件大小 */
  maxFileSize?: number;
  /** 是否允许多文件 */
  multiple?: boolean;
  /** 是否允许目录 */
  allowDirectories?: boolean;
}

export interface WebEnhancedFileManagerPlugin {
  /**
   * 初始化增强的Web文件系统
   */
  initializeWebFS(options?: WebEnhancedOptions): Promise<WebFileAccessResult>;

  /**
   * 检查浏览器支持的文件访问方式
   */
  checkWebCapabilities(): Promise<{
    fileSystemAccess: boolean;
    opfs: boolean;
    indexedDB: boolean;
    webWorkers: boolean;
    streams: boolean;
    dragDrop: boolean;
  }>;

  /**
   * 通过拖拽方式访问文件
   */
  enableDragDropAccess(
    element: HTMLElement,
    options?: DragDropOptions,
    callback?: (files: File[], directories?: FileSystemDirectoryHandle[]) => void
  ): Promise<void>;

  /**
   * 通过文件输入元素访问文件
   */
  createFileInput(options?: {
    multiple?: boolean;
    accept?: string;
    directory?: boolean;
  }): Promise<HTMLInputElement>;

  /**
   * 使用 OPFS (Origin Private File System) 存储文件
   */
  storeInOPFS(path: string, content: Blob | ArrayBuffer): Promise<WebFileAccessResult>;

  /**
   * 从 OPFS 读取文件
   */
  readFromOPFS(path: string): Promise<WebFileAccessResult>;

  /**
   * 使用 IndexedDB 缓存文件
   */
  cacheInIndexedDB(path: string, content: Blob | ArrayBuffer, metadata?: any): Promise<WebFileAccessResult>;

  /**
   * 从 IndexedDB 读取缓存文件
   */
  readFromIndexedDB(path: string): Promise<WebFileAccessResult>;

  /**
   * 创建虚拟文件系统
   */
  createVirtualFS(): Promise<VirtualFileSystem>;

  /**
   * 在虚拟文件系统中添加文件
   */
  addToVirtualFS(vfs: VirtualFileSystem, file: File | Blob, path: string): Promise<void>;

  /**
   * 从虚拟文件系统读取文件
   */
  readFromVirtualFS(vfs: VirtualFileSystem, path: string): Promise<VirtualFile | null>;

  /**
   * 使用 Web Workers 处理大文件
   */
  processFileWithWorker(
    file: File | Blob,
    operation: 'hash' | 'compress' | 'encrypt' | 'analyze',
    options?: any
  ): Promise<any>;

  /**
   * 流式处理大文件
   */
  streamProcessFile(
    file: File,
    processor: (chunk: Uint8Array) => Promise<void>,
    chunkSize?: number
  ): Promise<void>;

  /**
   * 突破文件大小限制的分片上传
   */
  chunkedFileUpload(
    file: File,
    uploadUrl: string,
    chunkSize?: number,
    progressCallback?: (progress: number) => void
  ): Promise<WebFileAccessResult>;

  /**
   * 突破下载限制的分片下载
   */
  chunkedFileDownload(
    url: string,
    progressCallback?: (progress: number) => void
  ): Promise<Blob>;

  /**
   * 使用 Service Worker 缓存文件
   */
  cacheWithServiceWorker(files: File[]): Promise<WebFileAccessResult>;

  /**
   * 绕过CORS限制的代理请求
   */
  proxyRequest(url: string, options?: RequestInit): Promise<Response>;

  /**
   * 创建文件下载链接（突破下载限制）
   */
  createDownloadLink(
    content: Blob | string,
    filename: string,
    options?: { autoDownload?: boolean }
  ): Promise<string>;

  /**
   * 使用 WebRTC 进行P2P文件传输
   */
  initP2PFileTransfer(): Promise<{
    sendFile: (file: File, peerId: string) => Promise<void>;
    receiveFile: (callback: (file: File) => void) => void;
  }>;

  /**
   * 使用 WebAssembly 进行高性能文件处理
   */
  wasmFileProcess(
    file: File,
    wasmModule: string,
    operation: string
  ): Promise<ArrayBuffer>;

  /**
   * 突破存储限制的分布式存储
   */
  distributedStorage(
    file: File,
    storageProviders: ('opfs' | 'indexeddb' | 'localstorage' | 'cache')[]
  ): Promise<{
    stored: boolean;
    providers: string[];
    retrieveKey: string;
  }>;
}
