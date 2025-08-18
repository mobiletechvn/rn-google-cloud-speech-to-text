/**
 * Utility functions for the Speech-to-Text example app
 */

import { Alert, Platform, Linking } from 'react-native';
import CONFIG from './config';

export interface LogEntry {
  timestamp: string;
  type: 'info' | 'error' | 'success' | 'warning';
  message: string;
  details?: any;
}

/**
 * Format timestamp for display
 */
export const formatTimestamp = (date?: Date): string => {
  const now = date || new Date();
  return now.toLocaleTimeString('en-US', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
};

/**
 * Create a log entry with timestamp
 */
export const createLogEntry = (
  type: LogEntry['type'],
  message: string,
  details?: any
): LogEntry => ({
  timestamp: formatTimestamp(),
  type,
  message,
  details,
});

/**
 * Get color for log entry type
 */
export const getLogColor = (type: LogEntry['type']): string => {
  switch (type) {
    case 'error':
      return CONFIG.UI.THEME.ERROR_COLOR;
    case 'success':
      return CONFIG.UI.THEME.SUCCESS_COLOR;
    case 'warning':
      return CONFIG.UI.THEME.WARNING_COLOR;
    default:
      return CONFIG.UI.THEME.MUTED_TEXT_COLOR;
  }
};

/**
 * Validate Google Cloud API key format
 */
export const validateApiKey = (apiKey: string): boolean => {
  if (!apiKey || apiKey.trim().length === 0) {
    return false;
  }

  // Basic validation - Google API keys typically start with 'AIza'
  if (!apiKey.startsWith('AIza')) {
    return false;
  }

  // Check length (Google API keys are typically 39 characters)
  if (apiKey.length !== 39) {
    return false;
  }

  return true;
};

/**
 * Validate language code format
 */
export const validateLanguageCode = (languageCode: string): boolean => {
  if (!languageCode || languageCode.trim().length === 0) {
    return false;
  }

  // Check if it's in the supported languages list
  return CONFIG.SUPPORTED_LANGUAGES.some((lang) => lang.code === languageCode);
};

/**
 * Format file size for display
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';

  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * Format duration for display
 */
export const formatDuration = (seconds: number): string => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

/**
 * Calculate audio level percentage
 */
export const calculateAudioLevel = (
  rawLevel: number,
  maxLevel: number = 1000
): number => {
  return Math.min((rawLevel / maxLevel) * 100, 100);
};

/**
 * Show error alert with helpful actions
 */
export const showErrorAlert = (
  title: string,
  message: string,
  actions?: Array<{
    text: string;
    onPress: () => void;
  }>
) => {
  const alertActions = [
    { text: 'OK', style: 'default' as const },
    ...(actions || []),
  ];

  Alert.alert(title, message, alertActions);
};

/**
 * Show API key setup alert
 */
export const showApiKeySetupAlert = () => {
  Alert.alert(
    'API Key Required',
    'To use speech recognition, you need a Google Cloud Speech-to-Text API key.',
    [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Get API Key',
        onPress: () =>
          Linking.openURL('https://console.cloud.google.com/apis/credentials'),
      },
    ]
  );
};

/**
 * Show permission denied alert
 */
export const showPermissionAlert = () => {
  Alert.alert(
    'Microphone Permission Required',
    'This app needs microphone access to record audio for speech recognition.',
    [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Open Settings',
        onPress: () => {
          if (Platform.OS === 'ios') {
            Linking.openURL('app-settings:');
          } else {
            Linking.openSettings();
          }
        },
      },
    ]
  );
};

/**
 * Debounce function to limit rapid function calls
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): T => {
  let timeout: NodeJS.Timeout;

  return ((...args: any[]) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(null, args), wait);
  }) as T;
};

/**
 * Get language name from code
 */
export const getLanguageName = (languageCode: string): string => {
  const language = CONFIG.SUPPORTED_LANGUAGES.find(
    (lang) => lang.code === languageCode
  );
  return language ? language.name : languageCode;
};

/**
 * Check if device supports speech recognition features
 */
export const checkDeviceCapabilities = () => {
  const androidVersion =
    Platform.OS === 'android' ? parseInt(Platform.Version.toString(), 10) : 0;

  return {
    hasMicrophone: true, // Assume all devices have microphones
    supportsHighQualityAudio: Platform.OS === 'ios' || androidVersion >= 21,
    supportsBackgroundRecording: Platform.OS === 'ios',
    supportsNoiseCancellation: Platform.OS === 'ios' || androidVersion >= 28,
  };
};

/**
 * Generate unique file ID
 */
export const generateFileId = (): string => {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substring(2, 8);
  return `speech_${timestamp}_${random}`;
};

/**
 * Log debug information (only in development)
 */
export const debugLog = (message: string, data?: any) => {
  if (CONFIG.DEBUG.ENABLE_LOGS && __DEV__) {
    console.log(`[SpeechDemo] ${message}`, data || '');
  }
};

/**
 * Error boundary helper
 */
export const handleError = (error: any, context: string): LogEntry => {
  const errorMessage = error?.message || error?.toString() || 'Unknown error';
  const logEntry = createLogEntry(
    'error',
    `${context}: ${errorMessage}`,
    error
  );

  debugLog(`Error in ${context}`, error);

  return logEntry;
};

export default {
  formatTimestamp,
  createLogEntry,
  getLogColor,
  validateApiKey,
  validateLanguageCode,
  formatFileSize,
  formatDuration,
  calculateAudioLevel,
  showErrorAlert,
  showApiKeySetupAlert,
  showPermissionAlert,
  debounce,
  getLanguageName,
  checkDeviceCapabilities,
  generateFileId,
  debugLog,
  handleError,
};
