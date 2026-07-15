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
import { globalSearch, getArtistSongs, searchSongs, normalizeSong } from "../api/musicApi";

export default function SearchScreen() {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { playSong } = usePlayer();
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [songs, setSongs] = useState([]);
  const [albums, setAlbums] = useState([]);
  const [artists, setArtists] = useState([]);
  const debounceRef = useRef(null);

  const runSearch = useCallback(async (q) => {
    if (!q.trim()) {
      setSongs([]);
      setAlbums([]);
      setArtists([]);
      return;
    }
    setLoading(true);
    try {
      const data = await globalSearch(q);
      setSongs((data?.songs?.results || []).map(normalizeSong));
      setAlbums(data?.albums?.results || []);
      setArtists(data?.artists?.results || []);
    } catch (e) {
      setSongs([]);
      setAlbums([]);
      setArtists([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => runSearch(query), 400);
    return () => clearTimeout(debounceRef.current);
  }, [query, runSearch]);

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
      const data = await searchSongs(album.title || album.name);
      const normalized = (data?.results || []).map(normalizeSong).filter((s) => s.audioUrl);
      if (normalized.length) playSong(normalized[0], normalized);
    } catch (e) {
      // ignore
    }
  };

  const hasQuery = query.trim().length > 0;
  const hasResults = songs.length || albums.length || artists.length;

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
          onSubmitEditing={() => runSearch(query)}
        />
        {loading ? (
          <ActivityIndicator size="small" color={theme.accent} />
        ) : query.length > 0 ? (
          <TouchableOpacity onPress={() => setQuery("")} hitSlop={8}>
            <Ionicons name="close-circle" size={18} color={theme.textFaint} />
          </TouchableOpacity>
        ) : null}
      </View>

      {!hasQuery ? (
        <View style={styles.placeholderWrap}>
          <Text style={styles.placeholderText}>result area</Text>
        </View>
      ) : !loading && !hasResults ? (
        <View style={styles.placeholderWrap}>
          <Ionicons name="sad-outline" size={36} color={theme.textFaint} />
          <Text style={styles.placeholderText}>No results for "{query}"</Text>
        </View>
      ) : (
        <FlatList
          data={songs}
          keyExtractor={(item, idx) => `song_${item.id}_${idx}`}
          contentContainerStyle={{ paddingBottom: 160, paddingTop: 8 }}
          ListHeaderComponent={
            <>
              {artists.length > 0 && (
                <>
                  <Text style={styles.sectionLabel}>Artists</Text>
                  <FlatList
                    data={artists}
                    horizontal
                    keyExtractor={(item, idx) => `artist_${item.id}_${idx}`}
                    showsHorizontalScrollIndicator={false}
                    contentContainerStyle={{ paddingHorizontal: 20 }}
                    renderItem={({ item }) => (
                      <TouchableOpacity style={styles.artistCard} onPress={() => onPressArtist(item)}>
                        <Image
                          source={{ uri: item.image?.[item.image.length - 1]?.url }}
                          style={styles.artistImg}
                        />
                        <Text style={styles.chipName} numberOfLines={1}>
                          {item.title}
                        </Text>
                      </TouchableOpacity>
                    )}
                  />
                </>
              )}

              {albums.length > 0 && (
                <>
                  <Text style={styles.sectionLabel}>Albums</Text>
                  <FlatList
                    data={albums}
                    horizontal
                    keyExtractor={(item, idx) => `album_${item.id}_${idx}`}
                    showsHorizontalScrollIndicator={false}
                    contentContainerStyle={{ paddingHorizontal: 20 }}
                    renderItem={({ item }) => (
                      <TouchableOpacity style={styles.albumCard} onPress={() => onPressAlbum(item)}>
                        <Image
                          source={{ uri: item.image?.[item.image.length - 1]?.url }}
                          style={styles.albumImg}
                        />
                        <Text style={styles.chipName} numberOfLines={1}>
                          {item.title}
                        </Text>
                      </TouchableOpacity>
                    )}
                  />
                </>
              )}

              {songs.length > 0 && <Text style={styles.sectionLabel}>Songs</Text>}
            </>
          }
          renderItem={({ item }) =>
            item.audioUrl ? (
              <SongListRow song={item} onPress={() => playSong(item, songs.filter((s) => s.audioUrl))} />
            ) : null
          }
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
      marginBottom: 6,
      backgroundColor: theme.surface,
      borderRadius: 12,
      paddingHorizontal: 14,
      paddingVertical: 12,
    },
    input: { flex: 1, color: theme.text, fontSize: 14.5, marginLeft: 10 },
    placeholderWrap: { alignItems: "center", marginTop: 90, paddingHorizontal: 40 },
    placeholderText: {
      color: theme.textFaint,
      fontSize: 13,
      marginTop: 14,
      textAlign: "center",
      lineHeight: 19,
    },
    sectionLabel: {
      color: theme.text,
      fontSize: 15,
      fontWeight: "700",
      marginLeft: 20,
      marginTop: 18,
      marginBottom: 10,
    },
    artistCard: { width: 90, marginRight: 14, alignItems: "center" },
    artistImg: { width: 76, height: 76, borderRadius: 38, backgroundColor: theme.placeholder },
    albumCard: { width: 110, marginRight: 14 },
    albumImg: { width: 110, height: 110, borderRadius: 14, backgroundColor: theme.placeholder },
    chipName: { color: theme.textSecondary, fontSize: 11.5, marginTop: 6, textAlign: "center" },
  });
