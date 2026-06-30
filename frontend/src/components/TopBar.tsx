import { Activity, RefreshCw, KeyRound, Zap } from "lucide-react";
import { StatusDot } from "./ui";
import { API_BASE } from "../lib/api";

function Logo() {
  return (
    <div className="flex items-center gap-2.5">
      <svg width="30" height="30" viewBox="0 0 32 32" className="shrink-0">
        <rect width="32" height="32" rx="8" fill="#121723" stroke="#283041" />
        <path d="M5 21 L12 12 L18 17 L27 7" stroke="#38BDF8" strokeWidth="2.4" fill="none" strokeLinecap="round" strokeLinejoin="round" />
        <circle cx="5" cy="21" r="2.3" fill="#34D399" />
        <circle cx="12" cy="12" r="1.9" fill="#38BDF8" />
        <circle cx="18" cy="17" r="1.9" fill="#38BDF8" />
        <circle cx="27" cy="7" r="2.3" fill="#A78BFA" />
      </svg>
      <div className="leading-tight">
        <div className="font-semibold tracking-tight">
          Trace<span className="text-signal">Trail</span>
        </div>
        <div className="font-mono text-[10px] uppercase tracking-[0.2em] text-muted">telemetry console</div>
      </div>
    </div>
  );
}

interface Props {
  apiKey: string;
  onApiKey: (k: string) => void;
  conn: "idle" | "ok" | "error";
  connMessage: string;
  loading: boolean;
  autoRefresh: boolean;
  onToggleAutoRefresh: () => void;
  onRefresh: () => void;
}

export default function TopBar({
  apiKey,
  onApiKey,
  conn,
  connMessage,
  loading,
  autoRefresh,
  onToggleAutoRefresh,
  onRefresh,
}: Props) {
  return (
    <header className="sticky top-0 z-30 border-b border-line bg-ink/85 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-[1400px] items-center gap-4 px-5">
        <Logo />

        <div className="ml-2 hidden items-center gap-2 rounded-lg border border-line bg-surface2 px-2.5 py-1.5 md:flex">
          <Activity size={13} className="text-signal" />
          <span className="font-mono text-xs text-muted">{API_BASE}</span>
        </div>

        <div className="flex-1" />

        {/* Tenant key */}
        <div className="flex items-center gap-2 rounded-lg border border-line bg-surface2 px-2.5 py-1.5 focus-within:border-signal/60 focus-within:shadow-glow">
          <KeyRound size={14} className="text-muted" />
          <input
            value={apiKey}
            onChange={(e) => onApiKey(e.target.value.trim())}
            spellCheck={false}
            placeholder="X-Tenant-API-Key"
            aria-label="Tenant API key"
            className="w-[230px] bg-transparent font-mono text-xs text-text outline-none placeholder:text-faint sm:w-[290px]"
          />
        </div>

        {/* Connection status */}
        <div
          className="hidden items-center gap-2 rounded-lg border border-line bg-surface2 px-3 py-1.5 lg:flex"
          title={connMessage}
        >
          <StatusDot tone={conn} />
          <span className="font-mono text-[11px] text-muted">
            {conn === "ok" ? "connected" : conn === "error" ? "no signal" : "idle"}
          </span>
        </div>

        <button
          onClick={onToggleAutoRefresh}
          className={`flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs font-medium transition ${
            autoRefresh
              ? "border-signal/40 bg-signal/10 text-signal"
              : "border-line bg-surface2 text-muted hover:text-text"
          }`}
          title="Auto-refresh every 10s"
        >
          <Zap size={13} className={autoRefresh ? "fill-signal/30" : ""} />
          <span className="hidden sm:inline">{autoRefresh ? "Live" : "Paused"}</span>
        </button>

        <button
          onClick={onRefresh}
          aria-label="Refresh"
          className="flex items-center gap-1.5 rounded-lg border border-line bg-surface2 px-2.5 py-1.5 text-xs font-medium text-text transition hover:border-line2 hover:bg-surface"
        >
          <RefreshCw size={13} className={loading ? "animate-spin text-signal" : ""} />
          <span className="hidden sm:inline">Refresh</span>
        </button>
      </div>
    </header>
  );
}
