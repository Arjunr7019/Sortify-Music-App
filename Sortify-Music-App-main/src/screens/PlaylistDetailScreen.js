import React, { useMemo } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity, Alert } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppScreen from "../components/AppScreen";
import SongListRow from "../components/SongListRow";
import { useAppTheme } from "../context/ThemeContext";
import { useLibrary } from "../context/LibraryContext";
import { usePlayer } from "../context/PlayerContext";

export default function PlaylistDetailScreen({ route, navigation }) {
  const { playlistId } = route.params;
  const insets = useSafeAreaInsets();
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { playlists, removeSongFromPlaylist, deletePlaylist } = useLibrary();
  const { playSong } = usePlayer();

  const playlist = playlists.find((p) => p.id === playlistId);

  if (!playlist) {
    return (
      <AppScreen>
        <View style={{ paddingTop: insets.top + 30, alignItems: "center" }}>
          <Text style={{ color: theme.textFaint }}>Playlist not found</Text>
        </View>
      </AppScreen>
    );
  }

  const confirmDelete = () => {
    Alert.alert("Delete playlist?", `"${playlist.name}" will be removed permanently.`, [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete",
        style: "destructive",
        onPress: () => {
          deletePlaylist(playlist.id);
          navigation.goBack();
        },
      },
    ]);
  };

  return (
    <AppScreen>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} hitSlop={10}>
          <Ionicons name="chevron-back" size={24} color={theme.text} />
        </TouchableOpacity>
        <Text style={styles.title} numberOfLines={1}>
          {playlist.name}
        </Text>
        <TouchableOpacity onPress={confirmDelete} hitSlop={10}>
          <Ionicons name="trash-outline" size={20} color={theme.textSecondary} />
        </TouchableOpacity>
      </View>

      <View style={styles.bannerWrap}>
        <View style={styles.banner}>
          <Ionicons name="musical-notes" size={38} color={theme.textFaint} />
        </View>
        <Text style={styles.count}>
          {playlist.songs.length} song{playlist.songs.length === 1 ? "" : "s"}
        </Text>
      </View>

      <FlatList
        data={playlist.songs}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ paddingBottom: 160, paddingTop: 10 }}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="musical-note-outline" size={30} color={theme.textFaint} />
            <Text style={styles.emptyText}>Add songs here from the player's + icon.</Text>
          </View>
        }
        renderItem={({ item }) => (
          <SongListRow
            song={item}
            onPress={() => playSong(item, playlist.songs)}
            onPressMore={() => removeSongFromPlaylist(playlist.id, item.id)}
          />
        )}
      />
    </AppScreen>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    header: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      paddingHorizontal: 20,
      marginBottom: 6,
    },
    title: { color: theme.text, fontSize: 18, fontWeight: "700", flex: 1, textAlign: "center" },
    bannerWrap: { alignItems: "center", marginTop: 10 },
    banner: {
      width: 140,
      height: 140,
      borderRadius: 18,
      backgroundColor: theme.surface,
      alignItems: "center",
      justifyContent: "center",
    },
    count: { color: theme.textFaint, fontSize: 12, marginTop: 10 },
    empty: { alignItems: "center", marginTop: 60, paddingHorizontal: 40 },
    emptyText: { color: theme.textFaint, fontSize: 13, marginTop: 14, textAlign: "center" },
  });
