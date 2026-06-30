import { Boxes } from "lucide-react";
import { ServiceSummary } from "../lib/api";
import { fmtCount, relTime, serviceColor } from "../lib/format";
import { Eyebrow } from "./ui";

interface Props {
  services: ServiceSummary[];
  selected: string | null;
  onSelect: (name: string) => void;
  loading: boolean;
}

export default function ServiceList({ services, selected, onSelect, loading }: Props) {
  const max = Math.max(1, ...services.map((s) => s.spanCount));

  return (
    <div className="flex h-full flex-col">
      <div className="flex items-center justify-between px-1 pb-3">
        <Eyebrow>Services</Eyebrow>
        <span className="font-mono text-[11px] text-muted">{services.length}</span>
      </div>

      {loading && services.length === 0 ? (
        <div className="space-y-2">
          {[0, 1, 2].map((i) => (
            <div key={i} className="h-[58px] animate-pulse rounded-lg border border-line bg-surface2/50" />
          ))}
        </div>
      ) : services.length === 0 ? (
        <div className="rounded-lg border border-dashed border-line bg-surface/40 px-4 py-8 text-center">
          <Boxes size={20} className="mx-auto mb-2 text-faint" />
          <p className="text-sm text-muted">No services reporting</p>
          <p className="mt-1 text-xs text-faint">
            Start the demo apps or send OTLP traffic to the ingestion service, then refresh.
          </p>
        </div>
      ) : (
        <div className="space-y-1.5 overflow-y-auto pr-0.5">
          {services.map((s) => {
            const active = s.name === selected;
            const c = serviceColor(s.name);
            const pct = Math.max(4, (s.spanCount / max) * 100);
            return (
              <button
                key={s.name}
                onClick={() => onSelect(s.name)}
                className={`group relative w-full overflow-hidden rounded-lg border px-3 py-2.5 text-left transition ${
                  active
                    ? "border-signal/50 bg-surface shadow-glow"
                    : "border-line bg-surface2/60 hover:border-line2 hover:bg-surface2"
                }`}
              >
                <span
                  className="absolute left-0 top-0 h-full w-[3px]"
                  style={{ background: c, opacity: active ? 1 : 0.55 }}
                />
                <div className="flex items-baseline justify-between gap-2">
                  <span className="truncate font-mono text-[13px] font-medium text-text">{s.name}</span>
                  <span className="tabular shrink-0 font-mono text-[13px] font-semibold" style={{ color: c }}>
                    {fmtCount(s.spanCount)}
                  </span>
                </div>
                <div className="mt-1.5 flex items-center justify-between gap-2">
                  <div className="h-1 flex-1 overflow-hidden rounded-full bg-line">
                    <div className="h-full rounded-full transition-all" style={{ width: `${pct}%`, background: c }} />
                  </div>
                  <span className="shrink-0 font-mono text-[10px] text-muted">{relTime(s.lastSeen)}</span>
                </div>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
