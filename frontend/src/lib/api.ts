// ---------------------------------------------------------------------------
// TraceTrail Query Service client.
//
// Contract (from query-service/.../controller/QueryController.java):
//   GET /services?lookbackMinutes=60
//       -> 200 [{ name, lastSeen, spanCount }]
//   GET /services/{name}/spans?from={epochMillis}&to={epochMillis}&limit=100
//       -> 200 [Span]   (window must be < 1 hour, else 400 text)
//   GET /services/{name}/latency?windowMinutes=5
//       -> 200 { p50, p95, p99, count, windowMinutes }   (values in ms)
//   GET /traces/{traceId}
//       -> 200 [Span] ordered by time asc, or 404 text
//
// Auth: every request carries header  X-Tenant-API-Key: <uuid>
//
// In dev we hit "/api/*" which Vite proxies to the Query Service (same-origin,
// so no CORS). In a built/static deploy, set VITE_QUERY_BASE to the service URL;
// the backend's CORS config allows the configured origins.
// ---------------------------------------------------------------------------

const RAW_BASE = (import.meta.env.VITE_QUERY_BASE as string | undefined) ?? "/api";
export const API_BASE = RAW_BASE.replace(/\/$/, "");

export interface ServiceSummary {
  name: string;
  lastSeen: string; // ISO-ish LocalDateTime, e.g. "2026-06-11T10:42:03"
  spanCount: number;
}

export interface Span {
  timeUnixNano: number;
  traceId: string;
  spanId: string;
  parentSpanId: string | null;
  tenantId: number;
  serviceName: string;
  spanName: string;
  kind: number; // OTLP SpanKind: 1 INTERNAL, 2 SERVER, 3 CLIENT, 4 PRODUCER, 5 CONSUMER
  endTimeUnixNano: number;
  durationNano: number;
  statusCode: number; // OTLP: 0 UNSET, 1 OK, 2 ERROR
  attributesJson: string;
}

export interface LatencyResponse {
  p50: number;
  p95: number;
  p99: number;
  count: number;
  windowMinutes: number;
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

async function request<T>(path: string, apiKey: string): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      headers: { "X-Tenant-API-Key": apiKey },
    });
  } catch {
    throw new ApiError(0, "Can't reach the Query Service. Is it running on :8080?");
  }

  if (!res.ok) {
    let detail = res.statusText;
    try {
      const txt = await res.text();
      if (txt) {
        try {
          detail = (JSON.parse(txt).error as string) ?? txt;
        } catch {
          detail = txt;
        }
      }
    } catch {
      /* keep statusText */
    }
    if (res.status === 401) throw new ApiError(401, "API key rejected. Check the tenant key.");
    throw new ApiError(res.status, detail);
  }

  // 204/empty guard
  const text = await res.text();
  return (text ? JSON.parse(text) : null) as T;
}

export const api = {
  services: (apiKey: string, lookbackMinutes = 60) =>
    request<ServiceSummary[]>(`/services?lookbackMinutes=${lookbackMinutes}`, apiKey),

  spans: (apiKey: string, service: string, fromMs: number, toMs: number, limit = 100) =>
    request<Span[]>(
      `/services/${encodeURIComponent(service)}/spans?from=${fromMs}&to=${toMs}&limit=${limit}`,
      apiKey
    ),

  latency: (apiKey: string, service: string, windowMinutes = 5) =>
    request<LatencyResponse>(
      `/services/${encodeURIComponent(service)}/latency?windowMinutes=${windowMinutes}`,
      apiKey
    ),

  trace: (apiKey: string, traceId: string) =>
    request<Span[]>(`/traces/${encodeURIComponent(traceId)}`, apiKey),
};
