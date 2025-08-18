/**
 * Example App Configuration
 *
 * Configure your Google Cloud Speech-to-Text settings here.
 * This file is ignored by git to keep your API keys secure.
 */

export const CONFIG = {
  // Your Google Cloud Speech-to-Text API Key
  // Get one from: https://console.cloud.google.com/apis/credentials
  GOOGLE_CLOUD_API_KEY: 'YOUR_API_KEY_HERE',

  // Default language code for speech recognition
  // See: https://cloud.google.com/speech-to-text/docs/languages
  DEFAULT_LANGUAGE_CODE: 'en-US',

  // Default audio settings
  DEFAULT_SETTINGS: {
    speechToFile: false,
    enableVAD: true, // Voice Activity Detection
    enablePunctuation: true,
    enableWordTimeOffsets: false,
    enableSpeakerDiarization: false,
  },

  // Available language options for the demo
  SUPPORTED_LANGUAGES: [
    { code: 'en-US', name: 'English (US)' },
    { code: 'en-GB', name: 'English (UK)' },
    { code: 'es-ES', name: 'Spanish (Spain)' },
    { code: 'es-MX', name: 'Spanish (Mexico)' },
    { code: 'fr-FR', name: 'French (France)' },
    { code: 'de-DE', name: 'German (Germany)' },
    { code: 'it-IT', name: 'Italian (Italy)' },
    { code: 'pt-PT', name: 'Portuguese (Portugal)' },
    { code: 'pt-BR', name: 'Portuguese (Brazil)' },
    { code: 'ru-RU', name: 'Russian (Russia)' },
    { code: 'ja-JP', name: 'Japanese (Japan)' },
    { code: 'ko-KR', name: 'Korean (South Korea)' },
    { code: 'zh-CN', name: 'Chinese (Mandarin, Simplified)' },
    { code: 'zh-TW', name: 'Chinese (Traditional)' },
    { code: 'ar-SA', name: 'Arabic (Saudi Arabia)' },
    { code: 'hi-IN', name: 'Hindi (India)' },
    { code: 'th-TH', name: 'Thai (Thailand)' },
    { code: 'vi-VN', name: 'Vietnamese (Vietnam)' },
  ],

  // Audio quality presets
  AUDIO_PRESETS: {
    LOW_QUALITY: {
      sampleRate: 16000,
      bitrate: 64000,
      channel: 1, // MONO
    },
    MEDIUM_QUALITY: {
      sampleRate: 22050,
      bitrate: 128000,
      channel: 1, // MONO
    },
    HIGH_QUALITY: {
      sampleRate: 44100,
      bitrate: 192000,
      channel: 2, // STEREO
    },
  },

  // UI Configuration
  UI: {
    THEME: {
      PRIMARY_COLOR: '#4285f4',
      SUCCESS_COLOR: '#00aa00',
      ERROR_COLOR: '#ff4444',
      WARNING_COLOR: '#ff9800',
      BACKGROUND_COLOR: '#f5f5f5',
      CARD_COLOR: '#ffffff',
      TEXT_COLOR: '#333333',
      MUTED_TEXT_COLOR: '#666666',
    },
    MAX_LOG_ENTRIES: 50,
    AUDIO_LEVEL_UPDATE_INTERVAL: 100, // ms
  },

  // Development/Debug settings
  DEBUG: {
    ENABLE_LOGS: __DEV__,
    MOCK_API_RESPONSES: false,
    SIMULATE_NETWORK_DELAY: false,
    LOG_AUDIO_DATA: false,
  },
};

export default CONFIG;
