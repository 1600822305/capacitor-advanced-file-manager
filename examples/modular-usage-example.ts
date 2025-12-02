/**
 * 模块化使用示例
 * 展示如何使用 capacitor-advanced-file-manager 的各个模块
 */

// 核心功能导入
import { AdvancedFileManager } from 'capacitor-advanced-file-manager';

// 模块化功能导入
import { FileSearch } from 'capacitor-advanced-file-manager/search';
import { BatchOperations } from 'capacitor-advanced-file-manager/batch';
// import { FileUtils } from 'capacitor-advanced-file-manager/utils';  // 开发中

export class FileManagerExample {
  
  /**
   * 基础文件操作示例
   */
  async basicOperations() {
    console.log('=== 基础文件操作 ===');
    
    // 检查权限
    const permissions = await AdvancedFileManager.checkPermissions();
    console.log('权限状态:', permissions);
    
    if (!permissions.granted) {
      await AdvancedFileManager.requestPermissions();
    }
    
    // 列出目录内容
    const files = await AdvancedFileManager.listDirectory({
      path: '/storage/emulated/0',
      showHidden: false,
      sortBy: 'name',
      sortOrder: 'asc'
    });
    
    console.log(`找到 ${files.totalCount} 个文件/文件夹`);
    files.files.slice(0, 5).forEach(file => {
      console.log(`${file.type === 'directory' ? '📁' : '📄'} ${file.name} (${file.size} bytes)`);
    });
    
    // 创建测试文件
    const testFile = '/storage/emulated/0/test-example.txt';
    await AdvancedFileManager.createFile({
      path: testFile,
      content: 'Hello from Advanced File Manager!\nThis is a test file.',
      encoding: 'utf8'
    });
    console.log('✅ 测试文件创建成功');
    
    // 读取文件
    const content = await AdvancedFileManager.readFile({
      path: testFile,
      encoding: 'utf8'
    });
    console.log('📖 文件内容:', content.content);
    
    // 获取文件信息
    const fileInfo = await AdvancedFileManager.getFileInfo({ path: testFile });
    console.log('ℹ️ 文件信息:', {
      name: fileInfo.name,
      size: fileInfo.size,
      type: fileInfo.type,
      mtime: new Date(fileInfo.mtime).toLocaleString()
    });
  }
  
  /**
   * 文件搜索示例
   */
  async searchExamples() {
    console.log('\n=== 文件搜索示例 ===');
    
    // 基本搜索
    const basicSearch = await FileSearch.search({
      directory: '/storage/emulated/0',
      query: '*.txt',
      searchType: 'name',
      recursive: false,
      maxResults: 10
    });
    
    console.log(`📍 基本搜索找到 ${basicSearch.totalFound} 个 .txt 文件`);
    
    // 高级搜索
    const advancedSearch = await FileSearch.search({
      directory: '/storage/emulated/0',
      query: 'photo',
      searchType: 'name',
      recursive: true,
      caseSensitive: false,
      fileTypes: ['jpg', 'jpeg', 'png', 'gif'],
      sizeFilter: {
        min: 1024,      // 大于 1KB
        max: 10485760   // 小于 10MB
      },
      maxResults: 20
    });
    
    console.log(`🔍 高级搜索找到 ${advancedSearch.totalFound} 个照片文件`);
    console.log(`⏱️ 搜索耗时: ${advancedSearch.searchTime}ms`);
    
    // 快速搜索
    const quickResults = await FileSearch.quickSearch(
      '/storage/emulated/0/Download',
      'download',
      5
    );
    console.log(`⚡ 快速搜索找到 ${quickResults.length} 个匹配文件`);
    
    // 搜索最近文件
    const recentFiles = await FileSearch.searchRecentFiles(
      '/storage/emulated/0',
      7, // 最近7天
      10
    );
    console.log(`📅 最近7天修改的文件: ${recentFiles.length} 个`);
    
    // 异步搜索示例
    console.log('🔄 开始异步搜索...');
    const searchHandle = await FileSearch.searchAsync({
      directory: '/storage/emulated/0',
      query: '*',
      recursive: true,
      maxResults: 100
    }, (progress) => {
      if (progress.filesSearched % 50 === 0) {
        console.log(`   搜索进度: ${progress.filesSearched} 文件已扫描, 找到 ${progress.matchesFound} 个匹配`);
      }
    });
    
    // 等待搜索完成
    setTimeout(async () => {
      try {
        const asyncResults = await FileSearch.getSearchResult(searchHandle.id);
        console.log(`✅ 异步搜索完成: 找到 ${asyncResults.totalFound} 个文件`);
      } catch (error) {
        console.log('异步搜索结果获取失败:', error.message);
      }
    }, 5000);
  }
  
