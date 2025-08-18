import { useEffect, useState } from 'react';
import {
  Text,
  View,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Alert,
  PermissionsAndroid,
  Platform,
  SafeAreaView,
  ActivityIndicator,
  TextInput,
} from 'react-native';

import GoogleCloudSpeechToText, {
  type SpeechRecognizeEvent,
  type VoiceStartEvent,
  type SpeechErrorEvent,
  type VoiceEvent,
  type SpeechStartEvent,
  multiply,
} from 'rn-google-cloud-speech-to-text';

// Demo API key placeholder - Replace with your actual API key
const GOOGLE_CLOUD_API_KEY = 'AAA';

interface LogEntry {
  timestamp: string;
  type: 'info' | 'error' | 'success';
  message: string;
}

export default function App() {
  const [transcript, setTranscript] = useState<string>('');
  const [isRecording, setIsRecording] = useState<boolean>(false);
  const [isInitialized, setIsInitialized] = useState<boolean>(false);
  const [apiKey, setApiKey] = useState<string>(GOOGLE_CLOUD_API_KEY);
  const [languageCode, setLanguageCode] = useState<string>('vi-VN');
  const [speechToFile, setSpeechToFile] = useState<boolean>(false);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [audioLevel, setAudioLevel] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [currentFileId, setCurrentFileId] = useState<string>('');
  const [audioFilePath, setAudioFilePath] = useState<string>('');

  // Legacy multiply test
  const multiplyResult = multiply(3, 7);

  const addLog = (type: LogEntry['type'], message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prev) => [
      { timestamp, type, message },
      ...prev.slice(0, 99), // Keep only last 100 logs
    ]);
    console.log(`[${timestamp}] ${type.toUpperCase()}: ${message}`);
  };

  useEffect(() => {
    requestPermissions();
    return () => {
      cleanup();
    };
  }, []);

  const requestPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
          {
            title: 'Speech Recognition Audio Permission',
            message:
              'This app needs access to your microphone for speech recognition.',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          }
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          addLog('success', 'Audio permission granted');
        } else {
          addLog('error', 'Audio permission denied');
        }
      } catch (err) {
        addLog('error', `Permission error: ${err}`);
      }
    }
  };

  const initializeSpeechRecognition = () => {
    // if (!apiKey || apiKey === GOOGLE_CLOUD_API_KEY) {
    //   Alert.alert(
    //     'API Key Required',
    //     'Please enter your Google Cloud Speech-to-Text API key to use this demo.'
    //   );
    //   return;
    // }

    try {
      GoogleCloudSpeechToText.setApiKey(apiKey);

      // Set up event listeners
      GoogleCloudSpeechToText.onVoice(onVoice);
      GoogleCloudSpeechToText.onVoiceStart(onVoiceStart);
      GoogleCloudSpeechToText.onVoiceEnd(onVoiceEnd);
      GoogleCloudSpeechToText.onSpeechError(onSpeechError);
      GoogleCloudSpeechToText.onSpeechRecognized(onSpeechRecognized);
      GoogleCloudSpeechToText.onSpeechRecognizing(onSpeechRecognizing);

      setIsInitialized(true);
      addLog('success', 'Speech recognition initialized');
    } catch (error) {
      addLog('error', `Initialization error: ${error}`);
    }
  };

  const cleanup = () => {
    try {
      GoogleCloudSpeechToText.removeListeners();
      GoogleCloudSpeechToText.destroy();
      setIsInitialized(false);
      addLog('info', 'Cleanup completed');
    } catch (error) {
      addLog('error', `Cleanup error: ${error}`);
    }
  };

  // Event handlers
  const onSpeechError = (error: SpeechErrorEvent) => {
    addLog('error', `Speech error: ${error.error?.message || 'Unknown error'}`);
    setIsRecording(false);
    setIsLoading(false);
  };

  const onSpeechRecognized = (result: SpeechRecognizeEvent) => {
    addLog('success', `Final result: ${result.transcript}`);
    setTranscript(result.transcript);
    // Don't set isRecording to false here - let onVoiceEnd handle it
    setIsLoading(false);
  };

  const onSpeechRecognizing = (result: SpeechRecognizeEvent) => {
    addLog('info', `Interim result: ${result.transcript}`);
    setTranscript(result.transcript);
  };

  const onVoiceStart = (event: VoiceStartEvent) => {
    addLog('info', `Voice started - Sample rate: ${event.sampleRate}Hz`);
    addLog('info', `State: isRecording=${isRecording}, isLoading=${isLoading}`);
    setIsRecording(true);
    setIsLoading(false); // Clear loading state when recording actually starts
  };

  const onVoice = (event: VoiceEvent) => {
    setAudioLevel(event.size);
  };

  const onVoiceEnd = () => {
    addLog('info', 'Voice ended');
    addLog(
      'info',
      `State before: isRecording=${isRecording}, isLoading=${isLoading}`
    );
    setIsRecording(false);
    setAudioLevel(0);
    setIsLoading(false); // Ensure loading state is cleared
  };

  const startRecognizing = async () => {
    if (!isInitialized) {
      Alert.alert(
        'Not Initialized',
        'Please initialize speech recognition first.'
      );
      return;
    }

    try {
      addLog(
        'info',
        `Starting - Current state: isRecording=${isRecording}, isLoading=${isLoading}`
      );
      setIsLoading(true);
      addLog('info', 'Starting speech recognition...');

      const result: SpeechStartEvent = await GoogleCloudSpeechToText.start({
        speechToFile,
        languageCode,
      });

      addLog('success', `Started recording - File ID: ${result.fileId}`);
      setCurrentFileId(result.fileId);
      // Don't set isRecording here, wait for onVoiceStart event
    } catch (error) {
      addLog('error', `Start error: ${error}`);
      setIsLoading(false);
    }
  };

  const stopRecognizing = async () => {
    try {
      addLog(
        'info',
        `Stopping - Current state: isRecording=${isRecording}, isLoading=${isLoading}`
      );
      setIsLoading(true);
      addLog('info', 'Stopping speech recognition...');

      await GoogleCloudSpeechToText.stop();
      addLog('success', 'Stopped recording');
      // Don't set isRecording here, wait for onVoiceEnd event
    } catch (error) {
      addLog('error', `Stop error: ${error}`);
      setIsRecording(false); // Force reset on error
    } finally {
      setIsLoading(false);
    }
  };

  const getAudioFile = async () => {
    if (!currentFileId) {
      Alert.alert(
        'No File ID',
        'No recording file ID available. Please record first with "Save speech to file" enabled.'
      );
      return;
    }

    try {
      setIsLoading(true);
      addLog('info', `Getting audio file for ID: ${currentFileId}...`);

      const result = await GoogleCloudSpeechToText.getAudioFile(currentFileId, {
        sampleRate: 44100,
        bitrate: 128000,
        channel: 2,
      });

      setAudioFilePath(result.path);
      addLog(
        'success',
        `Audio file saved: ${result.path} (${result.size} bytes)`
      );
    } catch (error) {
      addLog('error', `Get audio file error: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  const clearTranscript = () => {
    setTranscript('');
    addLog('info', 'Transcript cleared');
  };

  const clearLogs = () => {
    setLogs([]);
  };

  const getLogColor = (type: LogEntry['type']) => {
    switch (type) {
      case 'error':
        return '#ff4444';
      case 'success':
        return '#00aa00';
      default:
        return '#666';
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.title}>Google Cloud Speech-to-Text</Text>
          <Text style={styles.subtitle}>React Native Demo</Text>
          <Text style={styles.legacyTest}>
            Legacy multiply test: 3 × 7 = {multiplyResult}
          </Text>
        </View>

        {/* API Configuration */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Configuration</Text>

          <View style={styles.inputContainer}>
            <Text style={styles.label}>Google Cloud API Key:</Text>
            <TextInput
              style={styles.textInput}
              value={apiKey}
              onChangeText={setApiKey}
              placeholder="Enter your API key"
              secureTextEntry={true}
              autoCapitalize="none"
            />
          </View>

          <View style={styles.inputContainer}>
            <Text style={styles.label}>Language Code:</Text>
            <TextInput
              style={styles.textInput}
              value={languageCode}
              onChangeText={setLanguageCode}
              placeholder="e.g., en-US, es-ES, fr-FR"
              autoCapitalize="none"
            />
          </View>

          <TouchableOpacity
            style={[styles.checkboxContainer]}
            onPress={() => setSpeechToFile(!speechToFile)}
          >
            <View
              style={[styles.checkbox, speechToFile && styles.checkboxChecked]}
            />
            <Text style={styles.checkboxLabel}>Save speech to file</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[
              styles.button,
              styles.initButton,
              isInitialized && styles.buttonDisabled,
            ]}
            onPress={initializeSpeechRecognition}
            disabled={isInitialized}
          >
            <Text style={styles.buttonText}>
              {isInitialized
                ? 'Initialized ✓'
                : 'Initialize Speech Recognition'}
            </Text>
          </TouchableOpacity>
        </View>

        {/* Recording Controls */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Recording Controls</Text>

          <View style={styles.controlsContainer}>
            <TouchableOpacity
              style={[
                styles.button,
                styles.startButton,
                (!isInitialized || isRecording) && styles.buttonDisabled,
              ]}
              onPress={startRecognizing}
              disabled={!isInitialized || isRecording || isLoading}
            >
              {isLoading && !isRecording ? (
                <ActivityIndicator color="#fff" size="small" />
              ) : (
                <Text style={styles.buttonText}>Start Recording</Text>
              )}
            </TouchableOpacity>

            <TouchableOpacity
              style={[
                styles.button,
                styles.stopButton,
                !isRecording && styles.buttonDisabled,
              ]}
              onPress={stopRecognizing}
              disabled={!isRecording || isLoading}
            >
              {isLoading && isRecording ? (
                <ActivityIndicator color="#fff" size="small" />
              ) : (
                <Text style={styles.buttonText}>Stop Recording</Text>
              )}
            </TouchableOpacity>
          </View>

          {/* Get Audio File Button */}
          <TouchableOpacity
            style={[
              styles.button,
              styles.audioFileButton,
              (!currentFileId || !speechToFile) && styles.buttonDisabled,
            ]}
            onPress={getAudioFile}
            disabled={!currentFileId || !speechToFile || isLoading}
          >
            <Text style={styles.buttonText}>Get Audio File</Text>
          </TouchableOpacity>

          {/* Audio File Path Display */}
          {audioFilePath && (
            <View style={styles.audioFilePathContainer}>
              <Text style={styles.audioFilePathLabel}>Audio File:</Text>
              <Text style={styles.audioFilePathText}>{audioFilePath}</Text>
            </View>
          )}

          {/* Audio Level Indicator */}
          {isRecording && (
            <View style={styles.audioLevelContainer}>
              <Text style={styles.audioLevelLabel}>Audio Level:</Text>
              <View style={styles.audioLevelBar}>
                <View
                  style={[
                    styles.audioLevelFill,
                    { width: `${Math.min((audioLevel / 1000) * 100, 100)}%` },
                  ]}
                />
              </View>
            </View>
          )}

          {/* Status Indicator */}
          <View style={styles.statusContainer}>
            <View
              style={[
                styles.statusIndicator,
                { backgroundColor: isRecording ? '#00aa00' : '#ccc' },
              ]}
            />
            <Text style={styles.statusText}>
              {isRecording ? 'Recording...' : 'Not Recording'}
            </Text>
          </View>
        </View>

        {/* Transcript */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Transcript</Text>
            <TouchableOpacity
              onPress={clearTranscript}
              style={styles.clearButton}
            >
              <Text style={styles.clearButtonText}>Clear</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.transcriptContainer}>
            <Text style={styles.transcript}>
              {transcript || 'Transcript will appear here...'}
            </Text>
          </View>
        </View>

        {/* Logs */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Event Logs</Text>
            <TouchableOpacity onPress={clearLogs} style={styles.clearButton}>
              <Text style={styles.clearButtonText}>Clear</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.logsContainer}>
            <ScrollView
              style={styles.logsScrollView}
              showsVerticalScrollIndicator={true}
              nestedScrollEnabled={true}
            >
              {logs.length === 0 ? (
                <Text style={styles.noLogs}>No logs yet...</Text>
              ) : (
                logs.map((log, index) => (
                  <View key={index} style={styles.logEntry}>
                    <Text style={styles.logTimestamp}>{log.timestamp}</Text>
                    <Text
                      style={[
                        styles.logMessage,
                        { color: getLogColor(log.type) },
                      ]}
                    >
                      {log.message}
                    </Text>
                  </View>
                ))
              )}
            </ScrollView>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollView: {
    flex: 1,
  },
  header: {
    alignItems: 'center',
    paddingVertical: 20,
    backgroundColor: '#4285f4',
    marginBottom: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 5,
  },
  subtitle: {
    fontSize: 16,
    color: '#e3f2fd',
    marginBottom: 10,
  },
  legacyTest: {
    fontSize: 12,
    color: '#bbdefb',
    fontStyle: 'italic',
  },
  section: {
    backgroundColor: '#fff',
    marginHorizontal: 16,
    marginBottom: 16,
    borderRadius: 8,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
    color: '#333',
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  inputContainer: {
    marginBottom: 12,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 6,
    color: '#666',
  },
  textInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 6,
    padding: 12,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
  },
  checkboxContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderWidth: 2,
    borderColor: '#4285f4',
    borderRadius: 3,
    marginRight: 10,
  },
  checkboxChecked: {
    backgroundColor: '#4285f4',
  },
  checkboxLabel: {
    fontSize: 16,
    color: '#333',
  },
  controlsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  button: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 4,
  },
  initButton: {
    backgroundColor: '#4285f4',
  },
  startButton: {
    backgroundColor: '#00aa00',
  },
  stopButton: {
    backgroundColor: '#ff4444',
  },
  audioFileButton: {
    backgroundColor: '#ff9800',
    marginTop: 8,
  },
  buttonDisabled: {
    backgroundColor: '#ccc',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  audioFilePathContainer: {
    marginTop: 12,
    padding: 12,
    backgroundColor: '#f0f0f0',
    borderRadius: 6,
  },
  audioFilePathLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#666',
    marginBottom: 4,
  },
  audioFilePathText: {
    fontSize: 12,
    color: '#333',
    fontFamily: 'monospace',
  },
  audioLevelContainer: {
    marginBottom: 12,
  },
  audioLevelLabel: {
    fontSize: 14,
    color: '#666',
    marginBottom: 6,
  },
  audioLevelBar: {
    height: 8,
    backgroundColor: '#eee',
    borderRadius: 4,
    overflow: 'hidden',
  },
  audioLevelFill: {
    height: '100%',
    backgroundColor: '#4285f4',
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusIndicator: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginRight: 8,
  },
  statusText: {
    fontSize: 16,
    color: '#666',
  },
  transcriptContainer: {
    backgroundColor: '#f9f9f9',
    borderRadius: 6,
    padding: 16,
    minHeight: 100,
  },
  transcript: {
    fontSize: 16,
    color: '#333',
    lineHeight: 24,
  },
  logsContainer: {
    backgroundColor: '#f9f9f9',
    borderRadius: 6,
    padding: 12,
    height: 300, // Increased height
  },
  logsScrollView: {
    flex: 1,
  },
  noLogs: {
    fontSize: 14,
    color: '#999',
    fontStyle: 'italic',
    textAlign: 'center',
    paddingVertical: 20,
  },
  logEntry: {
    flexDirection: 'row',
    paddingVertical: 6,
    paddingHorizontal: 4,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    marginBottom: 2,
  },
  logTimestamp: {
    fontSize: 12,
    color: '#999',
    width: 80,
    marginRight: 10,
    fontFamily: 'monospace',
  },
  logMessage: {
    fontSize: 13,
    flex: 1,
    lineHeight: 18,
  },
  clearButton: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    backgroundColor: '#ff4444',
    borderRadius: 4,
  },
  clearButtonText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
});
