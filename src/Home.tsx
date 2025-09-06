import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, StatusBar, Image, requireNativeComponent } from 'react-native';
import axios from 'axios';

type Row = { id: string; title: string; subtitle: string; image: string };
type NativeHaiyveeListProps = {
  data: Row[];
  refreshing: boolean;
  onRefresh: () => void;
  onEndReached: () => void;
  onEndReachedThreshold?: number;
  contentPaddingTop?: number;
  contentPaddingBottom?: number;
  onItemPress?: (e: { nativeEvent: { id: string } }) => void;
  style?: any;
};

const HaiyveeList = requireNativeComponent<NativeHaiyveeListProps>('HaiyveeList');

const API_URL = 'https://api.loverume.com/api/posts/';
const LIMIT = 10;

type ApiPost = {
  _id?: string;
  id?: string;
  text?: string;
  owner?: { name?: string; avatar?: string };
  urls?: { url?: string; thumbnailUrl?: string }[];
};
type ApiResponse = {
  success?: boolean;
  posts?: ApiPost[];
  pagination?: { total?: number; currentPage?: number; totalPages?: number };
};

const Home: React.FC = () => {
  const [rows, setRows] = useState<Row[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingInitial, setLoadingInitial] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [offset, setOffset] = useState(0);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const mapToRows = useCallback((posts: ApiPost[]): Row[] => {
    return posts.map((p, idx) => {
      const image = p?.urls?.[0]?.thumbnailUrl || p?.urls?.[0]?.url || '';
      return {
        id: p._id || p.id || `${Date.now()}-${idx}`,
        title: p.owner?.name || 'Post',
        subtitle: p.text || '',
        image,
      };
    });
  }, []);

  const load = useCallback(
    async (reset = false) => {
      try {
        setError(null);
        if (reset) {
          setRefreshing(true);
          setLoadingInitial(true);
        } else {
          if (!hasMore || loadingMore) return;
          setLoadingMore(true);
        }

        const res = await axios.get<ApiResponse>(API_URL, {
          params: { offset: reset ? 0 : offset, limit: LIMIT, page: reset ? 1 : page },
        });

        const posts = Array.isArray(res.data?.posts) ? res.data!.posts! : [];
        console.log("posts", posts);
        
        const mapped = mapToRows(posts);

        setRows(prev => (reset ? mapped : [...prev, ...mapped]));

        const current = res.data?.pagination?.currentPage ?? (reset ? 1 : page);
        const totalPages = res.data?.pagination?.totalPages ?? current;
        const more = (current < totalPages && posts.length > 0) || posts.length === LIMIT;

        setHasMore(more);
        setOffset(prev => (reset ? LIMIT : prev + posts.length));
        setPage(prev => (reset ? 2 : prev + 1));
      } catch (e: any) {
        setError(e?.response?.data?.message || e?.message || 'Failed to load feed.');
        console.log("e?.response?.data?.message", e?.response?.data?.message);
        
      } finally {
        setRefreshing(false);
        setLoadingInitial(false);
        setLoadingMore(false);
      }
    },
    [offset, page, hasMore, loadingMore, mapToRows]
  );

  useEffect(() => {
    load(true);
  }, []);

  const overlay = useMemo(() => {
    if (loadingInitial) {
      return (
        <View style={styles.centerOverlay}>
          <ActivityIndicator />
          <Text style={styles.hint}>Loading feed…</Text>
        </View>
      );
    }
    if (!loadingInitial && rows.length === 0) {
      return (
        <View style={styles.centerOverlay}>
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
        style={{ flex: 1 }}
        data={rows}
        refreshing={refreshing}
        onRefresh={() => load(true)}
        onEndReached={() => { if (!loadingMore && hasMore) load(false); }}
        onEndReachedThreshold={0.7}
        contentPaddingTop={12}
        contentPaddingBottom={120}
        onItemPress={(e: any) => {
          console.log('Pressed id:', e?.nativeEvent?.id);
        }}
      />

      {overlay}

      {loadingMore && (
        <View style={styles.footerLoader}>
          <ActivityIndicator />
        </View>
      )}
    </View>
  );
};

export default Home;

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#000' },
  centerOverlay: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, alignItems: 'center', justifyContent: 'center' },
  hint: { color: '#aaa', marginTop: 8, textAlign: 'center', paddingHorizontal: 16 },
  footerLoader: { position: 'absolute', bottom: 16, left: 0, right: 0, alignItems: 'center' },
});


