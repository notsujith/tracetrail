import React from "react";

export function Eyebrow({ children }: { children: React.ReactNode }) {
  return (
    <div className="text-[10px] font-semibold uppercase tracking-[0.18em] text-muted">{children}</div>
  );
}

export function StatusDot({ tone }: { tone: "ok" | "error" | "unset" | "idle" }) {
  const map = {
    ok: "bg-ok",
    error: "bg-error",
    unset: "bg-muted",
    idle: "bg-line2",
  } as const;
  return (
    <span className="relative inline-flex h-2 w-2">
      <span className={`absolute inline-flex h-2 w-2 rounded-full ${map[tone]} ${tone === "ok" ? "animate-pulseDot" : ""}`} />
    </span>
  );
}

export function StatusBadge({ tone, label }: { tone: "ok" | "error" | "unset"; label: string }) {
  const map = {
    ok: "text-ok border-ok/30 bg-ok/10",
    error: "text-error border-error/30 bg-error/10",
    unset: "text-muted border-line2 bg-surface2",
  } as const;
  return (
    <span className={`inline-flex items-center rounded-md border px-1.5 py-0.5 font-mono text-[10px] font-semibold tracking-wide ${map[tone]}`}>
      {label}
    </span>
  );
}

export function Panel({
  children,
  className = "",
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <div className={`relative rounded-xl border border-line bg-surface/80 shadow-panel backdrop-blur-sm ${className}`}>
      {children}
    </div>
  );
}

export function Spinner({ label }: { label?: string }) {
  return (
    <div className="flex items-center gap-2 text-sm text-muted">
      <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-line2 border-t-signal" />
      {label}
    </div>
  );
}
