export interface AdvancedFileManagerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
