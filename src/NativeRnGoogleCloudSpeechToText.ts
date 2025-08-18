import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface StartOptions {
  speechToFile?: boolean;
  languageCode?: string;
}

export interface SpeechStartEvent {
  fileId: string;
  tmpPath: string;
}

export interface OutputConfig {
  sampleRate?: number;
  bitrate?: number;
  channel?: number;
}

export interface OutputFile {
  size: number;
  path: string;
}

export interface Spec extends TurboModule {
  // Legacy method for testing
  multiply(a: number, b: number): number;

  // Core speech-to-text methods
  start(options: StartOptions): Promise<SpeechStartEvent>;
  stop(): Promise<void>;
  setApiKey(apiKey: string): void;
  getAudioFile(fileId: string, options: OutputConfig): Promise<OutputFile>;
  destroy(): Promise<void>;

  // Event listener management (handled in JS layer)
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'RnGoogleCloudSpeechToText'
);
