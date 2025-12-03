import { WebPlugin } from '@capacitor/core';
import type { 
  BatchOperationsPlugin,
  BatchCopyOperation,
  BatchMoveOperation,
  BatchOperation,
  BatchOperationResult,
  BatchOperationItemResult,
  BatchOptions,
  BatchProgress,
  BatchHandle
} from './definitions';

export class BatchOperationsWeb extends WebPlugin implements BatchOperationsPlugin {
  private batchCounter = 0;
  private activeBatches = new Map<string, { 
    cancelled: boolean; 
    paused: boolean;
    operations: BatchOperation[];
    results: BatchOperationItemResult[];
  }>();

  async batchCopy(
    operations: BatchCopyOperation[],
    options?: BatchOptions
  ): Promise<BatchOperationResult> {
    const batchOps: BatchOperation[] = operations.map((op, index) => ({
      id: `copy_${index}`,
      type: 'copy',
      sourcePath: op.sourcePath,
      destinationPath: op.destinationPath
    }));

    return this.executeBatchOperations(batchOps, options);
  }

  async batchMove(
    operations: BatchMoveOperation[],
    options?: BatchOptions
  ): Promise<BatchOperationResult> {
    const batchOps: BatchOperation[] = operations.map((op, index) => ({
      id: `move_${index}`,
      type: 'move',
      sourcePath: op.sourcePath,
      destinationPath: op.destinationPath
    }));

    return this.executeBatchOperations(batchOps, options);
  }

  async batchDelete(
    paths: string[],
    options?: BatchOptions
  ): Promise<BatchOperationResult> {
    const batchOps: BatchOperation[] = paths.map((path, index) => ({
      id: `delete_${index}`,
      type: 'delete',
      sourcePath: path
    }));

    return this.executeBatchOperations(batchOps, options);
  }

  async batchOperateAsync(
    operations: BatchOperation[],
    options?: BatchOptions,
    progressCallback?: (progress: BatchProgress) => void
  ): Promise<BatchHandle> {
    const batchId = `batch_${++this.batchCounter}`;
    
    this.activeBatches.set(batchId, {
      cancelled: false,
      paused: false,
      operations,
      results: []
    });

    // 异步执行批量操作
    this.performAsyncBatch(batchId, operations, options, progressCallback).catch(console.error);

    return {
      id: batchId,
      cancel: () => this.cancelBatch(batchId),
      pause: () => this.pauseBatch(batchId),
      resume: () => this.resumeBatch(batchId),
      getStatus: () => this.getBatchStatus(batchId)
    };
  }

  async getBatchResult(batchId: string): Promise<BatchOperationResult> {
    const batch = this.activeBatches.get(batchId);
    if (!batch) {
      throw new Error(`Batch operation ${batchId} not found`);
    }

    const results = batch.results;
    const successful = results.filter(r => r.status === 'success').length;
    const failed = results.filter(r => r.status === 'failed').length;
    const skipped = results.filter(r => r.status === 'skipped').length;
    const totalTime = results.reduce((sum, r) => sum + r.duration, 0);

    return {
      total: results.length,
      successful,
      failed,
      skipped,
      results,
      totalTime
    };
  }

  async cancelBatch(batchId: string): Promise<void> {
    const batch = this.activeBatches.get(batchId);
    if (batch) {
      batch.cancelled = true;
    }
  }

  async pauseBatch(batchId: string): Promise<void> {
    const batch = this.activeBatches.get(batchId);
    if (batch) {
      batch.paused = true;
    }
  }

  async resumeBatch(batchId: string): Promise<void> {
    const batch = this.activeBatches.get(batchId);
    if (batch) {
      batch.paused = false;
    }
  }

  async batchRename(
    _directory: string,
    _pattern: string,
    _replacement: string,
    _options?: { recursive?: boolean; fileTypes?: string[] }
  ): Promise<BatchOperationResult> {
    throw new Error('Batch rename not supported in web browsers for security reasons');
  }

  async batchChangePermissions(
    _paths: string[],
    _permissions: string,
    _options?: BatchOptions
  ): Promise<BatchOperationResult> {
    throw new Error('Batch permission changes not supported in web browsers');
  }

  async batchChangeTimestamp(
    _paths: string[],
    _timestamp: number,
    _options?: BatchOptions
  ): Promise<BatchOperationResult> {
    throw new Error('Batch timestamp changes not supported in web browsers');
  }

