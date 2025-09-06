// src/Post.tsx
import React, {useState} from 'react';
import {View, TextInput, StyleSheet, Text, TouchableOpacity, Alert} from 'react-native';

const Post: React.FC = () => {
  const [text, setText] = useState('');

  const handlePublish = () => {
    if (!text.trim()) {
      Alert.alert('Empty', 'Type something to post!');
      return;
    }
    Alert.alert('Posted', text);
    setText('');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Create Post</Text>
      <TextInput
        style={styles.input}
        placeholder="What's on your mind?"
        placeholderTextColor="#888"
        multiline
        value={text}
        onChangeText={setText}
      />
      <TouchableOpacity style={styles.button} onPress={handlePublish}>
        <Text style={styles.buttonText}>Publish</Text>
      </TouchableOpacity>
    </View>
  );
};

export default Post;

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: '#000', padding: 16},
  title: {fontSize: 18, color: '#fff', marginBottom: 12},
  input: {
    minHeight: 140,
    color: '#fff',
    padding: 12,
    borderWidth: 1,
    borderColor: '#333',
    borderRadius: 8,
    textAlignVertical: 'top',
  },
  button: {
    backgroundColor: '#1e90ff',
    alignItems: 'center',
    justifyContent: 'center',
    height: 48,
    borderRadius: 8,
    marginTop: 12,
  },
  buttonText: {color: '#fff', fontSize: 16, fontWeight: '600'},
});
