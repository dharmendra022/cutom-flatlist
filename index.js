// index.js
import 'react-native-gesture-handler';
import { AppRegistry, Text, TextInput } from 'react-native';
import App from './App';
import { name as appName } from './app.json';
import 'react-native-gesture-handler';


// ✅ Register the native cell used by the list adapter
import HaiyveeCell from './src/native-cells/HaiyveeCell';

// optional: disable font scaling globally
if (Text.defaultProps == null) Text.defaultProps = {};
if (TextInput.defaultProps == null) TextInput.defaultProps = {};
Text.defaultProps.allowFontScaling = false;
TextInput.defaultProps.allowFontScaling = false;

// Register HaiyveeCell first (must match APP_NAME in Kotlin => "HaiyveeCell")
const keys = AppRegistry.getAppKeys?.() || [];
if (!keys.includes('HaiyveeCell')) {
  AppRegistry.registerComponent('HaiyveeCell', () => HaiyveeCell);
}

// Main app
AppRegistry.registerComponent(appName, () => App);
