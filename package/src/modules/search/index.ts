import { registerPlugin } from '@capacitor/core';
import type { FileSearchPlugin } from './definitions';

const FileSearch = registerPlugin<FileSearchPlugin>('FileSearch', {
  web: () => import('./web').then(m => new m.FileSearchWeb()),
});

export * from './definitions';
export { FileSearch };
