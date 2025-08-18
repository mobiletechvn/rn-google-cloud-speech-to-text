import { NativeModules, NativeEventEmitter } from 'react-native';
import RnGoogleCloudSpeechToText from './NativeRnGoogleCloudSpeechToText';

const VoiceEmitter = new NativeEventEmitter(
  NativeModules.RnGoogleCloudSpeechToText
);

type SpeechEvent = keyof SpeechEvents;

enum ChannelCount {
  MONO = 1,
  STEREO,
}

type SampleRate = 16000 | 11025 | 22050 | 44100;
type Bitrate = 64000 | 96000 | 128000 | 192000 | 256000;

export interface SpeechEvents {
  onVoiceStart?: (e: VoiceStartEvent) => void;
  onVoice?: (e: VoiceEvent) => void;
  onVoiceEnd?: () => void;

  onSpeechError?: (e: SpeechErrorEvent) => void;
  onSpeechRecognized?: (e: SpeechRecognizeEvent) => void;
  onSpeechRecognizing?: (e: SpeechRecognizeEvent) => void;
}

export interface VoiceStartEvent {
  sampleRate: number;
  voiceRecorderState: number;
}

export interface VoiceEvent {
  size: number;
}

export interface SpeechErrorEvent {
  error?: {
    code?: string;
    message?: string;
  };
}

export interface SpeechRecognizeEvent {
  isFinal: boolean;
  transcript: string;
}

export interface SpeechStartEvent {
  fileId: string;
  tmpPath: string;
}

export interface StartOptions {
  speechToFile?: boolean;
  languageCode?: string;
}

export interface OutputFile {
  size: number;
  path: string;
}

export interface OutputConfig {
  sampleRate?: SampleRate;
  bitrate?: Bitrate;
  channel?: ChannelCount;
}

class GCSpeechToText {
  private readonly _events: Required<SpeechEvents>;
  private _listeners: any[] | null;

  constructor() {
    this._listeners = null;
    this._events = {
      onVoice: () => undefined,
      onVoiceEnd: () => undefined,
      onSpeechError: () => undefined,
      onVoiceStart: () => undefined,
      onSpeechRecognized: () => undefined,
      onSpeechRecognizing: () => undefined,
    };
  }

  /**
   * Start speech recognize
   * return file Id: string if set saveToFile = true
   * @param options
   */
  async start(options?: StartOptions): Promise<SpeechStartEvent> {
    if (!this._listeners) {
      this._listeners = (Object.keys(this._events) as SpeechEvent[]).map(
        (key) => VoiceEmitter.addListener(key, this._events[key])
      );
    }
    return await RnGoogleCloudSpeechToText.start(
      Object.assign(
        {
          languageCode: 'en-US',
          speechToFile: false,
        },
        options
      )
    );
  }

  async stop(): Promise<void> {
    return await RnGoogleCloudSpeechToText.stop();
  }

  setApiKey(apiKey: string): void {
    RnGoogleCloudSpeechToText.setApiKey(apiKey);
  }

  /**
   * get recognized voice as aac file
   * @param fileId
   * @param options
   */
  async getAudioFile(
    fileId: string,
    options?: OutputConfig
  ): Promise<OutputFile> {
    return await RnGoogleCloudSpeechToText.getAudioFile(
      fileId,
      Object.assign(
        { channel: ChannelCount.STEREO, sampleRate: 44100, bitrate: 96000 },
        options
      )
    );
  }

  removeListeners() {
    if (this._listeners) {
      this._listeners.map((listener) => listener.remove());
      this._listeners = null;
    }
    this._listeners = null;
  }

  async destroy() {
    this.removeListeners();
    await RnGoogleCloudSpeechToText.destroy();
  }

  onVoiceStart(fn: (data: VoiceStartEvent) => void) {
    this._events.onVoiceStart = fn;
  }

  onVoice(fn: (data: VoiceEvent) => void) {
    this._events.onVoice = fn;
  }

  onVoiceEnd(fn: () => void) {
    this._events.onVoiceEnd = fn;
  }

  onSpeechError(fn: (error: SpeechErrorEvent) => void) {
    this._events.onSpeechError = fn;
  }

  onSpeechRecognized(fn: (data: SpeechRecognizeEvent) => void) {
    this._events.onSpeechRecognized = fn;
  }

  onSpeechRecognizing(fn: (data: SpeechRecognizeEvent) => void) {
    this._events.onSpeechRecognizing = fn;
  }
}

export default new GCSpeechToText();

// Legacy export for backward compatibility
export function multiply(a: number, b: number): number {
  return RnGoogleCloudSpeechToText.multiply(a, b);
}

// Named exports
export { ChannelCount, type SampleRate, type Bitrate, type SpeechEvent };
