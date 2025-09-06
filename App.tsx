// App.tsx
import React from 'react';
import {StatusBar, useColorScheme} from 'react-native';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import {NavigationContainer, DefaultTheme, DarkTheme} from '@react-navigation/native';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';

import Home from './src/Home';
import Post from './src/Post';

export type BottomTabParamList = {
  Home: undefined;
  Post: undefined;
};

const Tab = createBottomTabNavigator<BottomTabParamList>();

const App: React.FC = () => {
  const isDark = useColorScheme() === 'dark';

  return (
    <SafeAreaProvider>
      <StatusBar barStyle={isDark ? 'light-content' : 'dark-content'} />
      <NavigationContainer theme={isDark ? DarkTheme : DefaultTheme}>
        <Tab.Navigator
          screenOptions={{
            headerShown: false,
            tabBarStyle: {
              backgroundColor: isDark ? '#111' : '#fff',
              borderTopWidth: 0.5,
              borderTopColor: isDark ? '#222' : '#ddd',
              height: 58,
              paddingBottom: 6,
              paddingTop: 6,
            },
            tabBarActiveTintColor: isDark ? '#fff' : '#111',
            tabBarInactiveTintColor: '#888',
          }}>
          <Tab.Screen name="Home" component={Home} options={{tabBarLabel: 'Home'}} />
          <Tab.Screen name="Post" component={Post} options={{tabBarLabel: 'Post'}} />
        </Tab.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
};

export default App;
