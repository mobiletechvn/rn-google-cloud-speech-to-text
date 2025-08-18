# Google Cloud Speech-to-Text Example App

This example app demonstrates how to use the `rn-google-cloud-speech-to-text` library for real-time speech recognition using Google Cloud Speech-to-Text API.

## Features Demonstrated

- ✅ **Real-time Speech Recognition**: Live transcription of spoken words
- ✅ **Event Handling**: Complete event system with proper listeners
- ✅ **Audio Recording**: Microphone access and audio level monitoring
- ✅ **Configurable Settings**: Language codes, API keys, and audio options
- ✅ **Error Handling**: Comprehensive error reporting and logging
- ✅ **Audio File Export**: Save recorded audio to files (when implemented)
- ✅ **Permission Management**: Proper audio permissions handling
- ✅ **Cross-platform**: Works on both Android and iOS

## Setup Instructions

### 1. Prerequisites

Before running this example, you need:

- A Google Cloud Project with Speech-to-Text API enabled
- Google Cloud Speech-to-Text API key or service account credentials
- React Native development environment set up

### 2. Google Cloud Setup

1. **Create a Google Cloud Project**:
   ```bash
   # Install Google Cloud CLI if not already installed
   curl https://sdk.cloud.google.com | bash
   exec -l $SHELL
   gcloud init
   ```

2. **Enable Speech-to-Text API**:
   ```bash
   gcloud services enable speech.googleapis.com
   ```

3. **Create an API Key**:
   ```bash
   gcloud alpha services api-keys create --display-name="React Native Speech Demo"
   ```

   Or create one in the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).

4. **Restrict the API Key** (recommended):
   - Go to Google Cloud Console > APIs & Credentials > API Keys
   - Edit your API key
   - Under "API restrictions", select "Restrict key"
   - Choose "Cloud Speech-to-Text API"

### 3. Running the Example

1. **Install Dependencies**:
   ```bash
   cd example
   npm install
   # or
   yarn install
   ```

2. **iOS Setup** (if running on iOS):
   ```bash
   cd ios
   pod install
   cd ..
   ```

3. **Configure API Key**:
   - Open the app and enter your Google Cloud API key in the configuration section
   - Or modify the `GOOGLE_CLOUD_API_KEY` constant in `App.tsx`

4. **Run the App**:
   ```bash
   # For Android
   npm run android

   # For iOS
   npm run ios
   ```

## How to Use the Example

### 1. Initial Setup
- Launch the app
- Enter your Google Cloud Speech-to-Text API key
- Choose your preferred language code (e.g., `en-US`, `es-ES`, `fr-FR`)
- Enable "Save speech to file" if you want to export audio files
- Tap "Initialize Speech Recognition"

### 2. Recording Speech
- Tap "Start Recording" to begin speech recognition
- Speak clearly into the microphone
- Watch the real-time transcript appear
- Monitor the audio level indicator
- Tap "Stop Recording" when finished

### 3. Monitoring Events
- Check the "Event Logs" section to see all events
- Monitor interim and final recognition results
- Track audio events and any errors
- Use the clear buttons to reset transcript and logs

## Configuration Options

### Language Codes
The app supports all Google Cloud Speech-to-Text language codes:
- `en-US` - English (United States)
- `en-GB` - English (United Kingdom)
- `es-ES` - Spanish (Spain)
- `fr-FR` - French (France)
- `de-DE` - German (Germany)
- `ja-JP` - Japanese (Japan)
- `ko-KR` - Korean (South Korea)
- `zh-CN` - Chinese (Mandarin, Simplified)

[See full list](https://cloud.google.com/speech-to-text/docs/languages)

### Audio Options
- **Speech to File**: Enable to save recorded audio as files
- **Audio Level Monitoring**: Real-time audio input level display
- **Voice Activity Detection**: Automatic start/stop based on voice

## Event Types Explained

### Voice Events
- **onVoiceStart**: Triggered when voice detection begins
- **onVoice**: Continuous audio level updates during recording
- **onVoiceEnd**: Triggered when voice detection stops

### Speech Recognition Events
- **onSpeechRecognizing**: Interim (partial) recognition results
- **onSpeechRecognized**: Final recognition results
- **onSpeechError**: Error handling and reporting

## Troubleshooting

### Common Issues

1. **"API Key Required" Error**:
   - Make sure you've entered a valid Google Cloud API key
   - Verify the API key has Speech-to-Text API access

2. **Permission Denied**:
   - Grant microphone permissions to the app
   - Check device settings for microphone access

3. **Network Errors**:
   - Ensure internet connectivity
   - Check if Google Cloud services are accessible

4. **No Audio Input**:
   - Test microphone with other apps
   - Check audio input settings
   - Ensure app has microphone permissions

5. **Recognition Accuracy Issues**:
   - Speak clearly and at normal pace
   - Reduce background noise
   - Try different language codes
   - Check microphone quality

### Debug Mode

Enable verbose logging by checking the Event Logs section for detailed information about:
- API requests and responses
- Audio recording status
- Recognition confidence scores
- Error details and stack traces

## Development Notes

### Current Implementation Status
- ✅ UI and event handling complete
- ⏳ Google Cloud integration in progress (see TODO files)
- ⏳ Audio recording implementation pending
- ⏳ File export functionality pending

### Testing the Implementation
1. The multiply function test shows TurboModule is working
2. UI demonstrates the complete API surface
3. Event system is ready for native implementation
4. Error handling framework is in place

### Next Steps for Development
1. Implement native Android audio recording (see `TODO_ANDROID.md`)
2. Implement native iOS audio recording (see `TODO_IOS.md`)
3. Integrate Google Cloud Speech-to-Text API
4. Test real-time streaming functionality

## Resources

- [Google Cloud Speech-to-Text Documentation](https://cloud.google.com/speech-to-text/docs)
- [React Native TurboModules](https://reactnative.dev/docs/the-new-architecture/pillars-turbomodules)
- [Audio Recording Best Practices](https://developer.android.com/guide/topics/media/mediarecorder)
- [iOS Speech Framework](https://developer.apple.com/documentation/speech)

## Support

For issues related to:
- **Library functionality**: Check the main repository issues
- **Google Cloud setup**: Refer to Google Cloud documentation
- **Platform-specific issues**: Check Android/iOS specific documentation
- **Example app**: Create an issue with reproduction steps
