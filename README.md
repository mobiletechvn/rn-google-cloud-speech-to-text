# rn-google-cloud-speech-to-text

A React Native library for integrating Google Cloud Speech-to-Text API with real-time speech recognition capabilities.

## Features

- Real-time speech recognition using Google Cloud Speech-to-Text API
- Support for multiple languages
- Audio file export functionality
- TypeScript support
- Cross-platform (iOS & Android)
- Event-based architecture with comprehensive callbacks
- Configurable audio settings (sample rate, bitrate, channels)

## Installation

```bash
npm install rn-google-cloud-speech-to-text
```

### iOS Setup

Add the following to your `ios/Podfile`:

```ruby
pod 'RnGoogleCloudSpeechToText', :path => '../node_modules/rn-google-cloud-speech-to-text'
```

Then run:

```bash
cd ios && pod install
```

### Android Setup

The library should work out of the box for Android after installation. However, you may need to add packaging options to resolve potential conflicts with META-INF files.

Add the following to your `android/build.gradle` (app level) inside the `android` block:

```gradle
android {
  // ... other configurations

  packagingOptions {
    resources.excludes.add("META-INF/*")
  }
}
```

## Permissions

### iOS

Add the following to your `Info.plist`:

```xml
<key>NSMicrophoneUsageDescription</key>
<string>This app needs access to the microphone for speech recognition.</string>
```

### Android

Add the following to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

And request permission in your component:

```typescript
import { PermissionsAndroid, Platform } from 'react-native';

const requestPermissions = async () => {
  if (Platform.OS === 'android') {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
      {
        title: 'Speech Recognition Audio Permission',
        message: 'This app needs access to your microphone for speech recognition.',
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      }
    );
    return granted === PermissionsAndroid.RESULTS.GRANTED;
  }
  return true;
};
```

## Usage

### Basic Setup

```typescript
import GoogleCloudSpeechToText, {
  type SpeechRecognizeEvent,
  type VoiceStartEvent,
  type SpeechErrorEvent,
  type VoiceEvent,
  type SpeechStartEvent,
} from 'rn-google-cloud-speech-to-text';

export default function App() {
  const [transcript, setTranscript] = useState<string>('');
  const [isRecording, setIsRecording] = useState<boolean>(false);
  const [isInitialized, setIsInitialized] = useState<boolean>(false);

  useEffect(() => {
    initializeSpeechRecognition();
    return () => {
      cleanup();
    };
  }, []);

  const initializeSpeechRecognition = () => {
    // Set your Google Cloud API key
    GoogleCloudSpeechToText.setApiKey('YOUR_GOOGLE_CLOUD_API_KEY');

    // Set up event listeners
    GoogleCloudSpeechToText.onVoice(onVoice);
    GoogleCloudSpeechToText.onVoiceStart(onVoiceStart);
    GoogleCloudSpeechToText.onVoiceEnd(onVoiceEnd);
    GoogleCloudSpeechToText.onSpeechError(onSpeechError);
    GoogleCloudSpeechToText.onSpeechRecognized(onSpeechRecognized);
    GoogleCloudSpeechToText.onSpeechRecognizing(onSpeechRecognizing);

    setIsInitialized(true);
  };

  const cleanup = () => {
    GoogleCloudSpeechToText.removeListeners();
    GoogleCloudSpeechToText.destroy();
  };

  // Event handlers
  const onSpeechError = (error: SpeechErrorEvent) => {
    console.log('Speech error:', error.error?.message);
    setIsRecording(false);
  };

  const onSpeechRecognized = (result: SpeechRecognizeEvent) => {
    console.log('Final result:', result.transcript);
    setTranscript(result.transcript);
  };

  const onSpeechRecognizing = (result: SpeechRecognizeEvent) => {
    console.log('Interim result:', result.transcript);
    setTranscript(result.transcript);
  };

  const onVoiceStart = (event: VoiceStartEvent) => {
    console.log('Voice started, sample rate:', event.sampleRate);
    setIsRecording(true);
  };

  const onVoice = (event: VoiceEvent) => {
    // Audio level for visualization
    console.log('Audio level:', event.size);
  };

  const onVoiceEnd = () => {
    console.log('Voice ended');
    setIsRecording(false);
  };

  const startRecognizing = async () => {
    if (!isInitialized) return;

    try {
      const result: SpeechStartEvent = await GoogleCloudSpeechToText.start({
        speechToFile: false,
        languageCode: 'en-US',
      });
      console.log('Started recording, file ID:', result.fileId);
    } catch (error) {
      console.error('Start error:', error);
    }
  };

  const stopRecognizing = async () => {
    try {
      await GoogleCloudSpeechToText.stop();
      console.log('Stopped recording');
    } catch (error) {
      console.error('Stop error:', error);
    }
  };

  return (
    <View>
      <Text>{transcript || 'Transcript will appear here...'}</Text>
      <TouchableOpacity onPress={startRecognizing} disabled={isRecording}>
        <Text>Start Recording</Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={stopRecognizing} disabled={!isRecording}>
        <Text>Stop Recording</Text>
      </TouchableOpacity>
    </View>
  );
}
```

### Advanced Usage with Audio File Export

