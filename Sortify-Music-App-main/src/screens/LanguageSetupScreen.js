import React, { useState, useMemo } from "react";
import { View, Text, TouchableOpacity, StyleSheet, Image, FlatList } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppScreen from "../components/AppScreen";
import { useAppTheme } from "../context/ThemeContext";
import { ALL_LANGUAGES, useOnboarding } from "../context/OnboardingContext";

export default function LanguageSetupScreen() {
  const insets = useSafeAreaInsets();
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { saveLanguages } = useOnboarding();
  const [selected, setSelected] = useState([]);

  const toggle = (lang) => {
    setSelected((prev) => (prev.includes(lang) ? prev.filter((l) => l !== lang) : [...prev, lang]));
  };

  const handleContinue = () => {
    if (selected.length === 0) return;
    saveLanguages(selected);
  };

  return (
    <AppScreen>
      <View style={[styles.wrap, { paddingTop: insets.top + 40, paddingBottom: insets.bottom + 24 }]}>
        <Image source={require("../../assets/logo.png")} style={styles.logo} resizeMode="contain" />
        <Text style={styles.title}>Pick your languages</Text>
        <Text style={styles.subtitle}>
          We'll use these to curate trending songs for your Home screen. You can select more than one.
        </Text>

        <FlatList
          data={ALL_LANGUAGES}
          keyExtractor={(item) => item}
          numColumns={2}
          columnWrapperStyle={{ justifyContent: "space-between" }}
          style={{ marginTop: 28 }}
          renderItem={({ item }) => {
            const active = selected.includes(item);
            return (
              <TouchableOpacity
                style={[styles.chip, active && styles.chipActive]}
                onPress={() => toggle(item)}
                activeOpacity={0.75}
              >
                <Text style={[styles.chipText, active && styles.chipTextActive]}>{item}</Text>
                {active && (
                  <Ionicons name="checkmark-circle" size={16} color={theme.accentOn} style={{ marginLeft: 6 }} />
                )}
              </TouchableOpacity>
            );
          }}
        />

        <TouchableOpacity
          style={[styles.continueBtn, selected.length === 0 && { opacity: 0.4 }]}
          disabled={selected.length === 0}
          onPress={handleContinue}
          activeOpacity={0.85}
        >
          <Text style={styles.continueText}>
            Continue{selected.length ? ` (${selected.length})` : ""}
          </Text>
        </TouchableOpacity>
      </View>
    </AppScreen>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    wrap: { flex: 1, paddingHorizontal: 24 },
    logo: { width: 56, height: 56, marginBottom: 18 },
    title: { color: theme.text, fontSize: 26, fontWeight: "800" },
    subtitle: { color: theme.textFaint, fontSize: 13, marginTop: 10, lineHeight: 19 },
    chip: {
      width: "48%",
      marginBottom: 12,
      borderRadius: 14,
      paddingVertical: 14,
      alignItems: "center",
      flexDirection: "row",
      justifyContent: "center",
      backgroundColor: theme.surface,
    },
    chipActive: { backgroundColor: theme.accent },
    chipText: { color: theme.textSecondary, fontSize: 14, fontWeight: "600" },
    chipTextActive: { color: theme.accentOn, fontWeight: "700" },
    continueBtn: {
      marginTop: "auto",
      borderRadius: 16,
      paddingVertical: 16,
      alignItems: "center",
      backgroundColor: theme.accent,
    },
    continueText: { color: theme.accentOn, fontSize: 15, fontWeight: "700" },
  });
