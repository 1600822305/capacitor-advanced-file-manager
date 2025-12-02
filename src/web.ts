import { WebPlugin } from '@capacitor/core';

import type {
  AdvancedFileManagerPlugin,
  FileInfo,
  ListDirectoryOptions,
  ListDirectoryResult,
  FileOperationOptions,
  CreateFileOptions,
  ReadFileOptions,
  ReadFileResult,
  WriteFileOptions,
  MoveFileOptions,
  CopyFileOptions,
  RenameFileOptions,
  CreateDirectoryOptions,
  SearchFilesOptions,
  SearchFilesResult,
  PermissionResult,
  SystemFilePickerOptions,
  SystemFilePickerResult,
  SelectedFileInfo,
  // AI 编辑相关
  ReadFileRangeOptions,
  ReadFileRangeResult,
  InsertContentOptions,
  ReplaceInFileOptions,
  ReplaceInFileResult,
  ApplyDiffOptions,
  ApplyDiffResult,
  GetFileHashOptions,
  GetFileHashResult,
  GetLineCountResult
} from './definitions';

export class AdvancedFileManagerWeb extends WebPlugin implements AdvancedFileManagerPlugin {

  // 检查是否支持 File System Access API
  private isFileSystemAccessSupported(): boolean {
    return 'showDirectoryPicker' in window;
  }

  async requestPermissions(): Promise<PermissionResult> {
    if (this.isFileSystemAccessSupported()) {
      return { granted: true, message: 'File System Access API is supported' };
    } else {
      return {
        granted: false,
        message: 'File System Access API is not supported in this browser'
      };
    }
  }

  async checkPermissions(): Promise<PermissionResult> {
    return this.requestPermissions();
  }

  async listDirectory(options: ListDirectoryOptions): Promise<ListDirectoryResult> {
    if (!this.isFileSystemAccessSupported()) {
      throw new Error('File System Access API is not supported in this browser');
    }

    try {
      // 在Web平台，我们需要用户选择目录
      const dirHandle = await (window as any).showDirectoryPicker();
      const files: FileInfo[] = [];

      for await (const [name, handle] of dirHandle.entries()) {
        const file = await this.getFileInfoFromHandle(handle, name, options.path);
        if (options.showHidden || !file.isHidden) {
          files.push(file);
        }
      }

      // 排序
      if (options.sortBy) {
        files.sort((a, b) => {
          let comparison = 0;
          switch (options.sortBy) {
            case 'name':
              comparison = a.name.localeCompare(b.name);
              break;
            case 'size':
              comparison = a.size - b.size;
              break;
            case 'mtime':
              comparison = a.mtime - b.mtime;
              break;
            case 'type':
              comparison = a.type.localeCompare(b.type);
              break;
          }
          return options.sortOrder === 'desc' ? -comparison : comparison;
        });
      }

      return {
        files,
        totalCount: files.length
      };
    } catch (error) {
      throw new Error(`Failed to list directory: ${error}`);
    }
  }

  private async getFileInfoFromHandle(handle: any, name: string, basePath: string): Promise<FileInfo> {
    const isDirectory = handle.kind === 'directory';
    let size = 0;
    let mtime = 0;
    let ctime = 0;

    if (!isDirectory) {
      try {
        const file = await handle.getFile();
        size = file.size;
        mtime = file.lastModified;
        ctime = file.lastModified; // Web API doesn't provide creation time
      } catch (error) {
        console.warn('Failed to get file info:', error);
      }
    }

    return {
      name,
      path: `${basePath}/${name}`,
      size,
      type: isDirectory ? 'directory' : 'file',
      mtime,
      ctime,
      isHidden: name.startsWith('.')
    };
  }

  async createDirectory(_options: CreateDirectoryOptions): Promise<void> {
    throw new Error('Creating directories is not supported in web browsers for security reasons');
  }

  async deleteDirectory(_options: FileOperationOptions): Promise<void> {
    throw new Error('Deleting directories is not supported in web browsers for security reasons');
  }

