import React from "react";
import { StyleSheet, View } from "react-native";
import { BlurView } from "expo-blur";
import { GLASS_BG, GLASS_BORDER, GLASS_RADIUS, GLASS_BLUR_INTENSITY, glassShadow } from "../theme/glass";

/**
 * Standard glassmorphism container used across Sortify, matching:
 *   background: rgba(255,255,255,0.21); border-radius: 16px;
 *   box-shadow: 0 4px 30px rgba(0,0,0,0.1); backdrop-filter: blur(14.6px);
 *   border: 1px solid rgba(255,255,255,0.3);
 *
 * `style` sizes/positions the whole panel (width, flex, margin...).
 * The drop shadow lives on this outer, non-clipped wrapper; the blur,
 * fill, border and radius live on an inner wrapper with overflow:hidden
 * so the shadow itself never gets clipped.
 */
export default function GlassView({
  children,
  style,
  intensity = GLASS_BLUR_INTENSITY,
  radius = GLASS_RADIUS,
  border = true,
  shadow = true,
  contentStyle,
}) {
  return (
    <View style={[shadow && glassShadow, style]}>
      <View style={{ borderRadius: radius, overflow: "hidden" }}>
        <BlurView
          intensity={intensity}
          tint="light"
          style={[
            styles.blur,
            {
              borderRadius: radius,
              borderWidth: border ? 1 : 0,
              borderColor: GLASS_BORDER,
            },
            contentStyle,
          ]}
        >
          {children}
        </BlurView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  blur: {
    backgroundColor: GLASS_BG,
    padding: 0,
  },
});
