# Development Guide - Example App

This guide helps developers work with the Google Cloud Speech-to-Text example app during library development.

## 🏗️ Development Setup

### Prerequisites
- React Native development environment
- Node.js 18+
- Android Studio (for Android development)
- Xcode (for iOS development)
- Google Cloud account with Speech-to-Text API enabled

### Initial Setup

1. **Clone and Install**:
   ```bash
   git clone <repository-url>
   cd rn-google-cloud-speech-to-text/example
   npm install
   ```

2. **iOS Setup**:
   ```bash
   cd ios
   pod install
   cd ..
   ```

3. **Configure API Key**:
   - Copy `src/config.ts` to `src/config.local.ts` (gitignored)
   - Update `GOOGLE_CLOUD_API_KEY` in the local config
   - Or set it directly in the app UI

## 🚀 Running the Example

### Development Mode
```bash
# Start Metro bundler
npm start

# Run on Android (in separate terminal)
npm run android

# Run on iOS (in separate terminal)
npm run ios
```

### Clean Build
```bash
# Clean and run Android
npm run dev:android

# Clean and run iOS
npm run dev:ios

# Reset Metro cache
npm run reset
```

## 🧪 Testing During Development

### Current Testing Strategy

Since the native implementation is in progress, the example app currently provides:

1. **UI Testing**: Complete interface for all planned features
2. **Event System Testing**: Ready event listeners and handlers
3. **API Surface Testing**: All method signatures implemented
4. **Error Handling Testing**: Comprehensive error scenarios

### Testing TurboModule Integration

The `multiply` function serves as a basic TurboModule test:
```typescript
// Should return 21 if TurboModule is working
const result = multiply(3, 7);
```

### Testing Event Flow

Even without native implementation, you can test:
- Event listener setup/cleanup
- UI state management
- Error handling flow
- Logging system

## 🔧 Development Workflows

### 1. Testing Native Android Changes

```bash
# Clean and rebuild Android
cd android
./gradlew clean
cd ..
npm run android
```

### 2. Testing Native iOS Changes

```bash
# Clean iOS build
cd ios
xcodebuild clean
rm -rf build
pod install
cd ..
npm run ios
```

### 3. Testing JavaScript Changes

```bash
# Hot reload is enabled by default
# Save files and changes appear automatically
# For hard reset:
npm run reset
```

## 📱 Platform-Specific Development

### Android Development

**Key Files to Watch**:
- `android/src/main/java/com/rngooglecloudspeechtotext/RnGoogleCloudSpeechToTextModule.kt`
- `android/build.gradle` (dependencies)
- `android/src/main/AndroidManifest.xml` (permissions)

**Testing Commands**:
```bash
# View Android logs
npx react-native log-android

# Clean Android build
npm run clean:android

# Debug Android
npm run android -- --variant=debug
```

**Common Issues**:
- Build errors → Check Gradle dependencies
- Permission errors → Verify AndroidManifest.xml
- TurboModule not found → Check module registration

### iOS Development

**Key Files to Watch**:
- `ios/RnGoogleCloudSpeechToText.mm`
- `ios/RnGoogleCloudSpeechToText.h`
- `RnGoogleCloudSpeechToText.podspec`
- `example/ios/Podfile`

**Testing Commands**:
```bash
# View iOS logs
npx react-native log-ios

# Clean iOS build
npm run clean:ios

# Debug iOS
npm run ios -- --simulator="iPhone 14"
```

**Common Issues**:
- Build errors → Check CocoaPods dependencies
- Permission errors → Verify Info.plist
- TurboModule not found → Check header imports

## 🔍 Debugging Tips

### JavaScript Debugging

1. **Enable Remote Debugging**:
   - Shake device or press Cmd+D (iOS) / Cmd+M (Android)
   - Select "Debug JS Remotely"
   - Use Chrome DevTools

2. **Flipper Integration**:
   ```bash
   # Install Flipper
   npm install -g flipper-server
   flipper-server
   ```

3. **Console Logging**:
   ```typescript
   // Use the built-in debug logging
   import { debugLog } from './utils';
   debugLog('Testing feature', { data: 'example' });
   ```

### Native Debugging

1. **Android Studio**:
   - Open `android` folder in Android Studio
   - Set breakpoints in Kotlin code
   - Use "Attach to Process" for debugging

2. **Xcode**:
   - Open `ios/RnGoogleCloudSpeechToTextExample.xcworkspace`
   - Set breakpoints in Objective-C++ code
   - Run with debugger attached

### Network Debugging

```bash
# Monitor network traffic
npx react-native log-android | grep -i "network\|http\|api"
npx react-native log-ios | grep -i "network\|http\|api"
```

## 📊 Performance Monitoring

### Memory Usage

Monitor memory usage during audio recording:
```typescript
// Add to your testing code
const memoryInterval = setInterval(() => {
  if (__DEV__) {
    console.log('Memory usage check');
  }
}, 5000);
```

### Audio Performance

Test audio recording performance:
- Check audio buffer overflow
- Monitor audio quality
- Test on different devices
- Measure recognition latency

## 🧩 Integration Testing

### Mock Implementation Testing

While developing, you can mock native responses:

```typescript
// In config.ts, set:
DEBUG: {
  MOCK_API_RESPONSES: true,
}

// Create mock responses for testing UI
const mockStartResponse = {
  fileId: 'mock_file_123',
  tmpPath: '/tmp/mock_audio.wav'
};
```

### Error Scenario Testing

Test different error conditions:
```typescript
// Test API key errors
GoogleCloudSpeechToText.setApiKey('invalid_key');

// Test permission errors
// Deny microphone permission in device settings

// Test network errors
// Disable internet connection
```

## 📝 Development Checklist

### Before Committing Code

- [ ] All TypeScript errors resolved
- [ ] App builds successfully on both platforms
- [ ] No console errors in debug mode
- [ ] UI responds correctly to all user actions
- [ ] Error handling works as expected
- [ ] Logging system provides useful information
- [ ] Performance is acceptable on test devices

### Before Native Implementation

- [ ] JavaScript API is stable
- [ ] Event system is thoroughly tested
- [ ] UI handles all planned states
- [ ] Error messages are user-friendly
- [ ] Configuration options work correctly

### After Native Implementation

- [ ] Real audio recording works
- [ ] Google Cloud API integration functional
- [ ] Events emit correctly from native code
- [ ] File export functionality works
- [ ] Performance meets requirements
- [ ] Memory usage is acceptable
- [ ] Cross-platform behavior is consistent

## 🚦 Development Phases

### Phase 1: JavaScript Foundation ✅
- UI implementation complete
- Event system ready
- Error handling framework
- Configuration system

### Phase 2: Native Android Implementation ⏳
- Audio recording with AudioRecord
- Google Cloud Speech client
- Event emission to JavaScript
- File management

### Phase 3: Native iOS Implementation ⏳
- Audio recording with AVAudioEngine
- Google Cloud Speech client
- Event emission to JavaScript
- File management

### Phase 4: Integration & Polish ⏳
- Cross-platform testing
- Performance optimization
- Documentation completion
- Example app refinement

## 📞 Support During Development

### Getting Help

1. **Check TODO files** for implementation guidance
2. **Review logs** in the example app for debugging
3. **Test incrementally** - implement one feature at a time
4. **Use mock data** to test UI before native implementation

### Reporting Issues

When reporting issues, include:
- Platform (Android/iOS)
- Device/simulator information
- Steps to reproduce
- Console logs
- Expected vs actual behavior

This development guide will evolve as the native implementation progresses!