```typescript
const [speechToFile, setSpeechToFile] = useState<boolean>(true);
const [currentFileId, setCurrentFileId] = useState<string>('');
const [audioFilePath, setAudioFilePath] = useState<string>('');

const startRecordingWithFile = async () => {
  try {
    const result: SpeechStartEvent = await GoogleCloudSpeechToText.start({
      speechToFile: true,
      languageCode: 'vi-VN', // Vietnamese example
    });
    setCurrentFileId(result.fileId);
  } catch (error) {
    console.error('Start recording error:', error);
  }
};

const getAudioFile = async () => {
  if (!currentFileId) return;

  try {
    const result = await GoogleCloudSpeechToText.getAudioFile(currentFileId, {
      sampleRate: 44100,
      bitrate: 128000,
      channel: 2, // Stereo
    });

    setAudioFilePath(result.path);
    console.log(`Audio file saved: ${result.path} (${result.size} bytes)`);
  } catch (error) {
    console.error('Get audio file error:', error);
  }
};
```

## API Reference

### Methods

#### `setApiKey(apiKey: string): void`
Set your Google Cloud Speech-to-Text API key.

#### `start(options?: StartOptions): Promise<SpeechStartEvent>`
Start speech recognition.

**Options:**
- `speechToFile?: boolean` - Whether to save audio to file (default: false)
- `languageCode?: string` - Language code for recognition (default: 'en-US')

#### `stop(): Promise<void>`
Stop speech recognition.

#### `getAudioFile(fileId: string, options?: OutputConfig): Promise<OutputFile>`
Get the recorded audio file.

**Options:**
- `sampleRate?: SampleRate` - Audio sample rate (16000 | 11025 | 22050 | 44100)
- `bitrate?: Bitrate` - Audio bitrate (64000 | 96000 | 128000 | 192000 | 256000)
- `channel?: ChannelCount` - Channel count (1 = MONO, 2 = STEREO)

#### `removeListeners(): void`
Remove all event listeners.

#### `destroy(): Promise<void>`
Clean up resources.

### Event Listeners

#### `onVoiceStart(callback: (event: VoiceStartEvent) => void): void`
Called when voice input starts.

#### `onVoice(callback: (event: VoiceEvent) => void): void`
Called during voice input with audio level information.

#### `onVoiceEnd(callback: () => void): void`
Called when voice input ends.

#### `onSpeechError(callback: (error: SpeechErrorEvent) => void): void`
Called when an error occurs.

#### `onSpeechRecognized(callback: (result: SpeechRecognizeEvent) => void): void`
Called when final speech recognition result is available.

#### `onSpeechRecognizing(callback: (result: SpeechRecognizeEvent) => void): void`
Called with interim speech recognition results.

### Types

```typescript
interface VoiceStartEvent {
  sampleRate: number;
  voiceRecorderState: number;
}

interface VoiceEvent {
  size: number;
}

interface SpeechErrorEvent {
  error?: {
    code?: string;
    message?: string;
  };
}

interface SpeechRecognizeEvent {
  isFinal: boolean;
  transcript: string;
}

interface SpeechStartEvent {
  fileId: string;
  tmpPath: string;
}

interface StartOptions {
  speechToFile?: boolean;
  languageCode?: string;
}

interface OutputFile {
  size: number;
  path: string;
}

interface OutputConfig {
  sampleRate?: SampleRate;
  bitrate?: Bitrate;
  channel?: ChannelCount;
}
```

## Supported Languages

The library supports all languages supported by Google Cloud Speech-to-Text API. Some common language codes:

- `en-US` - English (US)
- `en-GB` - English (UK)
- `es-ES` - Spanish (Spain)
- `fr-FR` - French (France)
- `de-DE` - German (Germany)
- `ja-JP` - Japanese (Japan)
- `ko-KR` - Korean (South Korea)
- `zh-CN` - Chinese (Simplified)
- `vi-VN` - Vietnamese

## Example

Check out the [example app](./example) for a complete implementation with:

- Full UI for configuration
- Real-time audio level visualization
- Comprehensive event logging
- Audio file export functionality
- Error handling
- Permission management

To run the example:

```bash
cd example
npm install
# For iOS
cd ios && pod install && cd ..
npx react-native run-ios
# For Android
npx react-native run-android
```

## Google Cloud Setup

1. Create a Google Cloud project
2. Enable the Speech-to-Text API
3. Create credentials (API key or service account)
4. Use the API key in your app

For detailed setup instructions, visit the [Google Cloud Speech-to-Text documentation](https://cloud.google.com/speech-to-text/docs).

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

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
```

## Supported Languages

The library supports all languages supported by Google Cloud Speech-to-Text API. Some common language codes:

- `en-US` - English (US)
- `en-GB` - English (UK)
- `es-ES` - Spanish (Spain)
- `fr-FR` - French (France)
- `de-DE` - German (Germany)
- `ja-JP` - Japanese (Japan)
- `ko-KR` - Korean (South Korea)
- `zh-CN` - Chinese (Simplified)
- `vi-VN` - Vietnamese

## Example

Check out the [example app](./example) for a complete implementation with:

- Full UI for configuration
- Real-time audio level visualization
- Comprehensive event logging
- Audio file export functionality
- Error handling
- Permission management

To run the example:

```bash
cd example
npm install
# For iOS
cd ios && pod install && cd ..
npx react-native run-ios
# For Android
npx react-native run-android
```

## Google Cloud Setup

1. Create a Google Cloud project
2. Enable the Speech-to-Text API
3. Create credentials (API key or service account)
4. Use the API key in your app

For detailed setup instructions, visit the [Google Cloud Speech-to-Text documentation](https://cloud.google.com/speech-to-text/docs).

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
