import React from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppBackground from "../components/AppBackground";
import SongListRow from "../components/SongListRow";
import colors from "../theme/colors";
import { useLibrary } from "../context/LibraryContext";
import { usePlayer } from "../context/PlayerContext";
import { deleteSongFile } from "../utils/download";

export default function DownloadsScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const { downloads, removeDownload } = useLibrary();
  const { playSong } = usePlayer();

  const handlePlay = (song) => {
    // Prefer the offline file when available so downloads play without data.
    const playable = song.localUri ? { ...song, audioUrl: song.localUri } : song;
    const queue = downloads.map((s) => (s.localUri ? { ...s, audioUrl: s.localUri } : s));
    playSong(playable, queue);
  };

  const handleRemove = async (song) => {
    if (song.localUri) await deleteSongFile(song.localUri);
    removeDownload(song.id);
  };

  return (
    <AppBackground>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} hitSlop={10}>
          <Ionicons name="chevron-back" size={24} color={colors.text} />
        </TouchableOpacity>
        <Text style={styles.title}>Downloads</Text>
        <View style={{ width: 24 }} />
      </View>

      <FlatList
        data={downloads}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ paddingBottom: 160 + insets.bottom, paddingTop: 10 }}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="download-outline" size={40} color={colors.textFaint} />
            <Text style={styles.emptyText}>
              Tap the download icon on the player to save songs for offline listening.
            </Text>
          </View>
        }
        renderItem={({ item }) => (
          <SongListRow
            song={item}
            onPress={() => handlePlay(item)}
            onPressMore={() => handleRemove(item)}
          />
        )}
      />
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    marginBottom: 6,
  },
  title: { color: colors.text, fontSize: 18, fontWeight: "700" },
  empty: { alignItems: "center", marginTop: 90, paddingHorizontal: 40 },
  emptyText: { color: colors.textFaint, fontSize: 13, marginTop: 14, textAlign: "center" },
});
