import React from "react";
import { StatusBar } from "expo-status-bar";
import { NavigationContainer, DefaultTheme } from "@react-navigation/native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { View, ActivityIndicator } from "react-native";

import { LibraryProvider } from "./src/context/LibraryContext";
import { PlayerProvider } from "./src/context/PlayerContext";
import { OnboardingProvider, useOnboarding } from "./src/context/OnboardingContext";

import BottomTabs from "./src/navigation/BottomTabs";
import PlayerSheet from "./src/components/PlayerSheet";
import LanguageSetupScreen from "./src/screens/LanguageSetupScreen";
import AppBackground from "./src/components/AppBackground";
import colors from "./src/theme/colors";

const NavTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: "transparent",
    card: "transparent",
    border: "transparent",
    primary: colors.coral,
    text: colors.text,
  },
};

function RootNavigator() {
  const { loaded, needsSetup } = useOnboarding();

  if (!loaded) {
    return (
      <AppBackground>
        <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
          <ActivityIndicator color={colors.coral} size="large" />
        </View>
      </AppBackground>
    );
  }

  if (needsSetup) {
    return <LanguageSetupScreen />;
  }

  return (
    <View style={{ flex: 1 }}>
      <BottomTabs />
      <PlayerSheet />
    </View>
  );
}

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <OnboardingProvider>
          <LibraryProvider>
            <PlayerProvider>
              <NavigationContainer theme={NavTheme}>
                <StatusBar style="light" />
                <RootNavigator />
              </NavigationContainer>
            </PlayerProvider>
          </LibraryProvider>
        </OnboardingProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
