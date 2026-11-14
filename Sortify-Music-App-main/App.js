import React from "react";
import { NavigationContainer, DefaultTheme, DarkTheme } from "@react-navigation/native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { View, ActivityIndicator } from "react-native";

import { LibraryProvider } from "./src/context/LibraryContext";
import { PlayerProvider } from "./src/context/PlayerContext";
import { OnboardingProvider, useOnboarding } from "./src/context/OnboardingContext";
import { ThemeProvider, useAppTheme } from "./src/context/ThemeContext";
import { QualityProvider } from "./src/context/QualityContext";

import BottomTabs from "./src/navigation/BottomTabs";
import PlayerSheet from "./src/components/PlayerSheet";
import SettingsScreen from "./src/screens/SettingsScreen";
import LanguageSetupScreen from "./src/screens/LanguageSetupScreen";
import AppScreen from "./src/components/AppScreen";

function RootNavigator() {
  const { theme, mode } = useAppTheme();
  const { loaded, needsSetup } = useOnboarding();

  const navTheme = {
    ...(mode === "dark" ? DarkTheme : DefaultTheme),
    colors: {
      ...(mode === "dark" ? DarkTheme.colors : DefaultTheme.colors),
      background: theme.background,
      card: theme.background,
      border: theme.border,
      primary: theme.accent,
      text: theme.text,
    },
  };

  if (!loaded) {
    return (
      <AppScreen>
        <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
          <ActivityIndicator color={theme.accent} size="large" />
        </View>
      </AppScreen>
    );
  }

  return (
    <>
      <NavigationContainer theme={navTheme}>
        {needsSetup ? (
          <LanguageSetupScreen />
        ) : (
          <View style={{ flex: 1, backgroundColor: theme.background }}>
            <BottomTabs />
            <PlayerSheet />
          </View>
        )}
      </NavigationContainer>
      <SettingsScreen />
    </>
  );
}

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <ThemeProvider>
          <OnboardingProvider>
            <LibraryProvider>
              <QualityProvider>
                <PlayerProvider>
                  <RootNavigator />
                </PlayerProvider>
              </QualityProvider>
            </LibraryProvider>
          </OnboardingProvider>
        </ThemeProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
