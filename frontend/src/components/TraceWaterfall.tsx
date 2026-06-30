import { useEffect, useMemo, useState } from "react";
import { X, GitBranch } from "lucide-react";
import { Span } from "../lib/api";
import {
  clockFromNano,
  fmtMs,
  nanoToMs,
  parseAttributes,
  serviceColor,
  SPAN_KIND,
  statusLabel,
} from "../lib/format";
import { StatusBadge } from "./ui";

interface Row {
  span: Span;
  depth: number;
  offsetMs: number;
  durMs: number;
}

/** Order spans into a parent->child tree, then flatten depth-first. */
function buildRows(spans: Span[]): { rows: Row[]; t0: number; totalMs: number } {
  if (spans.length === 0) return { rows: [], t0: 0, totalMs: 1 };
  const t0 = Math.min(...spans.map((s) => s.timeUnixNano));
  const tEnd = Math.max(...spans.map((s) => s.endTimeUnixNano));
  const totalMs = Math.max(0.001, nanoToMs(tEnd - t0));

  const byId = new Map(spans.map((s) => [s.spanId, s]));
  const children = new Map<string, Span[]>();
  const roots: Span[] = [];
  for (const s of spans) {
    const parent = s.parentSpanId;
    if (parent && byId.has(parent)) {
      (children.get(parent) ?? children.set(parent, []).get(parent)!).push(s);
    } else {
      roots.push(s);
    }
  }
  const sortByTime = (a: Span, b: Span) => a.timeUnixNano - b.timeUnixNano;
  roots.sort(sortByTime);

  const rows: Row[] = [];
  const walk = (s: Span, depth: number) => {
    rows.push({
      span: s,
      depth,
      offsetMs: nanoToMs(s.timeUnixNano - t0),
      durMs: nanoToMs(s.durationNano),
    });
    (children.get(s.spanId) ?? []).sort(sortByTime).forEach((c) => walk(c, depth + 1));
  };
  roots.forEach((r) => walk(r, 0));
  return { rows, t0, totalMs };
}

interface Props {
  open: boolean;
  traceId: string | null;
  spans: Span[] | null;
  loading: boolean;
  error: string | null;
  onClose: () => void;
}

