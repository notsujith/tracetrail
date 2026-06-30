// Telemetry formatting helpers — duration, time, OTLP enums, colors.

const SPAN_PALETTE = ["#38BDF8", "#A78BFA", "#34D399", "#FBBF24", "#FB7185", "#22D3EE", "#F472B6", "#4ADE80"];

/** Stable color per service name (hash -> palette). */
export function serviceColor(name: string): string {
  let h = 0;
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) >>> 0;
  return SPAN_PALETTE[h % SPAN_PALETTE.length];
}

/** Nanoseconds -> milliseconds (number). */
export const nanoToMs = (n: number) => n / 1_000_000;

/** Human duration from milliseconds. */
export function fmtMs(ms: number): string {
  if (!isFinite(ms)) return "—";
  if (ms < 1) return `${(ms * 1000).toFixed(0)}µs`;
  if (ms < 1000) return `${ms.toFixed(ms < 10 ? 2 : ms < 100 ? 1 : 0)}ms`;
  return `${(ms / 1000).toFixed(2)}s`;
}

export const fmtDurNano = (n: number) => fmtMs(nanoToMs(n));

/** Compact integer (1.2k, 3.4M). */
export function fmtCount(n: number): string {
  if (n < 1000) return `${n}`;
  if (n < 1_000_000) return `${(n / 1000).toFixed(n < 10_000 ? 1 : 0)}k`;
  return `${(n / 1_000_000).toFixed(1)}M`;
}

/** "3m ago" relative time from an ISO-ish LocalDateTime string (assumed UTC). */
export function relTime(iso: string): string {
  const t = Date.parse(iso.endsWith("Z") ? iso : iso + "Z");
  if (isNaN(t)) return iso;
  const s = Math.max(0, (Date.now() - t) / 1000);
  if (s < 5) return "just now";
  if (s < 60) return `${Math.floor(s)}s ago`;
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

/** Clock time from epoch nanoseconds. */
export function clockFromNano(nano: number): string {
  const d = new Date(nanoToMs(nano));
  return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

export const SPAN_KIND: Record<number, string> = {
  0: "UNSPECIFIED",
  1: "INTERNAL",
  2: "SERVER",
  3: "CLIENT",
  4: "PRODUCER",
  5: "CONSUMER",
};

export function statusLabel(code: number): { label: string; tone: "ok" | "error" | "unset" } {
  if (code === 2) return { label: "ERROR", tone: "error" };
  if (code === 1) return { label: "OK", tone: "ok" };
  return { label: "UNSET", tone: "unset" };
}

/** attributes_json is stored as a JSON string; parse it leniently into entries. */
export function parseAttributes(raw: string | null | undefined): [string, string][] {
  if (!raw) return [];
  try {
    const obj = JSON.parse(raw);
    if (obj && typeof obj === "object") {
      return Object.entries(obj).map(([k, v]) => [k, typeof v === "string" ? v : JSON.stringify(v)]);
    }
  } catch {
    /* not JSON, show raw */
  }
  return [["raw", raw]];
}
