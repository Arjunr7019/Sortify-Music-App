// Palette pulled from the Sortify logo (pink -> coral -> amber gradient)
// on a near-black background, matching icon.png / adaptive-icon.png.
export const colors = {
  bgTop: "#150618",
  bgBottom: "#050107",
  surface: "rgba(255,255,255,0.06)",
  surfaceStrong: "rgba(255,255,255,0.10)",
  border: "rgba(255,255,255,0.14)",
  borderStrong: "rgba(255,255,255,0.22)",

  pink: "#E51E7C",
  coral: "#F0603F",
  amber: "#FBB531",

  gradient: ["#E51E7C", "#F0603F", "#FBB531"],
  gradientSoft: ["rgba(229,30,124,0.35)", "rgba(240,96,63,0.35)", "rgba(251,181,49,0.35)"],

  text: "#FFFFFF",
  textDim: "rgba(255,255,255,0.62)",
  textFaint: "rgba(255,255,255,0.38)",

  // Glass panels are a white-tinted, blurred overlay (see theme/glass.js),
  // so anything drawn ON TOP of one needs dark text instead of the white
  // text used on the app's dark background/gradients.
  onGlassText: "#1B0E1F",
  onGlassDim: "rgba(27,14,31,0.68)",
  onGlassFaint: "rgba(27,14,31,0.45)",

  danger: "#F0603F",
  success: "#1F9C6B",
};

export default colors;
