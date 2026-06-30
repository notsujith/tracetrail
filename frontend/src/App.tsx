import { useCallback, useEffect, useRef, useState } from "react";
import { AlertTriangle, MousePointerClick } from "lucide-react";
import { api, ApiError, LatencyResponse, ServiceSummary, Span } from "./lib/api";
import { serviceColor } from "./lib/format";
import TopBar from "./components/TopBar";
import ServiceList from "./components/ServiceList";
import LatencyPanel from "./components/LatencyPanel";
import SpansTable from "./components/SpansTable";
import TraceWaterfall from "./components/TraceWaterfall";
import { Eyebrow, Panel } from "./components/ui";

const DEMO_KEY = "11111111-1111-1111-1111-111111111111";
const KEY_STORE = "tracetrail.apiKey";

export default function App() {
  const [apiKey, setApiKey] = useState<string>(() => localStorage.getItem(KEY_STORE) || DEMO_KEY);
  const [conn, setConn] = useState<"idle" | "ok" | "error">("idle");
  const [connMessage, setConnMessage] = useState("");
  const [autoRefresh, setAutoRefresh] = useState(false);

  const [services, setServices] = useState<ServiceSummary[]>([]);
  const [servicesLoading, setServicesLoading] = useState(false);
  const [selected, setSelected] = useState<string | null>(null);

  const [latency, setLatency] = useState<LatencyResponse | null>(null);
  const [latencyLoading, setLatencyLoading] = useState(false);
  const [latWindow, setLatWindow] = useState(5);

  const [spans, setSpans] = useState<Span[]>([]);
  const [spansLoading, setSpansLoading] = useState(false);

  const [traceOpen, setTraceOpen] = useState(false);
  const [traceId, setTraceId] = useState<string | null>(null);
  const [traceSpans, setTraceSpans] = useState<Span[] | null>(null);
  const [traceLoading, setTraceLoading] = useState(false);
  const [traceError, setTraceError] = useState<string | null>(null);

  useEffect(() => {
    localStorage.setItem(KEY_STORE, apiKey);
  }, [apiKey]);

  // ---- loaders ---------------------------------------------------------------
  const loadServices = useCallback(async () => {
    if (!apiKey) return;
    setServicesLoading(true);
    try {
      const data = await api.services(apiKey, 60);
      setServices(data);
      setConn("ok");
      setConnMessage("Query Service reachable");
      setSelected((cur) => cur ?? data[0]?.name ?? null);
    } catch (e) {
      const err = e as ApiError;
      setConn("error");
      setConnMessage(err.message);
      setServices([]);
    } finally {
      setServicesLoading(false);
    }
  }, [apiKey]);

  const loadDetail = useCallback(
    async (service: string) => {
      if (!apiKey) return;
      setLatencyLoading(true);
      setSpansLoading(true);
      const now = Date.now();
      const from = now - 59 * 60 * 1000; // < 1h window required by the API
      const [lat, sp] = await Promise.allSettled([
        api.latency(apiKey, service, latWindow),
        api.spans(apiKey, service, from, now, 100),
      ]);
      if (lat.status === "fulfilled") setLatency(lat.value);
      else setLatency(null);
      if (sp.status === "fulfilled") setSpans(sp.value);
      else setSpans([]);
      setLatencyLoading(false);
      setSpansLoading(false);
    },
    [apiKey, latWindow]
  );

  // initial + key change
  useEffect(() => {
    setSelected(null);
    loadServices();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [apiKey]);

  // when a service is selected (or window changes), load its detail
  useEffect(() => {
    if (selected) loadDetail(selected);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected, latWindow]);

  // auto refresh
  const tick = useRef<number | null>(null);
  useEffect(() => {
    if (!autoRefresh) return;
    tick.current = window.setInterval(() => {
      loadServices();
      if (selected) loadDetail(selected);
    }, 10_000);
    return () => {
      if (tick.current) window.clearInterval(tick.current);
    };
  }, [autoRefresh, selected, loadServices, loadDetail]);

  const refreshAll = useCallback(() => {
    loadServices();
    if (selected) loadDetail(selected);
  }, [loadServices, loadDetail, selected]);

  const openTrace = useCallback(
    async (id: string) => {
      setTraceId(id);
      setTraceOpen(true);
      setTraceSpans(null);
      setTraceError(null);
      setTraceLoading(true);
      try {
        const data = await api.trace(apiKey, id);
        setTraceSpans(data);
      } catch (e) {
        setTraceError((e as ApiError).message);
      } finally {
        setTraceLoading(false);
      }
    },
    [apiKey]
  );

  const anyLoading = servicesLoading || latencyLoading || spansLoading;
  const showConnError = conn === "error";

  return (
    <div className="grid-bg min-h-screen">
      <TopBar
        apiKey={apiKey}
        onApiKey={setApiKey}
        conn={conn}
        connMessage={connMessage}
        loading={anyLoading}
        autoRefresh={autoRefresh}
        onToggleAutoRefresh={() => setAutoRefresh((v) => !v)}
        onRefresh={refreshAll}
      />

      <main className="relative z-10 mx-auto max-w-[1400px] px-5 py-6">
        {showConnError && (
          <Panel className="mb-5 flex items-start gap-3 border-error/30 bg-error/5 p-4">
            <AlertTriangle size={18} className="mt-0.5 shrink-0 text-error" />
            <div>
              <p className="text-sm font-medium text-text">Can't read telemetry</p>
              <p className="mt-0.5 text-xs text-muted">
                {connMessage}. Confirm the Query Service is up on{" "}
                <span className="font-mono text-faint">:8080</span> and the tenant key is valid, then refresh.
              </p>
            </div>
          </Panel>
        )}

        <div className="grid grid-cols-1 gap-5 lg:grid-cols-[300px_minmax(0,1fr)]">
          {/* left rail */}
          <aside className="lg:sticky lg:top-[88px] lg:max-h-[calc(100vh-108px)]">
            <ServiceList
              services={services}
              selected={selected}
              onSelect={setSelected}
              loading={servicesLoading}
            />
          </aside>

          {/* main column */}
          <section className="min-w-0 space-y-5">
            {selected ? (
              <>
                <div className="flex items-center gap-3">
                  <span className="h-3 w-3 rounded-sm" style={{ background: serviceColor(selected) }} />
                  <h1 className="font-mono text-lg font-semibold tracking-tight text-text">{selected}</h1>
                </div>
                <LatencyPanel
                  latency={latency}
                  loading={latencyLoading}
                  window={latWindow}
                  onWindow={setLatWindow}
                />
                <SpansTable
                  spans={spans}
                  loading={spansLoading}
                  service={selected}
                  onOpenTrace={openTrace}
                />
              </>
            ) : (
              <Panel className="flex flex-col items-center justify-center px-6 py-20 text-center">
                <MousePointerClick size={22} className="mb-3 text-faint" />
                <Eyebrow>Select a service</Eyebrow>
                <p className="mt-2 max-w-sm text-sm text-muted">
                  Pick a service on the left to see its latency percentiles, recent spans, and full
                  trace waterfalls.
                </p>
              </Panel>
            )}
          </section>
        </div>

        <footer className="mt-10 flex items-center justify-between border-t border-line pt-4 text-[11px] text-faint">
          <span className="font-mono">TraceTrail · Query Service read API</span>
          <span className="font-mono">tip: click any span to open its trace ↗</span>
        </footer>
      </main>

      <TraceWaterfall
        open={traceOpen}
        traceId={traceId}
        spans={traceSpans}
        loading={traceLoading}
        error={traceError}
        onClose={() => setTraceOpen(false)}
      />
    </div>
  );
}
