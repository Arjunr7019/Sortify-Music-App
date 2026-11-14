import React, { useState, useMemo } from "react";
import {
  Modal,
  View,
  Text,
  TouchableOpacity,
  TextInput,
  FlatList,
  StyleSheet,
  Pressable,
  Image,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useAppTheme } from "../context/ThemeContext";
import { useLibrary } from "../context/LibraryContext";

export default function AddToPlaylistModal({ visible, song, onClose }) {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { playlists, createPlaylist, addSongToPlaylist } = useLibrary();
  const [newName, setNewName] = useState("");
  const [justAddedId, setJustAddedId] = useState(null);

  if (!song) return null;

  const handleAdd = (playlistId) => {
    addSongToPlaylist(playlistId, song);
    setJustAddedId(playlistId);
    setTimeout(onClose, 260);
  };

  const handleCreate = () => {
    const name = newName.trim();
    if (!name) return;
    const id = createPlaylist(name);
    addSongToPlaylist(id, song);
    setNewName("");
    setTimeout(onClose, 260);
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose}>
        <Pressable onPress={(e) => e.stopPropagation()} style={styles.sheetWrap}>
          <View style={styles.sheet}>
            <View style={styles.dragHandle} />

            <View style={styles.songRow}>
              {song.image ? (
                <Image source={{ uri: song.image }} style={styles.songArt} />
              ) : (
                <View style={[styles.songArt, { backgroundColor: theme.placeholder }]} />
              )}
              <View style={{ flex: 1, marginLeft: 12 }}>
                <Text style={styles.title}>Add to playlist</Text>
                <Text style={styles.subtitle} numberOfLines={1}>
                  {song.name}
                </Text>
              </View>
            </View>

            <View style={styles.createRow}>
              <TextInput
                style={styles.input}
                placeholder="New playlist name"
                placeholderTextColor={theme.textFaint}
                value={newName}
                onChangeText={setNewName}
                returnKeyType="done"
                onSubmitEditing={handleCreate}
              />
              <TouchableOpacity
                onPress={handleCreate}
                disabled={!newName.trim()}
                style={[styles.createBtn, { opacity: newName.trim() ? 1 : 0.4 }]}
              >
                <Ionicons name="add" size={22} color={theme.accentOn} />
              </TouchableOpacity>
            </View>

            {playlists.length > 0 && <Text style={styles.sectionLabel}>Your playlists</Text>}

            <FlatList
              data={playlists}
              keyExtractor={(item) => item.id}
              style={{ maxHeight: 280 }}
              showsVerticalScrollIndicator={false}
              ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
              ListEmptyComponent={
                <View style={styles.emptyWrap}>
                  <Ionicons name="albums-outline" size={26} color={theme.textFaint} />
                  <Text style={styles.empty}>
                    No playlists yet — create one above to get started.
                  </Text>
                </View>
              }
              renderItem={({ item }) => {
                const already = item.songs.some((s) => s.id === song.id);
                const justAdded = justAddedId === item.id;
                return (
                  <TouchableOpacity
                    style={styles.plRow}
                    activeOpacity={0.7}
                    onPress={() => !already && handleAdd(item.id)}
                    disabled={already}
                  >
                    <View style={styles.plIconWrap}>
                      <Ionicons name="musical-notes" size={16} color={theme.accent} />
                    </View>
                    <View style={{ flex: 1, marginLeft: 12 }}>
                      <Text style={styles.plName} numberOfLines={1}>
                        {item.name}
                      </Text>
                      <Text style={styles.plCount}>
                        {item.songs.length} song{item.songs.length === 1 ? "" : "s"}
                      </Text>
                    </View>
                    <Ionicons
                      name={already || justAdded ? "checkmark-circle" : "add-circle-outline"}
                      size={22}
                      color={already || justAdded ? theme.success : theme.textFaint}
                    />
                  </TouchableOpacity>
                );
              }}
            />

            <TouchableOpacity style={styles.cancel} onPress={onClose}>
              <Text style={styles.cancelText}>Close</Text>
            </TouchableOpacity>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    backdrop: { flex: 1, backgroundColor: "rgba(0,0,0,0.5)", justifyContent: "flex-end" },
    sheetWrap: { paddingHorizontal: 16, paddingBottom: 30 },
    sheet: {
      backgroundColor: theme.background,
      borderRadius: 20,
      padding: 20,
      borderWidth: 1,
      borderColor: theme.border,
    },
    dragHandle: {
      alignSelf: "center",
      width: 36,
      height: 4,
      borderRadius: 2,
      backgroundColor: theme.surfaceAlt,
      marginBottom: 16,
    },
    songRow: { flexDirection: "row", alignItems: "center", marginBottom: 18 },
    songArt: { width: 50, height: 50, borderRadius: 12 },
    title: { color: theme.text, fontSize: 17, fontWeight: "700" },
    subtitle: { color: theme.textFaint, fontSize: 12, marginTop: 3 },
    createRow: { flexDirection: "row", alignItems: "center" },
    input: {
      flex: 1,
      color: theme.text,
      backgroundColor: theme.surface,
      borderRadius: 12,
      paddingHorizontal: 14,
      paddingVertical: 12,
      borderWidth: 1,
      borderColor: theme.border,
      fontSize: 14,
    },
    createBtn: {
      marginLeft: 10,
      width: 44,
      height: 44,
      borderRadius: 12,
      backgroundColor: theme.accent,
      alignItems: "center",
      justifyContent: "center",
    },
    sectionLabel: {
      color: theme.textFaint,
      fontSize: 11,
      fontWeight: "700",
      letterSpacing: 1,
      textTransform: "uppercase",
      marginTop: 22,
      marginBottom: 10,
    },
    emptyWrap: { alignItems: "center", paddingVertical: 22 },
    empty: { color: theme.textFaint, fontSize: 12.5, textAlign: "center", marginTop: 10, paddingHorizontal: 20 },
    plRow: {
      flexDirection: "row",
      alignItems: "center",
      backgroundColor: theme.surface,
      borderRadius: 14,
      paddingVertical: 10,
      paddingHorizontal: 12,
      borderWidth: 1,
      borderColor: theme.border,
    },
    plIconWrap: {
      width: 36,
      height: 36,
      borderRadius: 10,
      backgroundColor: theme.surfaceAlt,
      alignItems: "center",
      justifyContent: "center",
    },
    plName: { color: theme.text, fontSize: 14, fontWeight: "600" },
    plCount: { color: theme.textFaint, fontSize: 11.5, marginTop: 2 },
    cancel: { marginTop: 18, alignItems: "center", paddingVertical: 8 },
    cancelText: { color: theme.textSecondary, fontSize: 14, fontWeight: "600" },
  });
