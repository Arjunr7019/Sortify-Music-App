import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";

const STORAGE_KEY = "sortify:languages:v1";
const OnboardingContext = createContext(null);

export const ALL_LANGUAGES = [
  "Kannada",
  "English",
  "Hindi",
  "Tamil",
  "Telugu",
  "Marathi",
  "Malayalam",
];

export function OnboardingProvider({ children }) {
  const [languages, setLanguages] = useState([]);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const raw = await AsyncStorage.getItem(STORAGE_KEY);
        if (raw) setLanguages(JSON.parse(raw));
      } catch (e) {
        // ignore
      } finally {
        setLoaded(true);
      }
    })();
  }, []);

  const saveLanguages = useCallback(async (langs) => {
    setLanguages(langs);
    await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(langs));
  }, []);

  return (
    <OnboardingContext.Provider
      value={{
        languages,
        loaded,
        needsSetup: loaded && languages.length === 0,
        saveLanguages,
      }}
    >
      {children}
    </OnboardingContext.Provider>
  );
}

export function useOnboarding() {
  const ctx = useContext(OnboardingContext);
  if (!ctx) throw new Error("useOnboarding must be used within OnboardingProvider");
  return ctx;
}
