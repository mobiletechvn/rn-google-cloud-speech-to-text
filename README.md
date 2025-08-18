import GoogleCloudSpeechToText, {
  SpeechRecognizeEvent,
  VoiceStartEvent,
  SpeechErrorEvent,
  VoiceEvent,
  SpeechStartEvent,
} from 'rn-google-cloud-speech-to-text';
import { useEffect } from 'react';

const Separator = () => <View style={styles.separator} />;

export default function App() {
  const [transcript, setResult] = React.useState<string>('');

  useEffect(() => {
    PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.RECORD_AUDIO, {
      title: 'Cool Photo App Camera Permission',
      message:
        'Cool Photo App needs access to your camera ' +
        'so you can take awesome pictures.',
      buttonNeutral: 'Ask Me Later',
      buttonNegative: 'Cancel',
      buttonPositive: 'OK',
    });
  }, []);

  useEffect(() => {
    // GoogleCloudSpeechToText.setApiKey('key_____');
    GoogleCloudSpeechToText.onVoice(onVoice);
    GoogleCloudSpeechToText.onVoiceStart(onVoiceStart);
    GoogleCloudSpeechToText.onVoiceEnd(onVoiceEnd);
    GoogleCloudSpeechToText.onSpeechError(onSpeechError);
    GoogleCloudSpeechToText.onSpeechRecognized(onSpeechRecognized);
    GoogleCloudSpeechToText.onSpeechRecognizing(onSpeechRecognizing);
    return () => {
      GoogleCloudSpeechToText.removeListeners();
    };
  }, []);

  const onSpeechError = (_error: SpeechErrorEvent) => {
    console.log('onSpeechError: ', _error);
  };

  const onSpeechRecognized = (result: SpeechRecognizeEvent) => {
    console.log('onSpeechRecognized: ', result);
    setResult(result.transcript);
  };

  const onSpeechRecognizing = (result: SpeechRecognizeEvent) => {
    console.log('onSpeechRecognizing: ', result);
    setResult(result.transcript);
  };

  const onVoiceStart = (_event: VoiceStartEvent) => {
    console.log('onVoiceStart', _event);
  };

  const onVoice = (_event: VoiceEvent) => {
    console.log('onVoice', _event);
  };

  const onVoiceEnd = () => {
    console.log('onVoiceEnd: ');
  };

  const startRecognizing = async () => {
    const result: SpeechStartEvent = await GoogleCloudSpeechToText.start({
      speechToFile: true,
    });
    console.log('startRecognizing', result);
  };

  const stopRecognizing = async () => {
    await GoogleCloudSpeechToText.stop();
  };
```

```

import { NativeModules, NativeEventEmitter } from 'react-native';

const { GoogleCloudSpeechToText } = NativeModules;

const VoiceEmitter = new NativeEventEmitter(GoogleCloudSpeechToText);

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
   * return file Id: number if set saveToFile = true
   * @param options
   */
  async start(options?: StartOptions): Promise<SpeechStartEvent> {
    if (!this._listeners) {
      this._listeners = (Object.keys(
        this._events
      ) as SpeechEvent[]).map((key) =>
        VoiceEmitter.addListener(key, this._events[key])
      );
    }
    return await GoogleCloudSpeechToText.start(
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
    return await GoogleCloudSpeechToText.stop();
  }

  setApiKey(apiKey: string): void {
    GoogleCloudSpeechToText.setApiKey(apiKey);
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
    return await GoogleCloudSpeechToText.getAudioFile(
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
    await GoogleCloudSpeechToText.destroy();
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
```


## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
