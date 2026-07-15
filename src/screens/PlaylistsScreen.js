import React, { useState } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity, TextInput, Modal, Pressable } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppBackground from "../components/AppBackground";
import GlassView from "../components/GlassView";
import colors from "../theme/colors";
import { GLASS_BORDER } from "../theme/glass";
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

  const closeModal = () => {
    setShowCreate(false);
    setName("");
  };

  return (
    <AppBackground>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} hitSlop={10}>
          <Ionicons name="chevron-back" size={24} color={colors.text} />
        </TouchableOpacity>
        <Text style={styles.title}>Playlists</Text>
        <TouchableOpacity onPress={() => setShowCreate(true)} hitSlop={10}>
          <Ionicons name="add-circle" size={26} color={colors.coral} />
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
              <LinearGradient colors={colors.gradient} style={styles.artwork}>
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

      <Modal visible={showCreate} transparent animationType="fade" onRequestClose={closeModal}>
        <Pressable style={styles.backdrop} onPress={closeModal}>
          <Pressable style={styles.modalWrap} onPress={(e) => e.stopPropagation()}>
            <GlassView radius={22} style={{ padding: 22 }}>
              <View style={styles.modalHeader}>
                <LinearGradient colors={colors.gradient} style={styles.modalIcon}>
                  <Ionicons name="add" size={22} color="#fff" />
                </LinearGradient>
                <View style={{ marginLeft: 12, flex: 1 }}>
                  <Text style={styles.modalTitle}>New playlist</Text>
                  <Text style={styles.modalSubtitle}>Give your mix a name</Text>
                </View>
              </View>

              <TextInput
                style={styles.input}
                placeholder="e.g. Late night drive"
                placeholderTextColor={colors.onGlassFaint}
                value={name}
                onChangeText={setName}
                autoFocus
                returnKeyType="done"
                onSubmitEditing={handleCreate}
              />

              <View style={styles.modalActions}>
                <TouchableOpacity onPress={closeModal} style={styles.secondaryBtn}>
                  <Text style={styles.modalCancel}>Cancel</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  onPress={handleCreate}
                  disabled={!name.trim()}
                  style={{ opacity: name.trim() ? 1 : 0.4, flex: 1 }}
                >
                  <LinearGradient colors={colors.gradient} style={styles.primaryBtn}>
                    <Text style={styles.modalCreate}>Create playlist</Text>
                  </LinearGradient>
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
  cardName: { color: colors.onGlassText, fontSize: 14, fontWeight: "700" },
  cardCount: { color: colors.onGlassFaint, fontSize: 11, marginTop: 3 },
  backdrop: { flex: 1, backgroundColor: "rgba(5,1,7,0.55)", justifyContent: "center" },
  modalWrap: { paddingHorizontal: 24 },
  modalHeader: { flexDirection: "row", alignItems: "center", marginBottom: 18 },
  modalIcon: {
    width: 44,
    height: 44,
    borderRadius: 14,
    alignItems: "center",
    justifyContent: "center",
  },
  modalTitle: { color: colors.onGlassText, fontSize: 16, fontWeight: "700" },
  modalSubtitle: { color: colors.onGlassFaint, fontSize: 12, marginTop: 2 },
  input: {
    color: colors.onGlassText,
    backgroundColor: "rgba(255,255,255,0.10)",
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: GLASS_BORDER,
    fontSize: 14,
  },
  modalActions: { flexDirection: "row", alignItems: "center", marginTop: 18, gap: 12 },
  secondaryBtn: { paddingVertical: 14, paddingHorizontal: 16 },
  primaryBtn: { borderRadius: 12, paddingVertical: 14, alignItems: "center" },
  modalCancel: { color: colors.onGlassDim, fontSize: 14, fontWeight: "600" },
  modalCreate: { color: "#fff", fontSize: 14, fontWeight: "700" },
});
