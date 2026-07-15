import React, { useState, useMemo } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity, TextInput, Modal, Pressable, Image } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppScreen from "../components/AppScreen";
import { useAppTheme } from "../context/ThemeContext";
import { useLibrary } from "../context/LibraryContext";

export default function PlaylistsScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
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
    <AppScreen>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} hitSlop={10}>
          <Ionicons name="chevron-back" size={24} color={theme.text} />
        </TouchableOpacity>
        <Text style={styles.title}>Playlists</Text>
        <TouchableOpacity onPress={() => setShowCreate(true)} hitSlop={10}>
          <Ionicons name="add-circle" size={26} color={theme.accent} />
        </TouchableOpacity>
      </View>

      <FlatList
        data={playlists}
        keyExtractor={(item) => item.id}
        numColumns={2}
        columnWrapperStyle={{ paddingHorizontal: 16, justifyContent: "space-between" }}
        contentContainerStyle={{ paddingBottom: 160, paddingTop: 10 }}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="albums-outline" size={36} color={theme.textFaint} />
            <Text style={styles.emptyText}>Create your first playlist with the + button.</Text>
          </View>
        }
        renderItem={({ item }) => {
          const thumbs = (item.songs || []).slice(0, 4);
          return (
            <TouchableOpacity
              style={styles.card}
              activeOpacity={0.8}
              onPress={() => navigation.navigate("PlaylistDetail", { playlistId: item.id })}
            >
              {thumbs.length > 0 ? (
                <View style={styles.grid}>
                  {[0, 1, 2, 3].map((i) => (
                    <View key={i} style={styles.cell}>
                      {thumbs[i]?.image ? (
                        <Image source={{ uri: thumbs[i].image }} style={styles.cellImage} />
                      ) : (
                        <View style={[styles.cellImage, { backgroundColor: theme.placeholder }]} />
                      )}
                    </View>
                  ))}
                </View>
              ) : (
                <View style={styles.emptyArt}>
                  <Ionicons name="musical-notes" size={26} color={theme.textFaint} />
                </View>
              )}
              <Text style={styles.cardName} numberOfLines={1}>
                {item.name}
              </Text>
              <Text style={styles.cardCount}>
                {item.songs.length} song{item.songs.length === 1 ? "" : "s"}
              </Text>
            </TouchableOpacity>
          );
        }}
      />

      <Modal visible={showCreate} transparent animationType="fade" onRequestClose={closeModal}>
        <Pressable style={styles.backdrop} onPress={closeModal}>
          <Pressable style={styles.modalWrap} onPress={(e) => e.stopPropagation()}>
            <View style={styles.modal}>
              <View style={styles.modalHeader}>
                <View style={styles.modalIcon}>
                  <Ionicons name="add" size={20} color={theme.accentOn} />
                </View>
                <View style={{ marginLeft: 12, flex: 1 }}>
                  <Text style={styles.modalTitle}>New playlist</Text>
                  <Text style={styles.modalSubtitle}>Give your mix a name</Text>
                </View>
              </View>

              <TextInput
                style={styles.input}
                placeholder="e.g. Late night drive"
                placeholderTextColor={theme.textFaint}
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
                  style={[styles.primaryBtn, { opacity: name.trim() ? 1 : 0.4 }]}
                >
                  <Text style={styles.modalCreate}>Create playlist</Text>
                </TouchableOpacity>
              </View>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </AppScreen>
  );
}

const CARD_SIZE = 158;

const makeStyles = (theme) =>
  StyleSheet.create({
    header: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      paddingHorizontal: 20,
      marginBottom: 6,
    },
    title: { color: theme.text, fontSize: 18, fontWeight: "700" },
    empty: { alignItems: "center", marginTop: 90, paddingHorizontal: 40 },
    emptyText: { color: theme.textFaint, fontSize: 13, marginTop: 14, textAlign: "center" },
    card: { width: "48%", marginBottom: 20 },
    grid: {
      width: "100%",
      height: CARD_SIZE * 0.72,
      borderRadius: 14,
      overflow: "hidden",
      flexDirection: "row",
      flexWrap: "wrap",
    },
    cell: { width: "50%", height: "50%" },
    cellImage: { width: "100%", height: "100%" },
    emptyArt: {
      width: "100%",
      height: CARD_SIZE * 0.72,
      borderRadius: 14,
      backgroundColor: theme.surface,
      alignItems: "center",
      justifyContent: "center",
    },
    cardName: { color: theme.text, fontSize: 14, fontWeight: "700", marginTop: 8 },
    cardCount: { color: theme.textFaint, fontSize: 11, marginTop: 3 },
    backdrop: { flex: 1, backgroundColor: "rgba(0,0,0,0.5)", justifyContent: "center" },
    modalWrap: { paddingHorizontal: 24 },
    modal: {
      backgroundColor: theme.background,
      borderRadius: 20,
      padding: 22,
      borderWidth: 1,
      borderColor: theme.border,
    },
    modalHeader: { flexDirection: "row", alignItems: "center", marginBottom: 18 },
    modalIcon: {
      width: 44,
      height: 44,
      borderRadius: 14,
      backgroundColor: theme.accent,
      alignItems: "center",
      justifyContent: "center",
    },
    modalTitle: { color: theme.text, fontSize: 16, fontWeight: "700" },
    modalSubtitle: { color: theme.textFaint, fontSize: 12, marginTop: 2 },
    input: {
      color: theme.text,
      backgroundColor: theme.surface,
      borderRadius: 12,
      paddingHorizontal: 14,
      paddingVertical: 12,
      borderWidth: 1,
      borderColor: theme.border,
      fontSize: 14,
    },
    modalActions: { flexDirection: "row", alignItems: "center", marginTop: 18, gap: 12 },
    secondaryBtn: { paddingVertical: 14, paddingHorizontal: 16 },
    primaryBtn: {
      flex: 1,
      borderRadius: 12,
      paddingVertical: 14,
      alignItems: "center",
      backgroundColor: theme.accent,
    },
    modalCancel: { color: theme.textSecondary, fontSize: 14, fontWeight: "600" },
    modalCreate: { color: theme.accentOn, fontSize: 14, fontWeight: "700" },
  });
