# iOS Development TODO - Google Cloud Speech-to-Text

## 🚧 Current Status
- ✅ Basic TurboModule structure implemented
- ✅ Method stubs created with proper error handling
- ⏳ Google Cloud Speech-to-Text integration needed
- ⏳ Audio recording implementation needed
- ⏳ Real-time streaming implementation needed

## 📋 Implementation Tasks

### 1. Dependencies & Setup
- [ ] Add Google Cloud Speech-to-Text dependency via CocoaPods
  ```ruby
  pod 'GoogleCloudSpeech', '~> 4.x'
  pod 'gRPC', '~> 1.x'
  ```
- [ ] Configure iOS deployment target (minimum iOS 11.0)
- [ ] Add microphone usage permission to Info.plist
- [ ] Set up proper signing and provisioning profiles
- [ ] Configure network security settings for HTTPS

### 2. Audio Recording Implementation
- [ ] Implement AVAudioEngine for real-time audio capture
- [ ] Configure AVAudioSession for recording:
  - [ ] Set category to `.record` or `.playAndRecord`
  - [ ] Handle audio session interruptions
  - [ ] Manage audio route changes
- [ ] Set up audio format (16-bit PCM, 16kHz sample rate)
- [ ] Implement audio buffer management with AVAudioPCMBuffer
- [ ] Add voice activity detection
- [ ] Handle background audio recording
- [ ] Implement audio level monitoring

### 3. Google Cloud Speech Client Setup
- [ ] Initialize GCSSpeechClient with API key authentication
- [ ] Implement credential management:
  - [ ] Service account JSON file support
  - [ ] API key configuration
  - [ ] OAuth2 token handling
- [ ] Configure speech recognition settings:
  - [ ] Language code support
  - [ ] Audio encoding (LINEAR16)
  - [ ] Sample rate (16000 Hz)
  - [ ] Enable automatic punctuation
  - [ ] Enable speaker diarization (optional)
- [ ] Handle client lifecycle and cleanup

### 4. Streaming Recognition Implementation
- [ ] Implement bidirectional streaming with Google Cloud Speech API
- [ ] Create audio streaming pipeline:
  - [ ] AVAudioEngine → AVAudioPCMBuffer → gRPC stream
- [ ] Handle streaming states properly
- [ ] Implement stream lifecycle management
- [ ] Add reconnection logic for network failures
- [ ] Process partial and final recognition results
- [ ] Handle stream timeouts and errors

### 5. Event Emission to JavaScript
- [ ] Inherit from RCTEventEmitter for proper event emission
- [ ] Implement required RCTEventEmitter methods:
  - [ ] `supportedEvents`
  - [ ] `startObserving`
  - [ ] `stopObserving`
- [ ] Create event data structures matching TypeScript interfaces:
  - [ ] `onVoiceStart` - with sampleRate and voiceRecorderState
  - [ ] `onVoice` - with audio data size
  - [ ] `onVoiceEnd` - when voice detection stops
  - [ ] `onSpeechRecognizing` - for interim results
  - [ ] `onSpeechRecognized` - for final results
  - [ ] `onSpeechError` - with detailed error information
- [ ] Ensure thread safety when emitting events

### 6. Audio File Management
- [ ] Implement audio file recording using AVAudioRecorder
- [ ] Support multiple audio formats:
  - [ ] WAV (uncompressed)
  - [ ] AAC (compressed)
  - [ ] MP3 (if needed)
- [ ] Implement audio format conversion using AVAudioConverter
- [ ] Add configurable audio quality settings:
  - [ ] Sample rate (16kHz, 22kHz, 44.1kHz)
  - [ ] Bit rate (64k, 96k, 128k, 192k, 256k)
  - [ ] Channel count (MONO, STEREO)
- [ ] Implement file management in Documents/tmp directory
- [ ] Add file size calculation and reporting

### 7. Memory Management & Performance
- [ ] Implement proper ARC memory management
- [ ] Handle large audio buffers efficiently
- [ ] Optimize audio processing pipeline
- [ ] Implement audio buffer pooling
- [ ] Add background processing for audio streaming
- [ ] Optimize gRPC connection management
- [ ] Handle memory warnings appropriately

### 8. Error Handling & Edge Cases
- [ ] Handle microphone permission denial
- [ ] Implement network connectivity checks
- [ ] Handle API quota exceeded errors
- [ ] Add timeout handling for recognition requests
- [ ] Handle app lifecycle events:
  - [ ] App backgrounding/foregrounding
  - [ ] App termination
  - [ ] Audio session interruptions
