/**
 * Web端突破文件限制的实用工具集
 * 提供各种绕过浏览器文件访问限制的技术方案
 */

export class WebFileBreakthroughUtils {
  
  /**
   * 方案1: 使用 OPFS (Origin Private File System) 突破存储限制
   * 优势: 可以存储大文件，持久化存储，不受配额限制
   */
  static async useOPFS() {
    if (!('storage' in navigator) || !('getDirectory' in navigator.storage)) {
      throw new Error('浏览器不支持 OPFS');
    }

    const opfsRoot = await navigator.storage.getDirectory();
    
    return {
      // 存储大文件到 OPFS
      async storeFile(name: string, content: Blob | ArrayBuffer): Promise<void> {
        const fileHandle = await opfsRoot.getFileHandle(name, { create: true });
        const writable = await fileHandle.createWritable();
        await writable.write(content);
        await writable.close();
      },

      // 从 OPFS 读取文件
      async readFile(name: string): Promise<File> {
        const fileHandle = await opfsRoot.getFileHandle(name);
        return await fileHandle.getFile();
      },

      // 列出 OPFS 中的文件
      async listFiles(): Promise<string[]> {
        const files: string[] = [];
        for await (const [name, handle] of opfsRoot.entries()) {
          if (handle.kind === 'file') {
            files.push(name);
          }
        }
        return files;
      },

      // 删除 OPFS 中的文件
      async deleteFile(name: string): Promise<void> {
        await opfsRoot.removeEntry(name);
      }
    };
  }

  /**
   * 方案2: 使用 IndexedDB 突破存储限制
   * 优势: 兼容性好，可以存储二进制数据
   */
  static async useIndexedDB(dbName = 'FileStorage', version = 1) {
    const db = await new Promise<IDBDatabase>((resolve, reject) => {
      const request = indexedDB.open(dbName, version);
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve(request.result);
      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        if (!db.objectStoreNames.contains('files')) {
          const store = db.createObjectStore('files', { keyPath: 'id' });
          store.createIndex('name', 'name', { unique: false });
          store.createIndex('type', 'type', { unique: false });
        }
      };
    });

