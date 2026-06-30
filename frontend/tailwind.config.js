/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        // "Telemetry Console" palette — deep ink-navy, not pure black.
        ink:      "#0B0E14",
        surface:  "#121723",
        surface2: "#1A2030",
        line:     "#283041",
        line2:    "#374155",
        text:     "#E6EAF2",
        muted:    "#8A94A8",
        faint:    "#5A6378",
        signal:   "#38BDF8", // cyan — primary data
        ok:       "#34D399", // green
        warn:     "#FBBF24", // amber
        error:    "#FB7185", // rose
        accent:   "#A78BFA", // violet
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
        mono: ["'JetBrains Mono'", "ui-monospace", "SFMono-Regular", "monospace"],
      },
      boxShadow: {
        panel: "0 1px 0 0 rgba(255,255,255,0.03) inset, 0 8px 30px -12px rgba(0,0,0,0.6)",
        glow: "0 0 0 1px rgba(56,189,248,0.25), 0 0 24px -6px rgba(56,189,248,0.35)",
      },
      keyframes: {
        sweep: { "0%": { transform: "translateX(-100%)" }, "100%": { transform: "translateX(300%)" } },
        fadein: { from: { opacity: 0, transform: "translateY(4px)" }, to: { opacity: 1, transform: "translateY(0)" } },
        pulseDot: { "0%,100%": { opacity: 1 }, "50%": { opacity: 0.35 } },
      },
      animation: {
        sweep: "sweep 1.6s ease-in-out infinite",
        fadein: "fadein 0.25s ease-out both",
        pulseDot: "pulseDot 1.6s ease-in-out infinite",
      },
    },
  },
  plugins: [],
};
