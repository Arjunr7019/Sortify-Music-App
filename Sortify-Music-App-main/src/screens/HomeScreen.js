import React, { useEffect, useState, useCallback, useMemo } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  ActivityIndicator,
  RefreshControl,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import AppScreen from "../components/AppScreen";
import Header from "../components/Header";
import SectionHeader from "../components/SectionHeader";
import SongCard from "../components/SongCard";
import PlaylistCard from "../components/PlaylistCard";
import { useAppTheme } from "../context/ThemeContext";
import { useLibrary } from "../context/LibraryContext";
import { useOnboarding } from "../context/OnboardingContext";
import { usePlayer } from "../context/PlayerContext";
import { getTrendingForLanguage, normalizeSong } from "../api/musicApi";

export default function HomeScreen({ navigation }) {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { recentlyPlayed, playlists } = useLibrary();
  const { languages } = useOnboarding();
  const { playSong } = usePlayer();

  const [trendingByLanguage, setTrendingByLanguage] = useState({});
  const [loadingLangs, setLoadingLangs] = useState({});
  const [refreshing, setRefreshing] = useState(false);

  const loadLanguage = useCallback(async (lang) => {
    setLoadingLangs((prev) => ({ ...prev, [lang]: true }));
    try {
      const results = await getTrendingForLanguage(lang, 15);
      const normalized = results.map(normalizeSong).filter((s) => s.audioUrl);
      setTrendingByLanguage((prev) => ({ ...prev, [lang]: normalized }));
    } catch (e) {
      setTrendingByLanguage((prev) => ({ ...prev, [lang]: [] }));
    } finally {
      setLoadingLangs((prev) => ({ ...prev, [lang]: false }));
    }
  }, []);

  useEffect(() => {
    languages.forEach((lang) => loadLanguage(lang));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [languages]);

  const onRefresh = async () => {
    setRefreshing(true);
    await Promise.all(languages.map((lang) => loadLanguage(lang)));
    setRefreshing(false);
  };

  return (
    <AppScreen>
      <FlatList
        data={languages}
        keyExtractor={(l) => l}
        contentContainerStyle={{ paddingBottom: 140 }}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={theme.accent} />
        }
        ListHeaderComponent={
          <>
            <Header />

            {recentlyPlayed.length > 0 && (
              <>
                <SectionHeader title="Last Session" />
                <FlatList
                  data={recentlyPlayed}
                  horizontal
                  keyExtractor={(item, idx) => `${item.id}_${idx}`}
                  showsHorizontalScrollIndicator={false}
                  contentContainerStyle={{ paddingHorizontal: 20 }}
                  renderItem={({ item }) => (
                    <SongCard song={item} onPress={() => playSong(item, recentlyPlayed)} />
                  )}
                />
              </>
            )}

            <SectionHeader
              title="Your Playlists"
              onPressSeeAll={() => navigation.navigate("LibraryTab", { screen: "Playlists" })}
            />
            {playlists.length > 0 ? (
              <FlatList
                data={playlists}
                horizontal
                keyExtractor={(item) => item.id}
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={{ paddingHorizontal: 20 }}
                renderItem={({ item }) => (
                  <PlaylistCard
                    playlist={item}
                    onPress={() =>
                      navigation.navigate("LibraryTab", {
                        screen: "PlaylistDetail",
                        params: { playlistId: item.id },
                      })
                    }
                  />
                )}
              />
            ) : (
              <View style={styles.emptyPlaylists}>
                <Ionicons name="albums-outline" size={20} color={theme.textFaint} />
                <Text style={styles.emptyText}>
                  Tap the + icon on any song to start your first playlist.
                </Text>
              </View>
            )}
          </>
        }
        renderItem={({ item: lang }) => {
          const songs = trendingByLanguage[lang] || [];
          const isLoading = loadingLangs[lang];
          return (
            <View>
              <SectionHeader title={`Trending Songs`} subtitle={lang} />
              {isLoading && songs.length === 0 ? (
                <ActivityIndicator color={theme.accent} style={{ marginTop: 20 }} />
              ) : songs.length === 0 ? (
                <Text style={styles.emptyInline}>Nothing found right now.</Text>
              ) : (
                <FlatList
                  data={songs}
                  horizontal
                  keyExtractor={(item, idx) => `${lang}_${item.id}_${idx}`}
                  showsHorizontalScrollIndicator={false}
                  contentContainerStyle={{ paddingHorizontal: 20 }}
                  renderItem={({ item }) => (
                    <SongCard song={item} onPress={() => playSong(item, songs)} />
                  )}
                />
              )}
            </View>
          );
        }}
      />
    </AppScreen>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    emptyPlaylists: {
      marginHorizontal: 20,
      padding: 16,
      borderRadius: 14,
      backgroundColor: theme.surface,
      flexDirection: "row",
      alignItems: "center",
    },
    emptyText: { color: theme.textFaint, fontSize: 12, marginLeft: 10, flex: 1 },
    emptyInline: { color: theme.textFaint, fontSize: 12, marginLeft: 20, marginTop: 4 },
  });