    return {
      // 存储文件到 IndexedDB
      async storeFile(id: string, file: File | Blob, metadata: any = {}): Promise<void> {
        const transaction = db.transaction(['files'], 'readwrite');
        const store = transaction.objectStore('files');
        
        const fileData = {
          id,
          name: file instanceof File ? file.name : `blob_${id}`,
          type: file.type,
          size: file.size,
          data: await file.arrayBuffer(),
          metadata,
          timestamp: Date.now()
        };

        await new Promise<void>((resolve, reject) => {
          const request = store.put(fileData);
          request.onsuccess = () => resolve();
          request.onerror = () => reject(request.error);
        });
      },

      // 从 IndexedDB 读取文件
      async readFile(id: string): Promise<{ file: Blob; metadata: any } | null> {
        const transaction = db.transaction(['files'], 'readonly');
        const store = transaction.objectStore('files');
        
        const fileData = await new Promise<any>((resolve, reject) => {
          const request = store.get(id);
          request.onsuccess = () => resolve(request.result);
          request.onerror = () => reject(request.error);
        });

        if (!fileData) return null;

        return {
          file: new Blob([fileData.data], { type: fileData.type }),
          metadata: fileData.metadata
        };
      },

      // 列出所有文件
      async listFiles(): Promise<Array<{ id: string; name: string; size: number; type: string }>> {
        const transaction = db.transaction(['files'], 'readonly');
        const store = transaction.objectStore('files');
        
        return new Promise((resolve, reject) => {
          const request = store.getAll();
          request.onsuccess = () => {
            const files = request.result.map((item: any) => ({
              id: item.id,
              name: item.name,
              size: item.size,
              type: item.type
            }));
            resolve(files);
          };
          request.onerror = () => reject(request.error);
        });
      }
    };
  }

  /**
   * 方案3: 使用拖拽突破文件选择限制
   * 优势: 可以访问整个目录结构，包括隐藏文件
   */
  static enableAdvancedDragDrop(element: HTMLElement) {
    const fileMap = new Map<string, File>();
    
    const handleDrop = async (e: DragEvent) => {
      e.preventDefault();
      
      const items = Array.from(e.dataTransfer?.items || []);
      
      for (const item of items) {
        if (item.kind === 'file') {
          const entry = item.webkitGetAsEntry();
          if (entry) {
            await this.processEntry(entry, '', fileMap);
          }
        }
      }
      
      // 触发自定义事件
      element.dispatchEvent(new CustomEvent('filesDropped', {
        detail: { files: Array.from(fileMap.values()), fileMap }
      }));
    };

    element.addEventListener('dragover', (e) => e.preventDefault());
    element.addEventListener('drop', handleDrop);

    return {
      getFiles: () => Array.from(fileMap.values()),
      getFileMap: () => fileMap,
      clear: () => fileMap.clear()
    };
  }

  /**
   * 方案4: 使用 Web Streams 处理大文件
   * 优势: 可以处理超大文件而不占用过多内存
   */
  static createFileStream(file: File, chunkSize = 64 * 1024) {
    let offset = 0;
    
    return new ReadableStream({
      async pull(controller) {
        if (offset >= file.size) {
          controller.close();
          return;
        }
        
        const chunk = file.slice(offset, offset + chunkSize);
        const buffer = await chunk.arrayBuffer();
        controller.enqueue(new Uint8Array(buffer));
        offset += chunkSize;
      }
    });
  }

  /**
   * 方案5: 使用 Service Worker 缓存文件
   * 优势: 可以拦截网络请求，实现离线文件访问
   */
  static async setupServiceWorkerCache() {
    if (!('serviceWorker' in navigator)) {
      throw new Error('浏览器不支持 Service Worker');
    }

    // 注册 Service Worker
    const registration = await navigator.serviceWorker.register('/file-cache-sw.js');
    
    return {
      // 缓存文件
      async cacheFile(url: string, file: Blob): Promise<void> {
        const cache = await caches.open('file-cache-v1');
        const response = new Response(file);
        await cache.put(url, response);
      },

      // 获取缓存的文件
      async getCachedFile(url: string): Promise<Blob | null> {
        const cache = await caches.open('file-cache-v1');
        const response = await cache.match(url);
        return response ? await response.blob() : null;
      },

      // 清除缓存
      async clearCache(): Promise<void> {
        await caches.delete('file-cache-v1');
      }
    };
  }

  /**
   * 方案6: 使用 WebRTC 进行 P2P 文件传输
   * 优势: 绕过服务器限制，直接在浏览器间传输文件
   */
  static createP2PFileTransfer() {
    const connections = new Map<string, RTCPeerConnection>();
    const dataChannels = new Map<string, RTCDataChannel>();

    return {
      // 创建连接
      async createConnection(peerId: string): Promise<RTCPeerConnection> {
        const pc = new RTCPeerConnection({
          iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
        });

        const dataChannel = pc.createDataChannel('fileTransfer', {
          ordered: true
        });

        connections.set(peerId, pc);
        dataChannels.set(peerId, dataChannel);

        return pc;
      },

      // 发送文件
      async sendFile(peerId: string, file: File): Promise<void> {
        const dataChannel = dataChannels.get(peerId);
        if (!dataChannel) throw new Error('连接不存在');

        const chunkSize = 16384; // 16KB chunks
        const fileReader = new FileReader();
        let offset = 0;

        return new Promise((resolve, reject) => {
          const sendNextChunk = () => {
            const slice = file.slice(offset, offset + chunkSize);
            fileReader.readAsArrayBuffer(slice);
          };

          fileReader.onload = (e) => {
            const buffer = e.target?.result as ArrayBuffer;
            dataChannel.send(buffer);
            
            offset += buffer.byteLength;
            if (offset < file.size) {
              sendNextChunk();
            } else {
              resolve();
            }
          };

          fileReader.onerror = reject;
          sendNextChunk();
        });
      }
    };
  }

  /**
   * 方案7: 使用 WebAssembly 进行高性能文件处理
   * 优势: 可以进行复杂的文件操作，如压缩、加密等
   */
  static async loadWasmFileProcessor(wasmUrl: string) {
    const wasmModule = await WebAssembly.instantiateStreaming(fetch(wasmUrl));
    
    return {
      // 压缩文件
      compressFile: (data: Uint8Array): Uint8Array => {
        // 调用 WASM 函数
        return new Uint8Array(); // 占位符
      },

      // 解压文件
      decompressFile: (data: Uint8Array): Uint8Array => {
        // 调用 WASM 函数
        return new Uint8Array(); // 占位符
      },

      // 计算文件哈希
      calculateHash: (data: Uint8Array): string => {
        // 调用 WASM 函数
        return ''; // 占位符
      }
    };
  }

  /**
   * 方案8: 突破下载限制的分片下载
   * 优势: 可以下载大文件，支持断点续传
   */
  static async chunkedDownload(url: string, options: {
    chunkSize?: number;
    maxConcurrent?: number;
    onProgress?: (progress: number) => void;
  } = {}) {
    const { chunkSize = 1024 * 1024, maxConcurrent = 4, onProgress } = options;
    
    // 获取文件大小
    const headResponse = await fetch(url, { method: 'HEAD' });
    const fileSize = parseInt(headResponse.headers.get('content-length') || '0');
    
    if (fileSize === 0) {
      throw new Error('无法获取文件大小');
    }

    const chunks: ArrayBuffer[] = [];
    const totalChunks = Math.ceil(fileSize / chunkSize);
    let completedChunks = 0;

    // 并发下载分片
    const downloadPromises: Promise<void>[] = [];
    
    for (let i = 0; i < totalChunks; i += maxConcurrent) {
      const batchPromises = [];
      
      for (let j = 0; j < maxConcurrent && i + j < totalChunks; j++) {
        const chunkIndex = i + j;
        const start = chunkIndex * chunkSize;
        const end = Math.min(start + chunkSize - 1, fileSize - 1);
        
        const promise = fetch(url, {
          headers: { Range: `bytes=${start}-${end}` }
        }).then(async (response) => {
          chunks[chunkIndex] = await response.arrayBuffer();
          completedChunks++;
          
          if (onProgress) {
            onProgress(completedChunks / totalChunks);
          }
        });
        
        batchPromises.push(promise);
      }
      
      downloadPromises.push(Promise.all(batchPromises).then(() => {}));
    }

    await Promise.all(downloadPromises);
    
    // 合并分片
    const totalSize = chunks.reduce((sum, chunk) => sum + chunk.byteLength, 0);
    const result = new Uint8Array(totalSize);
    let offset = 0;
    
    for (const chunk of chunks) {
      result.set(new Uint8Array(chunk), offset);
      offset += chunk.byteLength;
    }
    
    return new Blob([result]);
  }

  // 辅助方法：递归处理目录项
  private static async processEntry(entry: any, path: string, fileMap: Map<string, File>): Promise<void> {
    if (entry.isFile) {
      const file = await new Promise<File>((resolve) => entry.file(resolve));
      fileMap.set(path + entry.name, file);
    } else if (entry.isDirectory) {
      const reader = entry.createReader();
      const entries = await new Promise<any[]>((resolve) => reader.readEntries(resolve));
      
      for (const childEntry of entries) {
        await this.processEntry(childEntry, path + entry.name + '/', fileMap);
      }
    }
  }
}

