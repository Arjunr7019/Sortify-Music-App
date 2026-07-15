// Shared glass panel styling, translated from:
//   background: rgba(255, 255, 255, 0.21);
//   border-radius: 16px;
//   box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
//   backdrop-filter: blur(14.6px);
//   border: 1px solid rgba(255, 255, 255, 0.3);
import { Platform } from "react-native";

export const GLASS_BG = "rgba(255, 255, 255, 0.3)";
export const GLASS_BORDER = "rgba(255,255,255,0.3)";
export const GLASS_RADIUS = 16;
// expo-blur's `intensity` (0-100) isn't a literal px value, but this sits in
// the same light-to-moderate blur range as backdrop-filter: blur(14.6px).
export const GLASS_BLUR_INTENSITY = 65;

// Apply to the OUTER (non-clipped) wrapper of a glass panel so the shadow
// isn't cut off by the inner overflow:hidden/borderRadius layer.
export const glassShadow = {
  shadowColor: "#000",
  shadowOffset: { width: 0, height: 4 },
  shadowOpacity: 0.1,
  shadowRadius: 30,
  ...Platform.select({ android: { elevation: 8 }, default: {} }),
};

export default {
  GLASS_BG,
  GLASS_BORDER,
  GLASS_RADIUS,
  GLASS_BLUR_INTENSITY,
  glassShadow,
};
