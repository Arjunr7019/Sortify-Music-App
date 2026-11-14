import React, { useState, useEffect, useRef, useCallback, useMemo } from "react";
import {
  View,
  Text,
  TextInput,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  Image,
  ActivityIndicator,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import AppScreen from "../components/AppScreen";
import Header from "../components/Header";
import SongListRow from "../components/SongListRow";
import { useAppTheme } from "../context/ThemeContext";
import { usePlayer } from "../context/PlayerContext";
import {
  searchSongs,
  searchAlbums,
  searchArtists,
  searchPlaylists,
  getArtistSongs,
  normalizeSong,
} from "../api/musicApi";

const TABS = [
  { key: "songs", label: "Songs" },
  { key: "albums", label: "Albums" },
  { key: "artists", label: "Artists" },
  { key: "playlists", label: "Playlists" },
];

const RESULT_LIMIT = 20;

const emptyTabState = { items: [], loading: false, loadedForQuery: null };

export default function SearchScreen() {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { playSong } = usePlayer();

  const [query, setQuery] = useState("");
  const [activeTab, setActiveTab] = useState("songs");
  const [tabData, setTabData] = useState({
    songs: emptyTabState,
    albums: emptyTabState,
    artists: emptyTabState,
    playlists: emptyTabState,
  });
  const debounceRef = useRef(null);

  const fetchTab = useCallback(async (tab, q) => {
    setTabData((prev) => ({ ...prev, [tab]: { ...prev[tab], loading: true } }));
    try {
      let items = [];
      if (tab === "songs") {
        const data = await searchSongs(q, 0, RESULT_LIMIT);
        items = (data?.results || []).map(normalizeSong);
      } else if (tab === "albums") {
        const data = await searchAlbums(q, RESULT_LIMIT);
        items = data?.results || [];
      } else if (tab === "artists") {
        const data = await searchArtists(q, RESULT_LIMIT);
        items = data?.results || [];
      } else if (tab === "playlists") {
        const data = await searchPlaylists(q, RESULT_LIMIT);
        items = data?.results || [];
      }
      setTabData((prev) => ({ ...prev, [tab]: { items, loading: false, loadedForQuery: q } }));
    } catch (e) {
      setTabData((prev) => ({ ...prev, [tab]: { items: [], loading: false, loadedForQuery: q } }));
    }
  }, []);

  // New query -> reset every tab's cache, then fetch only the active one.
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    const q = query.trim();
    if (!q) {
      setTabData({ songs: emptyTabState, albums: emptyTabState, artists: emptyTabState, playlists: emptyTabState });
      return;
    }
    debounceRef.current = setTimeout(() => {
      setTabData({ songs: emptyTabState, albums: emptyTabState, artists: emptyTabState, playlists: emptyTabState });
      fetchTab(activeTab, q);
    }, 400);
    return () => clearTimeout(debounceRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  // Switching tabs -> fetch that tab lazily if it isn't cached for this query yet.
  const onPressTab = (tab) => {
    setActiveTab(tab);
    const q = query.trim();
    if (!q) return;
    if (tabData[tab].loadedForQuery !== q && !tabData[tab].loading) {
      fetchTab(tab, q);
    }
  };

  const onPressArtist = async (artist) => {
    try {
      const data = await getArtistSongs(artist.id);
      const normalized = (data?.songs || []).map(normalizeSong).filter((s) => s.audioUrl);
      if (normalized.length) playSong(normalized[0], normalized);
    } catch (e) {
      // ignore
    }
  };

  const onPressAlbum = async (album) => {
    try {
      const data = await searchSongs(album.name, 0, RESULT_LIMIT);
      const normalized = (data?.results || []).map(normalizeSong).filter((s) => s.audioUrl);
      if (normalized.length) playSong(normalized[0], normalized);
    } catch (e) {
      // ignore
    }
  };

  const onPressPlaylist = async (playlist) => {
    try {
      const data = await searchSongs(playlist.name, 0, RESULT_LIMIT);
      const normalized = (data?.results || []).map(normalizeSong).filter((s) => s.audioUrl);
      if (normalized.length) playSong(normalized[0], normalized);
    } catch (e) {
      // ignore
    }
  };

  const hasQuery = query.trim().length > 0;
  const current = tabData[activeTab];

  return (
    <AppScreen>
      <Header />

      <View style={styles.searchWrap}>
        <Ionicons name="search" size={18} color={theme.textFaint} />
        <TextInput
          style={styles.input}
          placeholder="Songs, Albums or Artists"
          placeholderTextColor={theme.textFaint}
          value={query}
          onChangeText={setQuery}
          returnKeyType="search"
        />
        {query.length > 0 ? (
          <TouchableOpacity onPress={() => setQuery("")} hitSlop={8}>
            <Ionicons name="close-circle" size={18} color={theme.textFaint} />
          </TouchableOpacity>
        ) : null}
      </View>

      <View style={styles.tabsRow}>
        {TABS.map((tab) => {
          const active = activeTab === tab.key;
          return (
            <TouchableOpacity
              key={tab.key}
              style={[styles.tabChip, active && { backgroundColor: theme.accent }]}
              onPress={() => onPressTab(tab.key)}
              activeOpacity={0.75}
            >
              <Text style={[styles.tabText, active && { color: theme.accentOn, fontWeight: "700" }]}>
                {tab.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {!hasQuery ? (
        <View style={styles.placeholderWrap}>
          <Text style={styles.placeholderText}>result area</Text>
        </View>
      ) : current.loading && current.items.length === 0 ? (
        <ActivityIndicator color={theme.accent} style={{ marginTop: 40 }} />
      ) : current.items.length === 0 ? (
        <View style={styles.placeholderWrap}>
          <Ionicons name="sad-outline" size={36} color={theme.textFaint} />
          <Text style={styles.placeholderText}>No {activeTab} for "{query}"</Text>
        </View>
      ) : activeTab === "songs" ? (
        <FlatList
          key="list-songs"
          data={current.items}
          keyExtractor={(item, idx) => `song_${item.id}_${idx}`}
          contentContainerStyle={{ paddingBottom: 160, paddingTop: 8 }}
          renderItem={({ item }) =>
            item.audioUrl ? (
              <SongListRow
                song={item}
                onPress={() => playSong(item, current.items.filter((s) => s.audioUrl))}
              />
            ) : null
          }
        />
      ) : activeTab === "artists" ? (
        <FlatList
          key="list-artists"
          data={current.items}
          keyExtractor={(item, idx) => `artist_${item.id}_${idx}`}
          numColumns={3}
          columnWrapperStyle={{ paddingHorizontal: 16, justifyContent: "space-between" }}
          contentContainerStyle={{ paddingBottom: 160, paddingTop: 12 }}
          renderItem={({ item }) => (
            <TouchableOpacity style={styles.artistCard} onPress={() => onPressArtist(item)} activeOpacity={0.75}>
              {item.image?.[item.image.length - 1]?.url ? (
                <Image source={{ uri: item.image[item.image.length - 1].url }} style={styles.artistImg} />
              ) : (
                <View style={[styles.artistImg, { backgroundColor: theme.placeholder }]} />
              )}
              <Text style={styles.artistName} numberOfLines={1}>
                {item.name}
              </Text>
            </TouchableOpacity>
          )}
        />
      ) : activeTab === "albums" ? (
        <FlatList
          key="list-albums"
          data={current.items}
          keyExtractor={(item, idx) => `album_${item.id}_${idx}`}
          numColumns={2}
          columnWrapperStyle={{ paddingHorizontal: 16, justifyContent: "space-between" }}
          contentContainerStyle={{ paddingBottom: 160, paddingTop: 12 }}
          renderItem={({ item }) => (
            <TouchableOpacity style={styles.gridCard} onPress={() => onPressAlbum(item)} activeOpacity={0.8}>
              {item.image?.[item.image.length - 1]?.url ? (
                <Image source={{ uri: item.image[item.image.length - 1].url }} style={styles.gridImg} />
              ) : (
                <View style={[styles.gridImg, { backgroundColor: theme.placeholder }]} />
              )}
              <Text style={styles.gridName} numberOfLines={1}>
                {item.name}
              </Text>
              <Text style={styles.gridDetails} numberOfLines={1}>
                {item.artists?.primary?.[0]?.name || item.year || "Album"}
              </Text>
            </TouchableOpacity>
          )}
        />
      ) : (
        <FlatList
          key="list-playlists"
          data={current.items}
          keyExtractor={(item, idx) => `playlist_${item.id}_${idx}`}
          numColumns={2}
          columnWrapperStyle={{ paddingHorizontal: 16, justifyContent: "space-between" }}
          contentContainerStyle={{ paddingBottom: 160, paddingTop: 12 }}
          renderItem={({ item }) => (
            <TouchableOpacity style={styles.gridCard} onPress={() => onPressPlaylist(item)} activeOpacity={0.8}>
              {item.image?.[item.image.length - 1]?.url ? (
                <Image source={{ uri: item.image[item.image.length - 1].url }} style={styles.gridImg} />
              ) : (
                <View style={[styles.gridImg, { backgroundColor: theme.placeholder }]} />
              )}
              <Text style={styles.gridName} numberOfLines={1}>
                {item.name}
              </Text>
              <Text style={styles.gridDetails} numberOfLines={1}>
                {item.songCount ? `${item.songCount} songs` : "Playlist"}
              </Text>
            </TouchableOpacity>
          )}
        />
      )}
    </AppScreen>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    searchWrap: {
      flexDirection: "row",
      alignItems: "center",
      marginHorizontal: 20,
      marginBottom: 12,
      backgroundColor: theme.surface,
      borderRadius: 12,
      paddingHorizontal: 14,
      paddingVertical: 12,
    },
    input: { flex: 1, color: theme.text, fontSize: 14.5, marginLeft: 10 },
    tabsRow: {
      flexDirection: "row",
      paddingHorizontal: 20,
      gap: 8,
      marginBottom: 8,
    },
    tabChip: {
      paddingHorizontal: 14,
      paddingVertical: 8,
      borderRadius: 20,
      backgroundColor: theme.surface,
    },
    tabText: { color: theme.textSecondary, fontSize: 12.5, fontWeight: "600" },
    placeholderWrap: { alignItems: "center", marginTop: 70, paddingHorizontal: 40 },
    placeholderText: {
      color: theme.textFaint,
      fontSize: 13,
      marginTop: 14,
      textAlign: "center",
      lineHeight: 19,
    },
    artistCard: { width: "31%", alignItems: "center", marginBottom: 22 },
    artistImg: { width: 84, height: 84, borderRadius: 42 },
    artistName: { color: theme.text, fontSize: 12.5, fontWeight: "700", marginTop: 8, textAlign: "center" },
    gridCard: { width: "48%", marginBottom: 20 },
    gridImg: { width: "100%", height: 150, borderRadius: 14 },
    gridName: { color: theme.text, fontSize: 13, fontWeight: "700", marginTop: 8 },
    gridDetails: { color: theme.textFaint, fontSize: 11, marginTop: 2 },
  });
