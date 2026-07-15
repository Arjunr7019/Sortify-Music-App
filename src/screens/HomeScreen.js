import React, { useEffect, useState, useCallback } from "react";
import {
  View,
  Text,
  Image,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppBackground from "../components/AppBackground";
import GlassView from "../components/GlassView";
import SectionHeader from "../components/SectionHeader";
import SongCard from "../components/SongCard";
import PlaylistCard from "../components/PlaylistCard";
import colors from "../theme/colors";
import { useLibrary } from "../context/LibraryContext";
import { useOnboarding } from "../context/OnboardingContext";
import { usePlayer } from "../context/PlayerContext";
import { getTrendingForLanguage, normalizeSong } from "../api/musicApi";

export default function HomeScreen({ navigation }) {
  const insets = useSafeAreaInsets();
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
    <AppBackground>
      <FlatList
        data={languages}
        keyExtractor={(l) => l}
        contentContainerStyle={{ paddingBottom: 140 + insets.bottom }}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={colors.coral}
          />
        }
        ListHeaderComponent={
          <>
            <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
              <Image source={require("../../assets/logo.png")} style={styles.logo} resizeMode="contain" />
              <Text style={styles.greeting}>Good listening today</Text>
            </View>

            {recentlyPlayed.length > 0 && (
              <>
                <SectionHeader title="Last session" subtitle="Pick up where you left off" />
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
              title="Your playlists"
              subtitle={`${playlists.length} playlist${playlists.length === 1 ? "" : "s"}`}
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
              <GlassView radius={16} style={styles.emptyPlaylists}>
                <View style={styles.emptyPlaylistsInner}>
                  <Ionicons name="albums-outline" size={20} color={colors.textFaint} />
                  <Text style={styles.emptyText}>
                    Tap the + icon on any song to start your first playlist.
                  </Text>
                </View>
              </GlassView>
            )}
          </>
        }
        renderItem={({ item: lang }) => {
          const songs = trendingByLanguage[lang] || [];
          const isLoading = loadingLangs[lang];
          return (
            <View>
              <SectionHeader title={`Trending in ${lang}`} subtitle="Updated for you" />
              {isLoading && songs.length === 0 ? (
                <ActivityIndicator color={colors.coral} style={{ marginTop: 20 }} />
              ) : songs.length === 0 ? (
                <Text style={styles.emptyText}>Nothing found right now.</Text>
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
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 20,
    marginBottom: 6,
  },
  logo: { width: 34, height: 34, marginRight: 10 },
  greeting: { color: colors.text, fontSize: 17, fontWeight: "700" },
  emptyPlaylists: {
    marginHorizontal: 20,
  },
  emptyPlaylistsInner: {
    padding: 16,
    flexDirection: "row",
    alignItems: "center",
  },
  emptyText: { color: colors.textFaint, fontSize: 12, marginLeft: 10, flex: 1, paddingHorizontal: 20 },
});
