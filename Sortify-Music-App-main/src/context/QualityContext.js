import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { QUALITY_OPTIONS, DEFAULT_QUALITY } from "../api/musicApi";

const STORAGE_KEY = "sortify:audioQuality:v1";
const QualityContext = createContext(null);

export { QUALITY_OPTIONS };

export function QualityProvider({ children }) {
  const [quality, setQualityState] = useState(DEFAULT_QUALITY);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const saved = await AsyncStorage.getItem(STORAGE_KEY);
        if (saved && QUALITY_OPTIONS.includes(saved)) setQualityState(saved);
      } catch (e) {
        // ignore, fall back to default
      } finally {
        setLoaded(true);
      }
    })();
  }, []);

  const setQuality = useCallback((next) => {
    if (!QUALITY_OPTIONS.includes(next)) return;
    setQualityState(next);
    AsyncStorage.setItem(STORAGE_KEY, next).catch(() => {});
  }, []);

  return (
    <QualityContext.Provider value={{ quality, setQuality, loaded }}>
      {children}
    </QualityContext.Provider>
  );
}

export function useQuality() {
  const ctx = useContext(QualityContext);
  if (!ctx) throw new Error("useQuality must be used within QualityProvider");
  return ctx;
}
