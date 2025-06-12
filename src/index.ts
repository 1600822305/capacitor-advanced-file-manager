import { registerPlugin } from '@capacitor/core';

import type { AdvancedFileManagerPlugin } from './definitions';

const AdvancedFileManager = registerPlugin<AdvancedFileManagerPlugin>('AdvancedFileManager', {
  web: () => import('./web').then((m) => new m.AdvancedFileManagerWeb()),
});

export * from './definitions';
export { AdvancedFileManager };
