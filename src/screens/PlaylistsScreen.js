import React, { useState } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity, TextInput, Modal, Pressable } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppBackground from "../components/AppBackground";
import GlassView from "../components/GlassView";
import colors from "../theme/colors";
import { useLibrary } from "../context/LibraryContext";

export default function PlaylistsScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const { playlists, createPlaylist } = useLibrary();
  const [showCreate, setShowCreate] = useState(false);
  const [name, setName] = useState("");

  const handleCreate = () => {
    const trimmed = name.trim();
    if (!trimmed) return;
    createPlaylist(trimmed);
    setName("");
    setShowCreate(false);
  };

  return (
    <AppBackground>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} hitSlop={10}>
          <Ionicons name="chevron-back" size={24} color={colors.text} />
        </TouchableOpacity>
        <Text style={styles.title}>Playlists</Text>
        <TouchableOpacity onPress={() => setShowCreate(true)} hitSlop={10}>
          <Ionicons name="add" size={26} color={colors.text} />
        </TouchableOpacity>
      </View>

      <FlatList
        data={playlists}
        keyExtractor={(item) => item.id}
        numColumns={2}
        columnWrapperStyle={{ paddingHorizontal: 16, justifyContent: "space-between" }}
        contentContainerStyle={{ paddingBottom: 160 + insets.bottom, paddingTop: 10 }}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="albums-outline" size={40} color={colors.textFaint} />
            <Text style={styles.emptyText}>Create your first playlist with the + button.</Text>
          </View>
        }
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.card}
            activeOpacity={0.85}
            onPress={() => navigation.navigate("PlaylistDetail", { playlistId: item.id })}
          >
            <GlassView radius={18}>
              <LinearGradient colors={colors.gradientSoft} style={styles.artwork}>
                <Ionicons name="musical-notes" size={30} color="rgba(255,255,255,0.9)" />
              </LinearGradient>
              <View style={styles.cardFooter}>
                <Text style={styles.cardName} numberOfLines={1}>
                  {item.name}
                </Text>
                <Text style={styles.cardCount}>
                  {item.songs.length} song{item.songs.length === 1 ? "" : "s"}
                </Text>
              </View>
            </GlassView>
          </TouchableOpacity>
        )}
      />

      <Modal visible={showCreate} transparent animationType="fade" onRequestClose={() => setShowCreate(false)}>
        <Pressable style={styles.backdrop} onPress={() => setShowCreate(false)}>
          <Pressable style={styles.modalWrap} onPress={(e) => e.stopPropagation()}>
            <GlassView intensity={60} radius={22} style={{ padding: 20 }}>
              <Text style={styles.modalTitle}>New playlist</Text>
              <TextInput
                style={styles.input}
                placeholder="Playlist name"
                placeholderTextColor={colors.textFaint}
                value={name}
                onChangeText={setName}
                autoFocus
              />
              <View style={styles.modalActions}>
                <TouchableOpacity onPress={() => setShowCreate(false)} style={styles.modalBtn}>
                  <Text style={styles.modalCancel}>Cancel</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={handleCreate} style={styles.modalBtn}>
                  <Text style={styles.modalCreate}>Create</Text>
                </TouchableOpacity>
              </View>
            </GlassView>
          </Pressable>
        </Pressable>
      </Modal>
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
  card: { width: "48%", marginBottom: 16 },
  artwork: { height: 130, alignItems: "center", justifyContent: "center" },
  cardFooter: { padding: 12 },
  cardName: { color: colors.text, fontSize: 14, fontWeight: "700" },
  cardCount: { color: colors.textFaint, fontSize: 11, marginTop: 3 },
  backdrop: { flex: 1, backgroundColor: "rgba(5,1,7,0.6)", justifyContent: "center" },
  modalWrap: { paddingHorizontal: 24 },
  modalTitle: { color: colors.text, fontSize: 16, fontWeight: "700", marginBottom: 14 },
  input: {
    color: colors.text,
    backgroundColor: colors.surface,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: colors.border,
    fontSize: 14,
  },
  modalActions: { flexDirection: "row", justifyContent: "flex-end", marginTop: 16, gap: 18 },
  modalBtn: { paddingVertical: 6, paddingHorizontal: 4 },
  modalCancel: { color: colors.textDim, fontSize: 14, fontWeight: "600" },
  modalCreate: { color: colors.coral, fontSize: 14, fontWeight: "700" },
});