  async createFile(options: CreateFileOptions): Promise<void> {
    if (!this.isFileSystemAccessSupported()) {
      throw new Error('File System Access API is not supported in this browser');
    }

    try {
      const fileHandle = await (window as any).showSaveFilePicker({
        suggestedName: options.path.split('/').pop() || 'new-file.txt'
      });

      const writable = await fileHandle.createWritable();
      const content = options.content || '';

      if (options.encoding === 'base64') {
        const binaryString = atob(content);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
          bytes[i] = binaryString.charCodeAt(i);
        }
        await writable.write(bytes);
      } else {
        await writable.write(content);
      }

      await writable.close();
    } catch (error) {
      throw new Error(`Failed to create file: ${error}`);
    }
  }

  async readFile(options: ReadFileOptions): Promise<ReadFileResult> {
    if (!this.isFileSystemAccessSupported()) {
      throw new Error('File System Access API is not supported in this browser');
    }

    try {
      const [fileHandle] = await (window as any).showOpenFilePicker();
      const file = await fileHandle.getFile();

      let content: string;
      const encoding = options.encoding || 'utf8';

      if (encoding === 'base64') {
        const arrayBuffer = await file.arrayBuffer();
        const bytes = new Uint8Array(arrayBuffer);
        content = btoa(String.fromCharCode(...bytes));
      } else {
        content = await file.text();
      }

      return {
        content,
        encoding
      };
    } catch (error) {
      throw new Error(`Failed to read file: ${error}`);
    }
  }

  async writeFile(options: WriteFileOptions): Promise<void> {
    // 在Web平台，写入文件通常需要用户交互
    return this.createFile({
      path: options.path,
      content: options.content,
      encoding: options.encoding
    });
  }

  async deleteFile(_options: FileOperationOptions): Promise<void> {
    throw new Error('Deleting files is not supported in web browsers for security reasons');
  }

  async moveFile(_options: MoveFileOptions): Promise<void> {
    throw new Error('Moving files is not supported in web browsers for security reasons');
  }

  async copyFile(_options: CopyFileOptions): Promise<void> {
    throw new Error('Copying files is not supported in web browsers for security reasons');
  }

  async renameFile(_options: RenameFileOptions): Promise<void> {
    throw new Error('Renaming files is not supported in web browsers for security reasons');
  }

  async getFileInfo(_options: FileOperationOptions): Promise<FileInfo> {
    throw new Error('Getting file info for specific paths is not supported in web browsers');
  }

  async exists(_options: FileOperationOptions): Promise<{ exists: boolean }> {
    throw new Error('Checking file existence is not supported in web browsers for security reasons');
  }

  async searchFiles(_options: SearchFilesOptions): Promise<SearchFilesResult> {
    throw new Error('File search is not supported in web browsers for security reasons');
  }

  async openSystemFilePicker(options: SystemFilePickerOptions): Promise<SystemFilePickerResult> {
    if (!this.isFileSystemAccessSupported()) {
      throw new Error('File System Access API is not supported in this browser');
    }

    try {
      const files: SelectedFileInfo[] = [];
      const directories: SelectedFileInfo[] = [];

      if (options.type === 'directory' || options.type === 'both') {
        // 选择目录
        const dirHandle = await (window as any).showDirectoryPicker();
        const dirInfo: SelectedFileInfo = {
          name: dirHandle.name,
          path: `/${dirHandle.name}`,
          uri: dirHandle.name,
          size: 0,
          type: 'directory',
          mimeType: 'inode/directory',
          mtime: Date.now(),
          ctime: Date.now()
        };
        directories.push(dirInfo);
      }

      if (options.type === 'file' || options.type === 'both') {
        // 选择文件
        const fileHandles = await (window as any).showOpenFilePicker({
          multiple: options.multiple || false,
          types: options.accept ? [{
            description: 'Selected files',
            accept: options.accept.reduce((acc: any, ext: string) => {
              const mimeType = this.getMimeTypeFromExtension(ext);
              if (!acc[mimeType]) acc[mimeType] = [];
              acc[mimeType].push(`.${ext}`);
              return acc;
            }, {})
          }] : undefined
        });

        for (const handle of fileHandles) {
          try {
            const file = await handle.getFile();
            const fileInfo: SelectedFileInfo = {
              name: file.name,
              path: `/${file.name}`,
              uri: file.name,
              size: file.size,
              type: 'file',
              mimeType: file.type || 'application/octet-stream',
              mtime: file.lastModified,
              ctime: file.lastModified
            };
            files.push(fileInfo);
          } catch (error) {
            console.warn('Failed to get file info:', error);
          }
        }
      }

      return {
        files,
        directories,
        cancelled: false
      };
    } catch (error: unknown) {
      if (error instanceof Error && error.name === 'AbortError') {
        return {
          files: [],
          directories: [],
          cancelled: true
        };
      }
      throw new Error(`File picker failed: ${error}`);
    }
  }

  async openSystemFileManager(_path?: string): Promise<void> {
    // Web浏览器无法直接打开系统文件管理器
    // 但可以提供一些替代方案
    if (this.isFileSystemAccessSupported()) {
      try {
        await (window as any).showDirectoryPicker();
      } catch (error: unknown) {
        if (!(error instanceof Error) || error.name !== 'AbortError') {
          throw new Error('Cannot open system file manager in web browsers');
        }
      }
    } else {
      throw new Error('File system access not supported in this browser');
    }
  }

  async openFileWithSystemApp(_filePath: string, _mimeType?: string): Promise<void> {
    throw new Error('Opening files with system apps is not supported in web browsers');
  }

  // ============ AI 编辑操作 ============

  async readFileRange(_options: ReadFileRangeOptions): Promise<ReadFileRangeResult> {
    throw new Error('Reading file ranges is not supported in web browsers for security reasons');
  }

  async insertContent(_options: InsertContentOptions): Promise<void> {
    throw new Error('Inserting content is not supported in web browsers for security reasons');
  }

  async replaceInFile(_options: ReplaceInFileOptions): Promise<ReplaceInFileResult> {
    throw new Error('Replacing in file is not supported in web browsers for security reasons');
  }

  async applyDiff(_options: ApplyDiffOptions): Promise<ApplyDiffResult> {
    throw new Error('Applying diff is not supported in web browsers for security reasons');
  }

  async getFileHash(_options: GetFileHashOptions): Promise<GetFileHashResult> {
    throw new Error('Getting file hash is not supported in web browsers for security reasons');
  }

  async getLineCount(_options: FileOperationOptions): Promise<GetLineCountResult> {
    throw new Error('Getting line count is not supported in web browsers for security reasons');
  }

  private getMimeTypeFromExtension(extension: string): string {
    const mimeTypes: { [key: string]: string } = {
      'txt': 'text/plain',
      'pdf': 'application/pdf',
      'jpg': 'image/jpeg',
      'jpeg': 'image/jpeg',
      'png': 'image/png',
      'gif': 'image/gif',
      'mp4': 'video/mp4',
      'mp3': 'audio/mpeg',
      'zip': 'application/zip',
      'json': 'application/json',
      'xml': 'application/xml',
      'html': 'text/html',
      'css': 'text/css',
      'js': 'application/javascript'
    };

    return mimeTypes[extension.toLowerCase()] || 'application/octet-stream';
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