  /**
   * 批量操作示例
   */
  async batchOperationsExamples() {
    console.log('\n=== 批量操作示例 ===');
    
    // 创建测试文件
    const testFiles = [
      '/storage/emulated/0/batch-test-1.txt',
      '/storage/emulated/0/batch-test-2.txt',
      '/storage/emulated/0/batch-test-3.txt'
    ];
    
    console.log('📝 创建测试文件...');
    for (let i = 0; i < testFiles.length; i++) {
      await AdvancedFileManager.createFile({
        path: testFiles[i],
        content: `Test file ${i + 1} content\nCreated for batch operations demo.`
      });
    }
    
    // 批量复制
    const copyOperations = testFiles.map((file, index) => ({
      sourcePath: file,
      destinationPath: file.replace('.txt', `-copy-${index + 1}.txt`)
    }));
    
    console.log('📋 执行批量复制...');
    const copyResult = await BatchOperations.batchCopy(copyOperations, {
      overwrite: true,
      continueOnError: true
    });
    
    console.log(`✅ 批量复制完成: ${copyResult.successful}/${copyResult.total} 成功`);
    console.log(`⏱️ 总耗时: ${copyResult.totalTime}ms`);
    
    // 批量删除
    const filesToDelete = [
      ...testFiles,
      ...copyOperations.map(op => op.destinationPath)
    ];
    
    console.log('🗑️ 执行批量删除...');
    const deleteResult = await BatchOperations.batchDelete(filesToDelete, {
      continueOnError: true
    });
    
    console.log(`✅ 批量删除完成: ${deleteResult.successful}/${deleteResult.total} 成功`);
    
    // 异步批量操作示例
    console.log('🔄 异步批量操作示例...');
    
    // 先创建一些文件用于演示
    const asyncTestFiles = Array.from({ length: 5 }, (_, i) => 
      `/storage/emulated/0/async-test-${i + 1}.txt`
    );
    
    for (const file of asyncTestFiles) {
      await AdvancedFileManager.createFile({
        path: file,
        content: `Async test file content`
      });
    }
    
    const batchHandle = await BatchOperations.batchOperateAsync([
      ...asyncTestFiles.map((file, index) => ({
        id: `copy_${index}`,
        type: 'copy' as const,
        sourcePath: file,
        destinationPath: file.replace('.txt', '-async-copy.txt')
      })),
      ...asyncTestFiles.map((file, index) => ({
        id: `delete_${index}`,
        type: 'delete' as const,
        sourcePath: file
      }))
    ], {
      continueOnError: true,
      concurrency: 2
    }, (progress) => {
      console.log(`   批量操作进度: ${progress.percentage.toFixed(1)}% (${progress.completed}/${progress.total})`);
      console.log(`   当前操作: ${progress.currentOperation} - ${progress.currentFile}`);
    });
    
    // 等待批量操作完成
    setTimeout(async () => {
      try {
        const batchResult = await BatchOperations.getBatchResult(batchHandle.id);
        console.log(`✅ 异步批量操作完成: ${batchResult.successful}/${batchResult.total} 成功`);
        
        // 清理剩余文件
        const remainingFiles = asyncTestFiles.map(file => file.replace('.txt', '-async-copy.txt'));
        await BatchOperations.batchDelete(remainingFiles);
        console.log('🧹 清理完成');
      } catch (error) {
        console.log('异步批量操作结果获取失败:', error.message);
      }
    }, 3000);
  }
  
  /**
   * 错误处理示例
   */
  async errorHandlingExamples() {
    console.log('\n=== 错误处理示例 ===');
    
    try {
      // 尝试访问不存在的目录
      await AdvancedFileManager.listDirectory({
        path: '/non/existent/directory'
      });
    } catch (error) {
      console.log('❌ 预期的错误 - 目录不存在:', error.message);
    }
    
    try {
      // 尝试搜索无效路径
      await FileSearch.search({
        directory: '/invalid/path',
        query: 'test'
      });
    } catch (error) {
      console.log('❌ 预期的错误 - 搜索路径无效:', error.message);
    }
    
    try {
      // 尝试批量操作不存在的文件
      await BatchOperations.batchDelete([
        '/non/existent/file1.txt',
        '/non/existent/file2.txt'
      ], {
        continueOnError: true
      });
    } catch (error) {
      console.log('❌ 批量操作错误:', error.message);
    }
  }
  
  /**
   * 运行所有示例
   */
  async runAllExamples() {
    console.log('🚀 开始运行 Advanced File Manager 模块化示例\n');
    
    try {
      await this.basicOperations();
      await this.searchExamples();
      await this.batchOperationsExamples();
      await this.errorHandlingExamples();
      
      console.log('\n✅ 所有示例运行完成！');
    } catch (error) {
      console.error('❌ 示例运行失败:', error);
    }
  }
}

// 使用示例
export async function runFileManagerDemo() {
  const example = new FileManagerExample();
  await example.runAllExamples();
}

// 如果直接运行此文件
if (typeof window !== 'undefined') {
  // 在浏览器环境中
  (window as any).runFileManagerDemo = runFileManagerDemo;
  console.log('💡 在浏览器控制台中运行: runFileManagerDemo()');
}
