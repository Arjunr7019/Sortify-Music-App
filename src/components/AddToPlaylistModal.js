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
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import GlassView from "./GlassView";
import colors from "../theme/colors";
import { useLibrary } from "../context/LibraryContext";

export default function AddToPlaylistModal({ visible, song, onClose }) {
  const { playlists, createPlaylist, addSongToPlaylist } = useLibrary();
  const [newName, setNewName] = useState("");

  if (!song) return null;

  const handleAdd = (playlistId) => {
    addSongToPlaylist(playlistId, song);
    onClose();
  };

  const handleCreate = () => {
    const name = newName.trim();
    if (!name) return;
    const id = createPlaylist(name);
    addSongToPlaylist(id, song);
    setNewName("");
    onClose();
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose}>
        <Pressable onPress={(e) => e.stopPropagation()} style={styles.sheetWrap}>
          <GlassView radius={24} intensity={60} style={styles.glass}>
            <Text style={styles.title}>Add to playlist</Text>
            <Text style={styles.subtitle} numberOfLines={1}>
              {song.name}
            </Text>

            <View style={styles.createRow}>
              <TextInput
                style={styles.input}
                placeholder="New playlist name"
                placeholderTextColor={colors.textFaint}
                value={newName}
                onChangeText={setNewName}
              />
              <TouchableOpacity style={styles.createBtn} onPress={handleCreate}>
                <Ionicons name="add" size={20} color={colors.text} />
              </TouchableOpacity>
            </View>

            <FlatList
              data={playlists}
              keyExtractor={(item) => item.id}
              style={{ maxHeight: 260, marginTop: 8 }}
              ItemSeparatorComponent={() => <View style={styles.sep} />}
              ListEmptyComponent={
                <Text style={styles.empty}>No playlists yet — create one above.</Text>
              }
              renderItem={({ item }) => (
                <TouchableOpacity style={styles.plRow} onPress={() => handleAdd(item.id)}>
                  <Ionicons name="musical-notes" size={16} color={colors.coral} />
                  <Text style={styles.plName} numberOfLines={1}>
                    {item.name}
                  </Text>
                  <Text style={styles.plCount}>{item.songs.length}</Text>
                </TouchableOpacity>
              )}
            />

            <TouchableOpacity style={styles.cancel} onPress={onClose}>
              <Text style={styles.cancelText}>Cancel</Text>
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
    backgroundColor: "rgba(5,1,7,0.6)",
    justifyContent: "flex-end",
  },
  sheetWrap: { paddingHorizontal: 16, paddingBottom: 30 },
  glass: { padding: 20 },
  title: { color: colors.text, fontSize: 17, fontWeight: "700" },
  subtitle: { color: colors.textFaint, fontSize: 12, marginTop: 4 },
  createRow: { flexDirection: "row", alignItems: "center", marginTop: 16 },
  input: {
    flex: 1,
    color: colors.text,
    backgroundColor: colors.surface,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: colors.border,
    fontSize: 14,
  },
  createBtn: {
    marginLeft: 10,
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: colors.coral,
    alignItems: "center",
    justifyContent: "center",
  },
  sep: { height: 1, backgroundColor: colors.border, marginVertical: 2 },
  plRow: { flexDirection: "row", alignItems: "center", paddingVertical: 10 },
  plName: { color: colors.text, fontSize: 14, marginLeft: 10, flex: 1 },
  plCount: { color: colors.textFaint, fontSize: 12 },
  empty: { color: colors.textFaint, fontSize: 13, textAlign: "center", paddingVertical: 20 },
  cancel: { marginTop: 12, alignItems: "center", paddingVertical: 10 },
  cancelText: { color: colors.textDim, fontSize: 14, fontWeight: "600" },
});
