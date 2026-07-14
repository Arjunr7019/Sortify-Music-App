import React, { useState } from "react";
import { View, Text, TouchableOpacity, StyleSheet, Image, FlatList } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppBackground from "../components/AppBackground";
import GlassView from "../components/GlassView";
import colors from "../theme/colors";
import { ALL_LANGUAGES, useOnboarding } from "../context/OnboardingContext";

export default function LanguageSetupScreen() {
  const insets = useSafeAreaInsets();
  const { saveLanguages } = useOnboarding();
  const [selected, setSelected] = useState([]);

  const toggle = (lang) => {
    setSelected((prev) =>
      prev.includes(lang) ? prev.filter((l) => l !== lang) : [...prev, lang]
    );
  };

  const handleContinue = () => {
    if (selected.length === 0) return;
    saveLanguages(selected);
  };

  return (
    <AppBackground>
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
              <TouchableOpacity style={styles.chipWrap} onPress={() => toggle(item)} activeOpacity={0.8}>
                {active ? (
                  <LinearGradient colors={colors.gradient} style={styles.chipActive}>
                    <Text style={styles.chipTextActive}>{item}</Text>
                    <Ionicons name="checkmark-circle" size={16} color="#fff" style={{ marginLeft: 6 }} />
                  </LinearGradient>
                ) : (
                  <GlassView radius={16} style={{ width: "100%" }}>
                    <View style={styles.chipInactive}>
                      <Text style={styles.chipText}>{item}</Text>
                    </View>
                  </GlassView>
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
          <LinearGradient colors={colors.gradient} style={styles.continueGradient}>
            <Text style={styles.continueText}>
              Continue{selected.length ? ` (${selected.length})` : ""}
            </Text>
          </LinearGradient>
        </TouchableOpacity>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  wrap: { flex: 1, paddingHorizontal: 24 },
  logo: { width: 56, height: 56, marginBottom: 18 },
  title: { color: colors.text, fontSize: 26, fontWeight: "800" },
  subtitle: { color: colors.textFaint, fontSize: 13, marginTop: 10, lineHeight: 19 },
  chipWrap: { width: "48%", marginBottom: 12 },
  chipActive: {
    borderRadius: 16,
    paddingVertical: 14,
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "center",
  },
  chipInactive: { paddingVertical: 14, alignItems: "center" },
  chipText: { color: colors.textDim, fontSize: 14, fontWeight: "600" },
  chipTextActive: { color: "#fff", fontSize: 14, fontWeight: "700" },
  continueBtn: { marginTop: "auto", borderRadius: 18, overflow: "hidden" },
  continueGradient: { paddingVertical: 16, alignItems: "center" },
  continueText: { color: "#fff", fontSize: 15, fontWeight: "700" },
});
