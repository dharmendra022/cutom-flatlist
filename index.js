// index.js
import { AppRegistry, Text, TextInput } from 'react-native';
import App from './App';
import { name as appName } from './app.json';

// ✅ If you have the TSX/JSX cell component, import it:
import HaiyveeCell from './src/native-cells/HaiyveeCell';

// (Optional) Silence noisy logs during native testing
// import { LogBox } from 'react-native';
// LogBox.ignoreAllLogs();

// ✅ Disable font scaling globally (optional)
if (Text.defaultProps == null) Text.defaultProps = {};
if (TextInput.defaultProps == null) TextInput.defaultProps = {};
Text.defaultProps.allowFontScaling = false;
TextInput.defaultProps.allowFontScaling = false;

// ✅ Register the native cell BEFORE the main app
// The name must match what your Android native adapter expects (APP_NAME = "HaiyveeCell")
try {
  const keys = AppRegistry.getAppKeys?.() || [];
  if (!keys.includes('HaiyveeCell')) {
    AppRegistry.registerComponent('HaiyveeCell', () => HaiyveeCell);
  }
} catch (e) {
  console.warn('HaiyveeCell registration skipped:', e?.message);
}

// ✅ Register the main RN app
AppRegistry.registerComponent(appName, () => App);
