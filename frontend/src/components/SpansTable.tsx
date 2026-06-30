import { ChevronRight } from "lucide-react";
import { Span } from "../lib/api";
import { clockFromNano, fmtDurNano, nanoToMs, SPAN_KIND, statusLabel } from "../lib/format";
import { Eyebrow, Panel, Spinner, StatusBadge } from "./ui";

interface Props {
  spans: Span[];
  loading: boolean;
  service: string | null;
  onOpenTrace: (traceId: string) => void;
}

export default function SpansTable({ spans, loading, service, onOpenTrace }: Props) {
  const maxDur = Math.max(1, ...spans.map((s) => nanoToMs(s.durationNano)));

  return (
    <Panel className="overflow-hidden">
      <div className="flex items-center justify-between border-b border-line px-4 py-3">
        <Eyebrow>Recent spans</Eyebrow>
        <span className="font-mono text-[11px] text-muted">{service ? `${spans.length} · last 60m` : "—"}</span>
      </div>

      {loading && spans.length === 0 ? (
        <div className="p-6">
          <Spinner label="Loading spans…" />
        </div>
      ) : spans.length === 0 ? (
        <div className="px-4 py-10 text-center">
          <p className="text-sm text-muted">No spans in the last hour</p>
          <p className="mt-1 text-xs text-faint">Spans appear here as the selected service emits traces.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="text-left font-mono text-[10px] uppercase tracking-wider text-faint">
                <th className="px-4 py-2 font-medium">Time</th>
                <th className="px-2 py-2 font-medium">Span</th>
                <th className="px-2 py-2 font-medium">Kind</th>
                <th className="px-2 py-2 font-medium">Status</th>
                <th className="px-2 py-2 text-right font-medium">Duration</th>
                <th className="w-7 px-2 py-2" />
              </tr>
            </thead>
            <tbody>
              {spans.map((s) => {
                const st = statusLabel(s.statusCode);
                const durMs = nanoToMs(s.durationNano);
                const w = Math.max(3, (durMs / maxDur) * 100);
                return (
                  <tr
                    key={`${s.spanId}-${s.timeUnixNano}`}
                    onClick={() => onOpenTrace(s.traceId)}
                    className="group cursor-pointer border-t border-line/60 transition hover:bg-surface2/60"
                    title={`Open trace ${s.traceId}`}
                  >
                    <td className="whitespace-nowrap px-4 py-2.5 font-mono text-xs text-muted">
                      {clockFromNano(s.timeUnixNano)}
                    </td>
                    <td className="px-2 py-2.5">
                      <div className="font-mono text-[13px] text-text">{s.spanName}</div>
                      <div className="font-mono text-[10px] text-faint">{s.traceId.slice(0, 16)}…</div>
                    </td>
                    <td className="px-2 py-2.5">
                      <span className="font-mono text-[11px] text-muted">{SPAN_KIND[s.kind] ?? s.kind}</span>
                    </td>
                    <td className="px-2 py-2.5">
                      <StatusBadge tone={st.tone} label={st.label} />
                    </td>
                    <td className="px-2 py-2.5">
                      <div className="flex items-center justify-end gap-2">
                        <div className="hidden h-1.5 w-24 overflow-hidden rounded-full bg-surface2 sm:block">
                          <div
                            className="h-full rounded-full"
                            style={{
                              width: `${w}%`,
                              background: st.tone === "error" ? "#FB7185" : "#38BDF8",
                            }}
                          />
                        </div>
                        <span className="tabular w-16 text-right font-mono text-xs font-semibold text-text">
                          {fmtDurNano(s.durationNano)}
                        </span>
                      </div>
                    </td>
                    <td className="px-2 py-2.5">
                      <ChevronRight size={14} className="text-faint transition group-hover:translate-x-0.5 group-hover:text-signal" />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </Panel>
  );
}
