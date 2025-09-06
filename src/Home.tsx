// src/Home.tsx
import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {
  View,
  Text,
  StyleSheet,
  ActivityIndicator,
  StatusBar,
  requireNativeComponent,
  DeviceEventEmitter,
  NativeSyntheticEvent,
} from 'react-native';
import axios from 'axios';

type Row = {id: string; title: string; subtitle: string; image: string};

type ItemPressEvent = NativeSyntheticEvent<{id: string}>;

type NativeHaiyveeListProps = {
  data: Row[];
  refreshing: boolean;
  onRefresh: () => void;
  onEndReached: () => void;
  onEndReachedThreshold?: number;
  contentPaddingTop?: number;
  contentPaddingBottom?: number;
  onItemPress?: (e: ItemPressEvent) => void;
  style?: any;
};

const HaiyveeList = requireNativeComponent<NativeHaiyveeListProps>('HaiyveeList');

const API_URL = 'https://api.loverume.com/api/posts/';
const LIMIT = 10;

type ApiPost = {
  _id?: string;
  id?: string;
  text?: string;
  owner?: {name?: string; avatar?: string};
  urls?: {url?: string; thumbnailUrl?: string}[];
};
type ApiResponse = {
  success?: boolean;
  posts?: ApiPost[];
  pagination?: {total?: number; currentPage?: number; totalPages?: number};
};

const Home: React.FC = () => {
  const [rows, setRows] = useState<Row[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingInitial, setLoadingInitial] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [offset, setOffset] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const pendingRef = useRef(false);

  const mapToRows = useCallback((posts: ApiPost[], baseOffset: number): Row[] => {
    return posts.map((p, idx) => {
      const image = p?.urls?.[0]?.thumbnailUrl || p?.urls?.[0]?.url || '';
      return {
        id: p._id || p.id || image || `${baseOffset}-${idx}`,
        title: p?.owner?.name || 'Post',
        subtitle: p?.text || '',
        image,
      };
    });
  }, []);

  const load = useCallback(
    async (reset = false) => {
      if (pendingRef.current) return;
      pendingRef.current = true;

      try {
        setError(null);
        if (reset) {
          setRefreshing(true);
          setLoadingInitial(true);
        } else {
          if (!hasMore || loadingMore) return;
          setLoadingMore(true);
        }

        const nextOffset = reset ? 0 : offset;
        const res = await axios.get<ApiResponse>(API_URL, {params: {offset: nextOffset, limit: LIMIT}});
        const posts = Array.isArray(res.data?.posts) ? res.data.posts! : [];
        console.log("posts", posts);
        
        const mapped = mapToRows(posts, nextOffset);

        setRows(prev => (reset ? mapped : [...prev, ...mapped]));

        const total = res.data?.pagination?.total;
        if (typeof total === 'number') {
          setHasMore(nextOffset + posts.length < total);
        } else {
          setHasMore(posts.length === LIMIT);
        }
        setOffset(reset ? LIMIT : nextOffset + posts.length);
      } catch (e: any) {
        setError(e?.response?.data?.message || e?.message || 'Failed to load feed');
      } finally {
        setRefreshing(false);
        setLoadingInitial(false);
        setLoadingMore(false);
        pendingRef.current = false;
      }
    },
    [offset, hasMore, loadingMore, mapToRows],
  );

  useEffect(() => {
    load(true);
  }, [load]);

  // Optional: if you emit 'haiyvee_refresh' from MainActivity, it will reload:
  useEffect(() => {
    const sub = DeviceEventEmitter.addListener('haiyvee_refresh', () => {
      load(true);
    });
    return () => sub.remove();
  }, [load]);

  const overlay = useMemo(() => {
    if (loadingInitial) {
      return (
        <View style={styles.centerOverlay} pointerEvents="none">
          <ActivityIndicator color="#fff" />
          <Text style={styles.hint}>Loading feed…</Text>
        </View>
      );
    }
    if (!loadingInitial && rows.length === 0) {
      return (
        <View style={styles.centerOverlay} pointerEvents="none">
          <Text style={styles.hint}>{error ? error : 'No posts yet.'}</Text>
        </View>
      );
    }
    return null;
  }, [loadingInitial, rows.length, error]);

  return (
    <View style={styles.screen}>
      <StatusBar barStyle="light-content" backgroundColor="#000" />
      <HaiyveeList
        style={{flex: 1}}
        data={rows}
        refreshing={refreshing}
        onRefresh={() => load(true)}
        onEndReached={() => {
          if (!loadingMore && hasMore) load(false);
        }}
        onEndReachedThreshold={0.7}
        contentPaddingTop={12}
        contentPaddingBottom={120}
        onItemPress={(e: ItemPressEvent) => {
          console.log('Pressed id:', e?.nativeEvent?.id);
        }}
      />
      {overlay}
      {loadingMore && (
        <View style={styles.footerLoader}>
          <ActivityIndicator color="#fff" />
        </View>
      )}
    </View>
  );
};

export default Home;

const styles = StyleSheet.create({
  screen: {flex: 1, backgroundColor: '#000'},
  centerOverlay: {position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, alignItems: 'center', justifyContent: 'center'},
  hint: {color: '#aaa', marginTop: 8, textAlign: 'center', paddingHorizontal: 16},
  footerLoader: {position: 'absolute', bottom: 16, left: 0, right: 0, alignItems: 'center'},
});
