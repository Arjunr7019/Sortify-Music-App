import React, { useState, useMemo, useEffect } from "react";
import { Modal, View, Text, TouchableOpacity, StyleSheet, FlatList } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAppTheme } from "../context/ThemeContext";
import { ALL_LANGUAGES, useOnboarding } from "../context/OnboardingContext";

// Reuses the onboarding chip picker, but as a modal for editing the
// selection later from Settings (pre-filled with the current languages).
export default function LanguagePickerModal({ visible, onClose }) {
  const insets = useSafeAreaInsets();
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { languages, saveLanguages } = useOnboarding();
  const [selected, setSelected] = useState(languages);

  useEffect(() => {
    if (visible) setSelected(languages);
  }, [visible, languages]);

  const toggle = (lang) => {
    setSelected((prev) => (prev.includes(lang) ? prev.filter((l) => l !== lang) : [...prev, lang]));
  };

  const handleSave = () => {
    if (selected.length === 0) return;
    saveLanguages(selected);
    onClose();
  };

  return (
    <Modal visible={visible} animationType="slide" onRequestClose={onClose} presentationStyle="pageSheet">
      <View style={[styles.root, { paddingTop: insets.top + 16 }]}>
        <View style={styles.header}>
          <Text style={styles.title}>Trending languages</Text>
          <TouchableOpacity onPress={onClose} hitSlop={10}>
            <Ionicons name="close" size={24} color={theme.text} />
          </TouchableOpacity>
        </View>
        <Text style={styles.subtitle}>Pick the languages used for your Home trending rows.</Text>

        <FlatList
          data={ALL_LANGUAGES}
          keyExtractor={(item) => item}
          numColumns={2}
          columnWrapperStyle={{ justifyContent: "space-between" }}
          style={{ marginTop: 20 }}
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
          style={[styles.saveBtn, selected.length === 0 && { opacity: 0.4 }]}
          disabled={selected.length === 0}
          onPress={handleSave}
        >
          <Text style={styles.saveText}>Save</Text>
        </TouchableOpacity>
      </View>
    </Modal>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    root: { flex: 1, backgroundColor: theme.background, paddingHorizontal: 20 },
    header: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
    title: { color: theme.text, fontSize: 20, fontWeight: "800" },
    subtitle: { color: theme.textFaint, fontSize: 12.5, marginTop: 8, lineHeight: 18 },
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
    saveBtn: {
      marginTop: 12,
      marginBottom: 24,
      borderRadius: 16,
      paddingVertical: 16,
      alignItems: "center",
      backgroundColor: theme.accent,
    },
    saveText: { color: theme.accentOn, fontSize: 15, fontWeight: "700" },
  });
