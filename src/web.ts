import { WebPlugin } from '@capacitor/core';

import type { AdvancedFileManagerPlugin } from './definitions';

export class AdvancedFileManagerWeb extends WebPlugin implements AdvancedFileManagerPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