- [ ] Handle hardware changes (headphones, AirPods)
- [ ] Add comprehensive logging for debugging

### 9. iOS-Specific Features
- [ ] Support background audio processing
- [ ] Handle CallKit integration (if needed)
- [ ] Implement proper audio session management
- [ ] Support audio routing (speaker, headphones, Bluetooth)
- [ ] Handle Control Center audio controls
- [ ] Support iOS accessibility features
- [ ] Implement proper keyboard shortcuts (if applicable)

### 10. Configuration & Customization
- [ ] Support multiple language codes
- [ ] Implement custom recognition models
- [ ] Add profanity filtering options
- [ ] Support custom vocabulary/phrases
- [ ] Implement alternative recognition results
- [ ] Add confidence score reporting
- [ ] Support different audio qualities for different devices

### 11. Testing & Validation
- [ ] Create unit tests for core functionality
- [ ] Add integration tests with mock Google Cloud responses
- [ ] Test on different iOS devices and versions
- [ ] Validate audio recording quality across devices
- [ ] Test event emission timing and reliability
- [ ] Add performance tests for memory and CPU usage
- [ ] Test background audio processing
- [ ] Add end-to-end tests with real audio

## 🔧 Technical Implementation Details

### Key Classes to Implement:
1. **AudioRecordingManager** - Handle AVAudioEngine recording
2. **SpeechRecognitionManager** - Manage Google Cloud Speech client
3. **AudioStreamProcessor** - Process audio data for streaming
4. **EventEmitterManager** - Handle event emission to JavaScript
5. **AudioFileManager** - Handle audio file operations
6. **AudioSessionManager** - Manage AVAudioSession lifecycle

### Required Info.plist Entries:
```xml
<key>NSMicrophoneUsageDescription</key>
<string>This app needs access to the microphone for speech recognition</string>
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

### CocoaPods Dependencies:
```ruby
# Google Cloud Speech
pod 'GoogleCloudSpeech', '~> 4.x'
pod 'gRPC', '~> 1.x'

# Audio processing
pod 'AudioToolbox'
pod 'AVFoundation'
```

### Header File Updates Needed:
```objc
#import <React/RCTEventEmitter.h>
#import <AVFoundation/AVFoundation.h>
#import <AudioToolbox/AudioToolbox.h>

@interface RnGoogleCloudSpeechToText : RCTEventEmitter <RCTBridgeModule>
// ... existing interface
@end
```

## 📚 Resources & Documentation
- [Google Cloud Speech-to-Text iOS Documentation](https://cloud.google.com/speech-to-text/docs/libraries#client-libraries-install-objc)
- [AVAudioEngine Documentation](https://developer.apple.com/documentation/avfoundation/avaudioengine)
- [AVAudioSession Documentation](https://developer.apple.com/documentation/avfoundation/avaudiosession)
- [React Native TurboModules iOS Guide](https://reactnative.dev/docs/the-new-architecture/pillars-turbomodules)
- [iOS Audio Development Guide](https://developer.apple.com/library/archive/documentation/MusicAudio/Conceptual/AudioUnitHostingGuide_iOS/)

## 🎯 Priority Order
1. **HIGH**: AVAudioEngine recording and basic speech recognition
2. **HIGH**: Event emission using RCTEventEmitter
3. **MEDIUM**: Audio file export functionality
4. **MEDIUM**: Proper audio session management
5. **LOW**: Advanced iOS-specific features and optimizations

## 📝 iOS-Specific Notes
- Test on both physical devices and simulators
- Consider different iOS versions (minimum iOS 11.0)
- Handle audio session interruptions properly (calls, alarms, etc.)
- Test with different audio hardware (built-in mic, AirPods, etc.)
- Implement proper background audio handling
- Consider battery usage optimization
- Follow iOS Human Interface Guidelines for audio apps
- Test with VoiceOver and accessibility features
- Handle different device capabilities (iPhone vs iPad)

## ⚠️ Important Considerations
- iOS has strict background audio limitations
- Microphone permission is required and must be handled gracefully
- Audio session management is critical for proper operation
- Memory management is crucial when dealing with audio buffers
- Network requests must handle iOS background app refresh settings
- Consider iOS 14+ privacy indicators for microphone usage
