// 批量操作模块类型定义

export interface BatchOperation {
  /** 操作ID */
  id: string;
  /** 操作类型 */
  type: 'copy' | 'move' | 'delete';
  /** 源路径 */
  sourcePath: string;
  /** 目标路径（删除操作时可选） */
  destinationPath?: string;
}

export interface BatchOperationResult {
  /** 总操作数 */
  total: number;
  /** 成功数 */
  successful: number;
  /** 失败数 */
  failed: number;
  /** 跳过数 */
  skipped: number;
  /** 详细结果 */
  results: BatchOperationItemResult[];
  /** 总耗时（毫秒） */
  totalTime: number;
}

export interface BatchOperationItemResult {
  /** 操作ID */
  operationId: string;
  /** 操作状态 */
  status: 'success' | 'failed' | 'skipped';
  /** 源路径 */
  sourcePath: string;
  /** 目标路径 */
  destinationPath?: string;
  /** 错误信息（失败时） */
  error?: string;
  /** 跳过原因（跳过时） */
  skipReason?: string;
  /** 操作耗时（毫秒） */
  duration: number;
}

export interface BatchProgress {
  /** 当前操作索引 */
  currentIndex: number;
  /** 总操作数 */
  total: number;
  /** 当前操作的文件路径 */
  currentFile: string;
  /** 当前操作类型 */
  currentOperation: 'copy' | 'move' | 'delete';
  /** 已完成数 */
  completed: number;
  /** 失败数 */
  failed: number;
  /** 跳过数 */
  skipped: number;
  /** 进度百分比 */
  percentage: number;
  /** 预估剩余时间（毫秒） */
  estimatedTimeRemaining?: number;
}

export interface BatchOptions {
  /** 是否覆盖已存在的文件 */
  overwrite?: boolean;
  /** 遇到错误时是否继续 */
  continueOnError?: boolean;
  /** 是否跳过隐藏文件 */
  skipHidden?: boolean;
  /** 并发操作数（默认1，顺序执行） */
  concurrency?: number;
  /** 操作前确认回调 */
  confirmCallback?: (operation: BatchOperation) => Promise<boolean>;
}

export interface BatchHandle {
  /** 批量操作ID */
  id: string;
  /** 取消操作 */
  cancel(): Promise<void>;
  /** 暂停操作 */
  pause(): Promise<void>;
  /** 恢复操作 */
  resume(): Promise<void>;
  /** 获取当前状态 */
  getStatus(): Promise<'running' | 'paused' | 'cancelled' | 'completed'>;
}

export interface BatchCopyOperation {
  sourcePath: string;
  destinationPath: string;
}

export interface BatchMoveOperation {
  sourcePath: string;
  destinationPath: string;
}

export interface BatchDeleteOperation {
  path: string;
}

export interface BatchOperationsPlugin {
  /**
   * 批量复制文件
   */
  batchCopy(
    operations: BatchCopyOperation[],
    options?: BatchOptions
  ): Promise<BatchOperationResult>;

  /**
   * 批量移动文件
   */
  batchMove(
    operations: BatchMoveOperation[],
    options?: BatchOptions
  ): Promise<BatchOperationResult>;

  /**
   * 批量删除文件
   */
  batchDelete(
    paths: string[],
    options?: BatchOptions
  ): Promise<BatchOperationResult>;

  /**
   * 异步批量操作（带进度回调）
   */
  batchOperateAsync(
    operations: BatchOperation[],
    options?: BatchOptions,
    progressCallback?: (progress: BatchProgress) => void
  ): Promise<BatchHandle>;

  /**
   * 获取批量操作结果
   */
  getBatchResult(batchId: string): Promise<BatchOperationResult>;

  /**
   * 取消批量操作
   */
  cancelBatch(batchId: string): Promise<void>;

  /**
   * 暂停批量操作
   */
  pauseBatch(batchId: string): Promise<void>;

  /**
   * 恢复批量操作
   */
  resumeBatch(batchId: string): Promise<void>;

  /**
   * 批量重命名文件（按模式）
   */
  batchRename(
    directory: string,
    pattern: string,
    replacement: string,
    options?: { recursive?: boolean; fileTypes?: string[] }
  ): Promise<BatchOperationResult>;

  /**
   * 批量修改文件权限
   */
  batchChangePermissions(
    paths: string[],
    permissions: string,
    options?: BatchOptions
  ): Promise<BatchOperationResult>;

  /**
   * 批量修改文件时间戳
   */
  batchChangeTimestamp(
    paths: string[],
    timestamp: number,
    options?: BatchOptions
  ): Promise<BatchOperationResult>;
}
