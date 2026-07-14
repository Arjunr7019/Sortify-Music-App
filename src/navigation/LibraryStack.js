import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import LibraryScreen from "../screens/LibraryScreen";
import FavoritesScreen from "../screens/FavoritesScreen";
import PlaylistsScreen from "../screens/PlaylistsScreen";
import PlaylistDetailScreen from "../screens/PlaylistDetailScreen";
import DownloadsScreen from "../screens/DownloadsScreen";

const Stack = createNativeStackNavigator();

export default function LibraryStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="LibraryHome" component={LibraryScreen} />
      <Stack.Screen name="Favorites" component={FavoritesScreen} />
      <Stack.Screen name="Playlists" component={PlaylistsScreen} />
      <Stack.Screen name="PlaylistDetail" component={PlaylistDetailScreen} />
      <Stack.Screen name="Downloads" component={DownloadsScreen} />
    </Stack.Navigator>
  );
}
