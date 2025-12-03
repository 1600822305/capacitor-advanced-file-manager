/**
 * 系统文件管理器集成示例
 * 展示如何在移动端调用本地文件管理器
 */

import { AdvancedFileManager } from 'capacitor-advanced-file-manager';

export class SystemFileManagerExample {

  /**
   * 使用系统文件选择器选择文件
   */
  async selectFilesWithSystemPicker() {
    console.log('=== 系统文件选择器示例 ===');

    try {
      // 选择单个文件
      const singleFileResult = await AdvancedFileManager.openSystemFilePicker({
        type: 'file',
        multiple: false,
        accept: ['jpg', 'png', 'pdf', 'txt'],
        title: '选择一个文件'
      });

      if (!singleFileResult.cancelled) {
        console.log('✅ 选择的文件:', singleFileResult.files);
      } else {
        console.log('❌ 用户取消了文件选择');
      }

      // 选择多个文件
      const multipleFilesResult = await AdvancedFileManager.openSystemFilePicker({
        type: 'file',
        multiple: true,
        accept: ['jpg', 'jpeg', 'png', 'gif'],
        title: '选择多个图片文件'
      });

      if (!multipleFilesResult.cancelled) {
        console.log('✅ 选择的多个文件:', multipleFilesResult.files);
        console.log(`共选择了 ${multipleFilesResult.files.length} 个文件`);
      }

    } catch (error) {
      console.error('❌ 文件选择失败:', error);
    }
  }

  /**
   * 使用系统文件选择器选择目录
   */
  async selectDirectoryWithSystemPicker() {
    console.log('\n=== 系统目录选择器示例 ===');

    try {
      const directoryResult = await AdvancedFileManager.openSystemFilePicker({
        type: 'directory',
        multiple: false,
        title: '选择一个文件夹'
      });

      if (!directoryResult.cancelled) {
        console.log('✅ 选择的目录:', directoryResult.directories);
        
        // 列出选择目录中的文件
        if (directoryResult.directories.length > 0) {
          const directoryPath = directoryResult.directories[0];
          const files = await AdvancedFileManager.listDirectory({
            path: directoryPath,
            showHidden: false
          });
          
          console.log(`📁 目录 "${directoryPath}" 中的文件:`);
          files.files.forEach(file => {
            console.log(`  ${file.type === 'directory' ? '📁' : '📄'} ${file.name}`);
          });
        }
      } else {
        console.log('❌ 用户取消了目录选择');
      }

    } catch (error) {
      console.error('❌ 目录选择失败:', error);
    }
  }

  /**
   * 打开系统文件管理器
   */
  async openSystemFileManager() {
    console.log('\n=== 打开系统文件管理器示例 ===');

    try {
      // 打开默认文件管理器
      await AdvancedFileManager.openSystemFileManager();
      console.log('✅ 系统文件管理器已打开');

      // 打开指定路径的文件管理器
      const specificPath = '/storage/emulated/0/Download';
      await AdvancedFileManager.openSystemFileManager(specificPath);
      console.log(`✅ 已打开路径: ${specificPath}`);

    } catch (error) {
      console.error('❌ 打开文件管理器失败:', error);
    }
  }

  /**
   * 使用系统应用打开文件
   */
  async openFileWithSystemApp() {
    console.log('\n=== 使用系统应用打开文件示例 ===');

    try {
      // 首先创建一个测试文件
      const testFilePath = '/storage/emulated/0/test-document.txt';
      await AdvancedFileManager.createFile({
        path: testFilePath,
        content: '这是一个测试文档\n用于演示系统应用打开功能。',
        encoding: 'utf8'
      });

      console.log('📝 测试文件已创建:', testFilePath);

      // 使用系统应用打开文件（自动检测MIME类型）
      await AdvancedFileManager.openFileWithSystemApp(testFilePath);
      console.log('✅ 文件已用系统应用打开');

      // 指定MIME类型打开文件
      await AdvancedFileManager.openFileWithSystemApp(testFilePath, 'text/plain');
      console.log('✅ 文件已用指定的文本编辑器打开');

    } catch (error) {
      console.error('❌ 打开文件失败:', error);
    }
  }