export default function TraceWaterfall({ open, traceId, spans, loading, error, onClose }: Props) {
  const [selected, setSelected] = useState<string | null>(null);

  useEffect(() => {
    setSelected(null);
  }, [traceId]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && onClose();
    if (open) window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  const { rows, totalMs } = useMemo(() => buildRows(spans ?? []), [spans]);
  const services = useMemo(() => [...new Set((spans ?? []).map((s) => s.serviceName))], [spans]);
  const sel = rows.find((r) => r.span.spanId === selected)?.span ?? null;

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <div className="absolute inset-0 bg-ink/70 backdrop-blur-sm animate-fadein" onClick={onClose} />
      <div className="relative flex h-full w-full max-w-3xl animate-fadein flex-col border-l border-line bg-surface shadow-panel">
        {/* header */}
        <div className="flex items-start justify-between border-b border-line px-5 py-4">
          <div>
            <div className="flex items-center gap-2 text-[10px] font-semibold uppercase tracking-[0.18em] text-muted">
              <GitBranch size={13} className="text-accent" /> Trace waterfall
            </div>
            <div className="mt-1 font-mono text-sm text-text">{traceId}</div>
            {rows.length > 0 && (
              <div className="mt-1 font-mono text-[11px] text-faint">
                {rows.length} spans · {services.length} services · {fmtMs(totalMs)} total
              </div>
            )}
          </div>
          <button
            onClick={onClose}
            aria-label="Close"
            className="rounded-lg border border-line bg-surface2 p-1.5 text-muted transition hover:text-text"
          >
            <X size={16} />
          </button>
        </div>

        {/* service legend */}
        {services.length > 0 && (
          <div className="flex flex-wrap gap-x-4 gap-y-1.5 border-b border-line px-5 py-2.5">
            {services.map((sv) => (
              <span key={sv} className="flex items-center gap-1.5 font-mono text-[11px] text-muted">
                <span className="h-2 w-2 rounded-sm" style={{ background: serviceColor(sv) }} />
                {sv}
              </span>
            ))}
          </div>
        )}

        {/* body */}
        <div className="flex-1 overflow-y-auto">
          {loading ? (
            <div className="space-y-2 p-5">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="h-7 animate-pulse rounded bg-surface2/60" style={{ marginLeft: `${(i % 3) * 18}px` }} />
              ))}
            </div>
          ) : error ? (
            <div className="px-5 py-12 text-center">
              <p className="text-sm text-error">{error}</p>
              <p className="mt-1 text-xs text-faint">
                The trace may have rolled out of the retention window. Pick a more recent span.
              </p>
            </div>
          ) : (
            <div className="py-2">
              {rows.map((r) => {
                const c = serviceColor(r.span.serviceName);
                const st = statusLabel(r.span.statusCode);
                const isErr = st.tone === "error";
                const left = Math.min(99, (r.offsetMs / totalMs) * 100);
                const width = Math.max(0.6, Math.min(100 - left, (r.durMs / totalMs) * 100));
                const active = r.span.spanId === selected;
                return (
                  <button
                    key={`${r.span.spanId}-${r.span.timeUnixNano}`}
                    onClick={() => setSelected(active ? null : r.span.spanId)}
                    className={`block w-full px-5 py-1 text-left transition hover:bg-surface2/50 ${active ? "bg-surface2/70" : ""}`}
                  >
                    <div className="flex items-center gap-3">
                      <div className="flex min-w-0 items-center gap-2" style={{ width: "38%", paddingLeft: r.depth * 14 }}>
                        {r.depth > 0 && <span className="text-faint">└</span>}
                        <span className="h-2.5 w-2.5 shrink-0 rounded-sm" style={{ background: c }} />
                        <span className="truncate font-mono text-[12px] text-text">{r.span.spanName}</span>
                        {isErr && <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-error" />}
                      </div>
                      <div className="relative h-5 flex-1 rounded bg-surface2/40">
                        <div
                          className="absolute inset-y-0 my-auto h-3.5 rounded"
                          style={{
                            left: `${left}%`,
                            width: `${width}%`,
                            background: isErr ? "#FB7185" : c,
                            opacity: active ? 1 : 0.85,
                          }}
                        />
                      </div>
                      <span className="tabular w-16 shrink-0 text-right font-mono text-[11px] font-semibold text-muted">
                        {fmtMs(r.durMs)}
                      </span>
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* selected span detail */}
        {sel && (
          <div className="max-h-[42%] overflow-y-auto border-t border-line bg-ink/40 px-5 py-4 animate-fadein">
            <div className="mb-3 flex items-center justify-between">
              <span className="font-mono text-sm text-text">{sel.spanName}</span>
              <StatusBadge tone={statusLabel(sel.statusCode).tone} label={statusLabel(sel.statusCode).label} />
            </div>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-1.5 font-mono text-[11px]">
              <Detail k="service" v={sel.serviceName} />
              <Detail k="kind" v={SPAN_KIND[sel.kind] ?? String(sel.kind)} />
              <Detail k="span_id" v={sel.spanId} />
              <Detail k="parent" v={sel.parentSpanId || "—"} />
              <Detail k="start" v={clockFromNano(sel.timeUnixNano)} />
              <Detail k="duration" v={fmtMs(nanoToMs(sel.durationNano))} />
            </dl>
            {(() => {
              const attrs = parseAttributes(sel.attributesJson);
              if (attrs.length === 0) return null;
              return (
                <div className="mt-3 border-t border-line/60 pt-3">
                  <div className="mb-1.5 text-[10px] font-semibold uppercase tracking-wider text-faint">attributes</div>
                  <dl className="grid grid-cols-1 gap-y-1 font-mono text-[11px] sm:grid-cols-2 sm:gap-x-6">
                    {attrs.map(([k, v]) => (
                      <Detail key={k} k={k} v={v} />
                    ))}
                  </dl>
                </div>
              );
            })()}
          </div>
        )}
      </div>
    </div>
  );
}

function Detail({ k, v }: { k: string; v: string }) {
  return (
    <div className="flex items-baseline justify-between gap-3 border-b border-line/30 py-0.5">
      <dt className="shrink-0 text-faint">{k}</dt>
      <dd className="truncate text-right text-muted" title={v}>
        {v}
      </dd>
    </div>
  );
}
