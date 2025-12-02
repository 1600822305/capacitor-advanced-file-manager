import { registerPlugin } from '@capacitor/core';
import type { BatchOperationsPlugin } from './definitions';

const BatchOperations = registerPlugin<BatchOperationsPlugin>('BatchOperations', {
  web: () => import('./web').then(m => new m.BatchOperationsWeb()),
});

export * from './definitions';
export { BatchOperations };
