import { LatencyResponse } from "../lib/api";
import { fmtMs } from "../lib/format";
import { Eyebrow, Panel, Spinner } from "./ui";

const WINDOWS = [1, 5, 15, 30, 60];

interface Props {
  latency: LatencyResponse | null;
  loading: boolean;
  window: number;
  onWindow: (m: number) => void;
}

function Rung({ label, value, max, color }: { label: string; value: number; max: number; color: string }) {
  const pct = max > 0 ? Math.max(2, (value / max) * 100) : 0;
  return (
    <div className="flex items-center gap-3">
      <span className="w-9 shrink-0 font-mono text-xs font-semibold" style={{ color }}>
        {label}
      </span>
      <div className="relative h-7 flex-1 overflow-hidden rounded-md bg-surface2">
        <div
          className="absolute inset-y-0 left-0 rounded-md transition-all duration-500"
          style={{ width: `${pct}%`, background: `linear-gradient(90deg, ${color}40, ${color})` }}
        />
        <span className="tabular absolute inset-y-0 right-2.5 flex items-center font-mono text-xs font-semibold text-text">
          {fmtMs(value)}
        </span>
      </div>
    </div>
  );
}

export default function LatencyPanel({ latency, loading, window, onWindow }: Props) {
  const max = latency ? Math.max(latency.p99, latency.p95, latency.p50, 0.001) : 1;
  const empty = latency && latency.count === 0;

  return (
    <Panel className="p-4">
      <div className="mb-3.5 flex items-center justify-between">
        <div>
          <Eyebrow>Latency percentiles</Eyebrow>
          <p className="mt-0.5 font-mono text-[11px] text-faint">
            {latency ? `${latency.count.toLocaleString()} spans · last ${latency.windowMinutes}m` : "—"}
          </p>
        </div>
        <div className="flex items-center gap-1 rounded-lg border border-line bg-surface2 p-0.5">
          {WINDOWS.map((m) => (
            <button
              key={m}
              onClick={() => onWindow(m)}
              className={`rounded-md px-2 py-1 font-mono text-[11px] transition ${
                m === window ? "bg-signal/15 text-signal" : "text-muted hover:text-text"
              }`}
            >
              {m}m
            </button>
          ))}
        </div>
      </div>

      {loading && !latency ? (
        <div className="py-6">
          <Spinner label="Computing percentiles…" />
        </div>
      ) : empty ? (
        <p className="py-6 text-center text-sm text-muted">No spans in this window. Try a wider window.</p>
      ) : latency ? (
        <div className="space-y-2.5">
          <Rung label="p50" value={latency.p50} max={max} color="#38BDF8" />
          <Rung label="p95" value={latency.p95} max={max} color="#FBBF24" />
          <Rung label="p99" value={latency.p99} max={max} color="#FB7185" />
        </div>
      ) : null}
    </Panel>
  );
}