  private async getBatchStatus(batchId: string): Promise<'running' | 'paused' | 'cancelled' | 'completed'> {
    const batch = this.activeBatches.get(batchId);
    if (!batch) {
      return 'completed';
    }

    if (batch.cancelled) {
      return 'cancelled';
    }

    if (batch.paused) {
      return 'paused';
    }

    return 'running';
  }

  private async executeBatchOperations(
    operations: BatchOperation[],
    options?: BatchOptions
  ): Promise<BatchOperationResult> {
    const startTime = Date.now();
    const results: BatchOperationItemResult[] = [];

    for (const operation of operations) {
      const operationStartTime = Date.now();
      
      try {
        // 检查确认回调
        if (options?.confirmCallback) {
          const confirmed = await options.confirmCallback(operation);
          if (!confirmed) {
            results.push({
              operationId: operation.id,
              status: 'skipped',
              sourcePath: operation.sourcePath,
              destinationPath: operation.destinationPath,
              skipReason: 'User cancelled',
              duration: Date.now() - operationStartTime
            });
            continue;
          }
        }

        await this.executeOperation(operation, options);
        
        results.push({
          operationId: operation.id,
          status: 'success',
          sourcePath: operation.sourcePath,
          destinationPath: operation.destinationPath,
          duration: Date.now() - operationStartTime
        });

      } catch (error) {
        results.push({
          operationId: operation.id,
          status: 'failed',
          sourcePath: operation.sourcePath,
          destinationPath: operation.destinationPath,
          error: error instanceof Error ? error.message : String(error),
          duration: Date.now() - operationStartTime
        });

        if (!options?.continueOnError) {
          break;
        }
      }
    }

    const successful = results.filter(r => r.status === 'success').length;
    const failed = results.filter(r => r.status === 'failed').length;
    const skipped = results.filter(r => r.status === 'skipped').length;

    return {
      total: operations.length,
      successful,
      failed,
      skipped,
      results,
      totalTime: Date.now() - startTime
    };
  }

  private async executeOperation(operation: BatchOperation, options?: BatchOptions): Promise<void> {
    // 在Web环境中，文件操作受到很大限制
    // 这里提供基本的实现框架，实际功能需要用户交互
    
    switch (operation.type) {
      case 'copy':
        if (!operation.destinationPath) {
          throw new Error('Destination path required for copy operation');
        }
        await this.copyFileWeb(operation.sourcePath, operation.destinationPath, options);
        break;
        
      case 'move':
        if (!operation.destinationPath) {
          throw new Error('Destination path required for move operation');
        }
        await this.moveFileWeb(operation.sourcePath, operation.destinationPath, options);
        break;
        
      case 'delete':
        await this.deleteFileWeb(operation.sourcePath, options);
        break;
        
      default:
        throw new Error(`Unsupported operation type: ${operation.type}`);
    }
  }

  private async copyFileWeb(_sourcePath: string, _destinationPath: string, _options?: BatchOptions): Promise<void> {
    throw new Error('File copy operations require user interaction in web browsers');
  }

  private async moveFileWeb(_sourcePath: string, _destinationPath: string, _options?: BatchOptions): Promise<void> {
    throw new Error('File move operations require user interaction in web browsers');
  }

  private async deleteFileWeb(_path: string, _options?: BatchOptions): Promise<void> {
    throw new Error('File delete operations require user interaction in web browsers');
  }

  private async performAsyncBatch(
    batchId: string,
    operations: BatchOperation[],
    options?: BatchOptions,
    progressCallback?: (progress: BatchProgress) => void
  ): Promise<void> {
    const batch = this.activeBatches.get(batchId);
    if (!batch) return;

    let completed = 0;
    let failed = 0;
    let skipped = 0;

    for (let i = 0; i < operations.length; i++) {
      // 检查是否被取消或暂停
      if (batch.cancelled) break;
      
      while (batch.paused && !batch.cancelled) {
        await new Promise(resolve => setTimeout(resolve, 100));
      }

      const operation = operations[i];
      
      if (progressCallback) {
        progressCallback({
          currentIndex: i,
          total: operations.length,
          currentFile: operation.sourcePath,
          currentOperation: operation.type,
          completed,
          failed,
          skipped,
          percentage: (i / operations.length) * 100
        });
      }

      try {
        await this.executeOperation(operation, options);
        completed++;
      } catch (error) {
        failed++;
        if (!options?.continueOnError) break;
      }
    }

    // 最终进度回调
    if (progressCallback) {
      progressCallback({
        currentIndex: operations.length,
        total: operations.length,
        currentFile: '',
        currentOperation: 'copy',
        completed,
        failed,
        skipped,
        percentage: 100
      });
    }

    this.activeBatches.delete(batchId);
  }
}
