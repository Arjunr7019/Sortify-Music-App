import React from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppBackground from "../components/AppBackground";
import SongListRow from "../components/SongListRow";
import colors from "../theme/colors";
import { useLibrary } from "../context/LibraryContext";
import { usePlayer } from "../context/PlayerContext";

export default function FavoritesScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const { favorites } = useLibrary();
  const { playSong } = usePlayer();

  return (
    <AppBackground>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} hitSlop={10}>
          <Ionicons name="chevron-back" size={24} color={colors.text} />
        </TouchableOpacity>
        <Text style={styles.title}>Favorites</Text>
        <View style={{ width: 24 }} />
      </View>

      <FlatList
        data={favorites}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ paddingBottom: 160 + insets.bottom, paddingTop: 10 }}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="heart-outline" size={40} color={colors.textFaint} />
            <Text style={styles.emptyText}>
              Tap the heart on any song to save it here.
            </Text>
          </View>
        }
        renderItem={({ item }) => (
          <SongListRow song={item} onPress={() => playSong(item, favorites)} />
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
