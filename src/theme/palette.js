// Flat, non-glass palette matching the new light/dark mockups.
// Same accent orange in both modes; everything else swaps.
const accent = "#E8720C";
const accentDark = "#C85F09";

export const lightPalette = {
  mode: "light",
  background: "#FFFFFF",
  surface: "#F2F2F4",       // cards, rows, input fill
  surfaceAlt: "#E7E7EA",    // icon chips, nested elements
  placeholder: "#ABABB0",   // "banner" image placeholders
  border: "#E6E6E9",
  divider: "#ECECEF",

  text: "#131316",
  textSecondary: "#6E6E76",
  textFaint: "#9A9AA1",

  accent,
  accentDark,
  accentOn: "#FFFFFF",      // text/icons drawn on top of the accent color
  accentSoft: "rgba(232,114,12,0.12)",

  danger: "#E8720C",
  success: "#1F9C6B",

  statusBarStyle: "dark",
};

export const darkPalette = {
  mode: "dark",
  background: "#0B0B0D",
  surface: "#1B1B1F",
  surfaceAlt: "#262629",
  placeholder: "#5C5C61",
  border: "#28282C",
  divider: "#222226",

  text: "#FFFFFF",
  textSecondary: "#A5A5AC",
  textFaint: "#75757C",

  accent,
  accentDark,
  accentOn: "#FFFFFF",
  accentSoft: "rgba(232,114,12,0.18)",

  danger: "#E8720C",
  success: "#3FDBA0",

  statusBarStyle: "light",
};

export function getPalette(mode) {
  return mode === "dark" ? darkPalette : lightPalette;
}
