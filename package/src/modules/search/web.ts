import { WebPlugin } from '@capacitor/core';
import type { 
  FileSearchPlugin, 
  SearchOptions, 
  SearchResult, 
  SearchFileInfo, 
  SearchProgress,
  SearchHandle 
} from './definitions';

export class FileSearchWeb extends WebPlugin implements FileSearchPlugin {
  private searchCounter = 0;
  private activeSearches = new Map<string, { cancelled: boolean }>();

  async search(options: SearchOptions): Promise<SearchResult> {
    const startTime = Date.now();
    
    if (!this.isFileSystemAccessSupported()) {
      throw new Error('File System Access API is not supported in this browser');
    }

    try {
      // 在Web环境中，我们需要用户选择目录
      const dirHandle = await (window as any).showDirectoryPicker();
      const files: SearchFileInfo[] = [];
      
      await this.searchInDirectory(dirHandle, '', options, files);
      
      const searchTime = Date.now() - startTime;
      const truncated = options.maxResults ? files.length >= options.maxResults : false;
      
      return {
        files: files.slice(0, options.maxResults || files.length),
        totalFound: files.length,
        searchTime,
        truncated
      };
    } catch (error) {
      throw new Error(`Search failed: ${error}`);
    }
  }

  async searchAsync(
    options: SearchOptions,
    progressCallback?: (progress: SearchProgress) => void
  ): Promise<SearchHandle> {
    const searchId = `search_${++this.searchCounter}`;
    this.activeSearches.set(searchId, { cancelled: false });

    // 异步执行搜索
    this.performAsyncSearch(searchId, options, progressCallback).catch(console.error);

    return {
      id: searchId,
      cancel: () => this.cancelSearch(searchId)
    };
  }

  async getSearchResult(_searchId: string): Promise<SearchResult> {
    // Web环境中的简化实现
    throw new Error('Async search results not implemented for web platform');
  }

  async cancelSearch(searchId: string): Promise<void> {
    const search = this.activeSearches.get(searchId);
    if (search) {
      search.cancelled = true;
      this.activeSearches.delete(searchId);
    }
  }

  async quickSearch(directory: string, query: string, maxResults = 50): Promise<SearchFileInfo[]> {
    return this.search({
      directory,
      query,
      searchType: 'name',
      recursive: false,
      maxResults,
      caseSensitive: false
    }).then(result => result.files);
  }

  async searchRecentFiles(directory: string, days = 7, maxResults = 100): Promise<SearchFileInfo[]> {
    const cutoffTime = Date.now() - (days * 24 * 60 * 60 * 1000);
    
    return this.search({
      directory,
      query: '*',
      searchType: 'name',
      recursive: true,
      maxResults,
      dateFilter: {
        from: cutoffTime
      }
    }).then(result => result.files);
  }

  async searchLargeFiles(directory: string, minSize: number, maxResults = 50): Promise<SearchFileInfo[]> {
    return this.search({
      directory,
      query: '*',
      searchType: 'name',
      recursive: true,
      maxResults,
      sizeFilter: {
        min: minSize
      }
    }).then(result => result.files);
  }

  async findDuplicateFiles(directory: string): Promise<{ [key: string]: SearchFileInfo[] }> {
    const allFiles = await this.search({
      directory,
      query: '*',
      searchType: 'name',
      recursive: true
    });

    const duplicates: { [key: string]: SearchFileInfo[] } = {};
    const sizeGroups: { [size: number]: SearchFileInfo[] } = {};

    // 按文件大小分组
    allFiles.files.forEach(file => {
      if (file.type === 'file') {
        if (!sizeGroups[file.size]) {
          sizeGroups[file.size] = [];
        }
        sizeGroups[file.size].push(file);
      }
    });

    // 找出重复文件
    Object.entries(sizeGroups).forEach(([size, files]) => {
      if (files.length > 1) {
        duplicates[`size_${size}`] = files;
      }
    });

    return duplicates;
  }

  private isFileSystemAccessSupported(): boolean {
    return 'showDirectoryPicker' in window;
  }

  private async searchInDirectory(
    dirHandle: any,
    currentPath: string,
    options: SearchOptions,
    results: SearchFileInfo[]
  ): Promise<void> {
    if (options.maxResults && results.length >= options.maxResults) {
      return;
    }

    try {
      for await (const [name, handle] of dirHandle.entries()) {
        const fullPath = currentPath ? `${currentPath}/${name}` : name;
        
        // 检查是否应该包含隐藏文件
        if (!options.includeHidden && name.startsWith('.')) {
          continue;
        }

        if (handle.kind === 'file') {
          const file = await handle.getFile();
          const fileInfo = await this.createSearchFileInfo(handle, name, fullPath, file, options);
          
          if (fileInfo && this.matchesSearchCriteria(fileInfo, options)) {
            results.push(fileInfo);
          }
        } else if (handle.kind === 'directory' && options.recursive) {
          await this.searchInDirectory(handle, fullPath, options, results);
        }
      }
    } catch (error) {
      console.warn(`Failed to search in directory ${currentPath}:`, error);
    }
  }

  private async createSearchFileInfo(
    _handle: any,
    name: string,
    path: string,
    file: File,
    _options: SearchOptions
  ): Promise<SearchFileInfo | null> {
    try {
      return {
        name,
        path,
        size: file.size,
        type: 'file',
        mtime: file.lastModified,
        ctime: file.lastModified,
        isHidden: name.startsWith('.'),
        matchInfo: {
          matchType: 'name',
          matchedText: name
        }
      };
    } catch (error) {
      return null;
    }
  }

  private matchesSearchCriteria(file: SearchFileInfo, options: SearchOptions): boolean {
    // 文件类型过滤
    if (options.fileTypes && options.fileTypes.length > 0) {
      const ext = file.name.split('.').pop()?.toLowerCase();
      if (!ext || !options.fileTypes.includes(ext)) {
        return false;
      }
    }

    // 大小过滤
    if (options.sizeFilter) {
      if (options.sizeFilter.min && file.size < options.sizeFilter.min) {
        return false;
      }
      if (options.sizeFilter.max && file.size > options.sizeFilter.max) {
        return false;
      }
    }

    // 日期过滤
    if (options.dateFilter) {
      if (options.dateFilter.from && file.mtime < options.dateFilter.from) {
        return false;
      }
      if (options.dateFilter.to && file.mtime > options.dateFilter.to) {
        return false;
      }
    }

    // 名称匹配
    if (options.searchType === 'name' || options.searchType === 'both') {
      return this.matchesQuery(file.name, options.query, options);
    }

    return true;
  }

  private matchesQuery(text: string, query: string, options: SearchOptions): boolean {
    if (query === '*') return true;

    const searchText = options.caseSensitive ? text : text.toLowerCase();
    const searchQuery = options.caseSensitive ? query : query.toLowerCase();

    if (options.useRegex) {
      try {
        const regex = new RegExp(searchQuery, options.caseSensitive ? 'g' : 'gi');
        return regex.test(searchText);
      } catch {
        return false;
      }
    }

    return searchText.includes(searchQuery);
  }

  private async performAsyncSearch(
    searchId: string,
    options: SearchOptions,
    progressCallback?: (progress: SearchProgress) => void
  ): Promise<void> {
    // Web环境中的异步搜索实现
    // 这里可以实现更复杂的异步搜索逻辑
    console.log(`Starting async search ${searchId}`, options);
    
    if (progressCallback) {
      progressCallback({
        filesSearched: 0,
        directoriesSearched: 0,
        currentFile: '',
        matchesFound: 0
      });
    }
  }
}
