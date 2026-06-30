# TraceTrail — Frontend (Telemetry Console)

A React + Vite + Tailwind dashboard for the TraceTrail **Query Service** read API.
It lists reporting services, shows latency percentiles, streams recent spans, and
renders full **trace waterfalls** when you click any span.

## Run (dev)

```bash
npm install
npm run dev          # http://localhost:5173
```

The dev server proxies `/api/*` → `http://localhost:8080` (the Query Service), so
the browser stays same-origin and **no CORS is involved locally**. Start the
backend first (see the repo root README), then open the app.

Default tenant key is the seeded `demo-tenant` (`11111111-…`). Change it in the
top bar; it's remembered in `localStorage`.

## Build (static)

```bash
npm run build        # outputs dist/
npm run preview      # serve the build on http://localhost:4173
```

When served as a static bundle there is no proxy, so set `VITE_QUERY_BASE` to the
Query Service URL and make sure that origin is in the backend CORS allow-list
(`tracetrail.cors.allowed-origins`, default already includes :5173 and :4173).

## Contract this UI speaks

| Endpoint | Used for |
|---|---|
| `GET /services?lookbackMinutes=60` | service list (name, lastSeen, spanCount) |
| `GET /services/{name}/latency?windowMinutes=5` | p50 / p95 / p99 ladder |
| `GET /services/{name}/spans?from&to&limit` | recent spans table (window < 1h) |
| `GET /traces/{traceId}` | trace waterfall |

Auth header on every call: `X-Tenant-API-Key: <uuid>`.

## Run inside Docker (no Node needed)

The repo's `docker compose up --build` builds this app and serves it from nginx on
**http://localhost:3000**, proxying `/api/*` to the `query` container. That path is
same-origin, so CORS does not apply. See the root README.
