import React, { useMemo } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppScreen from "../components/AppScreen";
import SongListRow from "../components/SongListRow";
import { useAppTheme } from "../context/ThemeContext";
import { useLibrary } from "../context/LibraryContext";
import { usePlayer } from "../context/PlayerContext";

export default function FavoritesScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { favorites } = useLibrary();
  const { playSong } = usePlayer();

  return (
    <AppScreen>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} hitSlop={10}>
          <Ionicons name="chevron-back" size={24} color={theme.text} />
        </TouchableOpacity>
        <Text style={styles.title}>Favourites</Text>
        <View style={{ width: 24 }} />
      </View>

      <FlatList
        data={favorites}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ paddingBottom: 160, paddingTop: 10 }}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="heart-outline" size={36} color={theme.textFaint} />
            <Text style={styles.emptyText}>Tap the heart on any song to save it here.</Text>
          </View>
        }
        renderItem={({ item }) => <SongListRow song={item} onPress={() => playSong(item, favorites)} />}
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
    title: { color: theme.text, fontSize: 18, fontWeight: "700" },
    empty: { alignItems: "center", marginTop: 90, paddingHorizontal: 40 },
    emptyText: { color: theme.textFaint, fontSize: 13, marginTop: 14, textAlign: "center" },
  });
