import React, { useState } from "react";
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
import { LinearGradient } from "expo-linear-gradient";
import GlassView from "./GlassView";
import colors from "../theme/colors";
import { GLASS_BORDER } from "../theme/glass";
import { useLibrary } from "../context/LibraryContext";

export default function AddToPlaylistModal({ visible, song, onClose }) {
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
          <GlassView radius={22} style={styles.glass}>
            <View style={styles.dragHandle} />

            <View style={styles.songRow}>
              {song.image ? (
                <Image source={{ uri: song.image }} style={styles.songArt} />
              ) : (
                <View style={[styles.songArt, { backgroundColor: colors.surface }]} />
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
                placeholderTextColor={colors.textFaint}
                value={newName}
                onChangeText={setNewName}
                returnKeyType="done"
                onSubmitEditing={handleCreate}
              />
              <TouchableOpacity
                onPress={handleCreate}
                disabled={!newName.trim()}
                style={{ opacity: newName.trim() ? 1 : 0.4 }}
              >
                <LinearGradient colors={colors.gradient} style={styles.createBtn}>
                  <Ionicons name="add" size={22} color="#fff" />
                </LinearGradient>
              </TouchableOpacity>
            </View>

            {playlists.length > 0 && (
              <Text style={styles.sectionLabel}>Your playlists</Text>
            )}

            <FlatList
              data={playlists}
              keyExtractor={(item) => item.id}
              style={{ maxHeight: 280 }}
              showsVerticalScrollIndicator={false}
              ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
              ListEmptyComponent={
                <View style={styles.emptyWrap}>
                  <Ionicons name="albums-outline" size={26} color={colors.textFaint} />
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
                    activeOpacity={0.75}
                    onPress={() => !already && handleAdd(item.id)}
                    disabled={already}
                  >
                    <View style={styles.plIconWrap}>
                      <Ionicons name="musical-notes" size={16} color={colors.coral} />
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
                      color={already || justAdded ? colors.success : colors.textFaint}
                    />
                  </TouchableOpacity>
                );
              }}
            />

            <TouchableOpacity style={styles.cancel} onPress={onClose}>
              <Text style={styles.cancelText}>Close</Text>
            </TouchableOpacity>
          </GlassView>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: "rgba(5,1,7,0.55)",
    justifyContent: "flex-end",
  },
  sheetWrap: { paddingHorizontal: 16, paddingBottom: 30 },
  glass: { padding: 20 },
  dragHandle: {
    alignSelf: "center",
    width: 36,
    height: 4,
    borderRadius: 2,
    backgroundColor: colors.borderStrong,
    marginBottom: 16,
  },
  songRow: { flexDirection: "row", alignItems: "center", marginBottom: 18 },
  songArt: { width: 50, height: 50, borderRadius: 12, borderWidth: 1, borderColor: GLASS_BORDER },
  title: { color: colors.text, fontSize: 17, fontWeight: "700" },
  subtitle: { color: colors.textFaint, fontSize: 12, marginTop: 3 },
  createRow: { flexDirection: "row", alignItems: "center" },
  input: {
    flex: 1,
    color: colors.text,
    backgroundColor: "rgba(255,255,255,0.10)",
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: GLASS_BORDER,
    fontSize: 14,
  },
  createBtn: {
    marginLeft: 10,
    width: 44,
    height: 44,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
  },
  sectionLabel: {
    color: colors.textFaint,
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 1,
    textTransform: "uppercase",
    marginTop: 22,
    marginBottom: 10,
  },
  emptyWrap: { alignItems: "center", paddingVertical: 22 },
  empty: { color: colors.textFaint, fontSize: 12.5, textAlign: "center", marginTop: 10, paddingHorizontal: 20 },
  plRow: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "rgba(255,255,255,0.06)",
    borderRadius: 14,
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.10)",
  },
  plIconWrap: {
    width: 36,
    height: 36,
    borderRadius: 10,
    backgroundColor: "rgba(255,255,255,0.08)",
    alignItems: "center",
    justifyContent: "center",
  },
  plName: { color: colors.text, fontSize: 14, fontWeight: "600" },
  plCount: { color: colors.textFaint, fontSize: 11.5, marginTop: 2 },
  cancel: { marginTop: 18, alignItems: "center", paddingVertical: 8 },
  cancelText: { color: colors.textDim, fontSize: 14, fontWeight: "600" },
});
