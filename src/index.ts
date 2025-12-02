import { registerPlugin } from '@capacitor/core';

import type { AdvancedFileManagerPlugin } from './definitions';

const AdvancedFileManager = registerPlugin<AdvancedFileManagerPlugin>('AdvancedFileManager', {
  web: () => import('./web').then((m) => new m.AdvancedFileManagerWeb()),
});

export * from './definitions';
export { AdvancedFileManager };

// 模块化功能导出 - 暂时注释掉，避免 Rollup 打包问题
// export * from './modules/search';
// export * from './modules/batch';
// export * from './modules/utils';        // 待实现
// export * from './modules/preview';      // 待实现
// export * from './modules/share';        // 待实现
// export * from './modules/web-enhanced'; // Web端增强功能