  /**
   * 文件管理器工作流示例
   */
  async fileManagerWorkflow() {
    console.log('\n=== 完整文件管理工作流示例 ===');

    try {
      // 1. 让用户选择一个目录
      console.log('步骤 1: 选择工作目录');
      const dirResult = await AdvancedFileManager.openSystemFilePicker({
        type: 'directory',
        title: '选择工作目录'
      });

      if (dirResult.cancelled) {
        console.log('❌ 用户取消了操作');
        return;
      }

      const workingDir = dirResult.directories[0];
      console.log('✅ 工作目录:', workingDir);

      // 2. 列出目录中的文件
      console.log('\n步骤 2: 列出目录内容');
      const files = await AdvancedFileManager.listDirectory({
        path: workingDir,
        showHidden: false,
        sortBy: 'name'
      });

      console.log(`📁 目录中有 ${files.totalCount} 个项目:`);
      files.files.slice(0, 10).forEach(file => {
        const icon = file.type === 'directory' ? '📁' : '📄';
        const size = file.type === 'file' ? ` (${this.formatFileSize(file.size)})` : '';
        console.log(`  ${icon} ${file.name}${size}`);
      });

      // 3. 让用户选择要操作的文件
      console.log('\n步骤 3: 选择要操作的文件');
      const fileResult = await AdvancedFileManager.openSystemFilePicker({
        type: 'file',
        multiple: false,
        title: '选择要操作的文件'
      });

      if (!fileResult.cancelled && fileResult.files.length > 0) {
        const selectedFile = fileResult.files[0];
        console.log('✅ 选择的文件:', selectedFile);

        // 4. 获取文件信息
        console.log('\n步骤 4: 获取文件信息');
        const fileInfo = await AdvancedFileManager.getFileInfo({
          path: selectedFile
        });

        console.log('📋 文件信息:');
        console.log(`  名称: ${fileInfo.name}`);
        console.log(`  大小: ${this.formatFileSize(fileInfo.size)}`);
        console.log(`  类型: ${fileInfo.type}`);
        console.log(`  修改时间: ${new Date(fileInfo.mtime).toLocaleString()}`);

        // 5. 提供操作选项
        console.log('\n步骤 5: 文件操作选项');
        console.log('可用操作:');
        console.log('  1. 用系统应用打开');
        console.log('  2. 复制到其他位置');
        console.log('  3. 查看文件内容（如果是文本文件）');

        // 示例：用系统应用打开
        await AdvancedFileManager.openFileWithSystemApp(selectedFile);
        console.log('✅ 文件已用系统应用打开');
      }

    } catch (error) {
      console.error('❌ 工作流执行失败:', error);
    }
  }

  /**
   * 平台特定功能演示
   */
  async platformSpecificFeatures() {
    console.log('\n=== 平台特定功能演示 ===');

    // 检测平台
    const platform = this.detectPlatform();
    console.log(`当前平台: ${platform}`);

    switch (platform) {
      case 'android':
        await this.androidSpecificFeatures();
        break;
      case 'ios':
        await this.iosSpecificFeatures();
        break;
      case 'web':
        await this.webSpecificFeatures();
        break;
    }
  }

  private async androidSpecificFeatures() {
    console.log('\n🤖 Android 特定功能:');
    
    try {
      // Android 可以访问外部存储
      const externalDirs = [
        '/storage/emulated/0/Download',
        '/storage/emulated/0/Pictures',
        '/storage/emulated/0/Documents',
        '/storage/emulated/0/DCIM'
      ];

      for (const dir of externalDirs) {
        try {
          const exists = await AdvancedFileManager.exists({ path: dir });
          if (exists.exists) {
            console.log(`✅ 可访问: ${dir}`);
          }
        } catch (error) {
          console.log(`❌ 无法访问: ${dir}`);
        }
      }

      // 打开 Android 文件管理器
      await AdvancedFileManager.openSystemFileManager('/storage/emulated/0');
      console.log('✅ Android 文件管理器已打开');

    } catch (error) {
      console.error('❌ Android 功能演示失败:', error);
    }
  }

  private async iosSpecificFeatures() {
    console.log('\n🍎 iOS 特定功能:');
    
    try {
      // iOS 主要在应用沙盒内工作
      const documentsDir = await this.getDocumentsDirectory();
      console.log(`📁 文档目录: ${documentsDir}`);

      // 打开 iOS Files 应用
      await AdvancedFileManager.openSystemFileManager();
      console.log('✅ iOS Files 应用已打开');

    } catch (error) {
      console.error('❌ iOS 功能演示失败:', error);
    }
  }

  private async webSpecificFeatures() {
    console.log('\n🌐 Web 特定功能:');
    
    try {
      // Web 平台使用 File System Access API
      console.log('Web 平台文件访问受浏览器安全策略限制');
      console.log('支持的功能:');
      console.log('  ✅ 文件选择器');
      console.log('  ✅ 目录选择器（现代浏览器）');
      console.log('  ❌ 直接文件系统访问');
      console.log('  ❌ 系统文件管理器');

      // 尝试打开文件选择器
      const result = await AdvancedFileManager.openSystemFilePicker({
        type: 'file',
        multiple: true,
        title: 'Web 文件选择器演示'
      });

      if (!result.cancelled) {
        console.log('✅ Web 文件选择器工作正常');
      }

    } catch (error) {
      console.error('❌ Web 功能演示失败:', error);
    }
  }

  // 辅助方法
  private formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  private detectPlatform(): string {
    if (typeof window !== 'undefined') {
      return 'web';
    }
    // 在实际应用中，可以通过 Capacitor.getPlatform() 获取
    return 'unknown';
  }

  private async getDocumentsDirectory(): Promise<string> {
    // 在实际应用中，可以通过 Capacitor Filesystem 插件获取
    return '/Documents';
  }

  /**
   * 运行所有示例
   */
  async runAllExamples() {
    console.log('🚀 开始运行系统文件管理器集成示例\n');

    try {
      await this.selectFilesWithSystemPicker();
      await this.selectDirectoryWithSystemPicker();
      await this.openSystemFileManager();
      await this.openFileWithSystemApp();
      await this.fileManagerWorkflow();
      await this.platformSpecificFeatures();

      console.log('\n✅ 所有示例运行完成！');
    } catch (error) {
      console.error('❌ 示例运行失败:', error);
    }
  }
}

// 使用示例
export async function runSystemFileManagerDemo() {
  const example = new SystemFileManagerExample();
  await example.runAllExamples();
}

// 如果直接运行此文件
if (typeof window !== 'undefined') {
  (window as any).runSystemFileManagerDemo = runSystemFileManagerDemo;
  console.log('💡 在浏览器控制台中运行: runSystemFileManagerDemo()');
}
