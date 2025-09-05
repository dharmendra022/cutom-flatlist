import React from 'react';
import { View, Text, StyleSheet, Image } from 'react-native';

type Props = { id?: string; title?: string; subtitle?: string; image?: string };

export default function HaiyveeCell({ title = 'Post', subtitle = '', image = '' }: Props) {
  return (
    <View style={styles.card}>
      <Text style={styles.title} numberOfLines={1}>{title}</Text>
      {!!subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}
      {!!image && (
        <View style={styles.mediaWrapper}>
          <Image source={{ uri: image }} style={styles.media} resizeMode="cover" />
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#0E1013', borderRadius: 12, padding: 12, margin: 12 },
  title: { color: '#fff', fontWeight: '700', fontSize: 15 },
  subtitle: { color: '#e6e6e6', marginTop: 8, lineHeight: 20 },
  mediaWrapper: { marginTop: 10, borderRadius: 10, overflow: 'hidden' },
  media: { width: '100%', height: 260, backgroundColor: '#14171a' },
});