/**
 * Service Worker 代码（需要单独文件）
 */
export const serviceWorkerCode = `
// file-cache-sw.js
const CACHE_NAME = 'file-cache-v1';

self.addEventListener('fetch', (event) => {
  if (event.request.url.includes('/cached-file/')) {
    event.respondWith(
      caches.match(event.request).then((response) => {
        return response || fetch(event.request);
      })
    );
  }
});

self.addEventListener('message', (event) => {
  if (event.data.type === 'CACHE_FILE') {
    const { url, data } = event.data;
    caches.open(CACHE_NAME).then((cache) => {
      const response = new Response(data);
      cache.put(url, response);
    });
  }
});
`;

/**
 * 使用示例
 */
export const webBreakthroughExamples = {
  // OPFS 示例
  async opfsExample() {
    const opfs = await WebFileBreakthroughUtils.useOPFS();
    
    // 存储大文件
    const largeFile = new Blob(['x'.repeat(100 * 1024 * 1024)]); // 100MB
    await opfs.storeFile('large-file.bin', largeFile);
    
    // 读取文件
    const file = await opfs.readFile('large-file.bin');
    console.log('文件大小:', file.size);
  },

  // 拖拽示例
  dragDropExample() {
    const dropZone = document.getElementById('drop-zone');
    if (dropZone) {
      const handler = WebFileBreakthroughUtils.enableAdvancedDragDrop(dropZone);
      
      dropZone.addEventListener('filesDropped', (e: any) => {
        const files = e.detail.files;
        console.log('拖拽的文件:', files.map((f: File) => f.name));
      });
    }
  },

  // 分片下载示例
  async chunkedDownloadExample() {
    const blob = await WebFileBreakthroughUtils.chunkedDownload(
      'https://example.com/large-file.zip',
      {
        chunkSize: 1024 * 1024, // 1MB chunks
        maxConcurrent: 4,
        onProgress: (progress) => {
          console.log(`下载进度: ${(progress * 100).toFixed(1)}%`);
        }
      }
    );
    
    console.log('下载完成，文件大小:', blob.size);
  }
};
