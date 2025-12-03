import { registerPlugin } from '@capacitor/core';
import type { WebEnhancedFileManagerPlugin } from './definitions';

const WebEnhancedFileManager = registerPlugin<WebEnhancedFileManagerPlugin>('WebEnhancedFileManager', {
  web: () => import('./web').then(m => new m.WebEnhancedFileManagerWeb()),
});

export * from './definitions';
export * from './breakthrough-utils';
export { WebEnhancedFileManager };
