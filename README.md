# TraceTrail

> Self-hosted distributed tracing & observability platform with a multi-agent AI investigation engine.

TraceTrail ingests OpenTelemetry telemetry from microservices, stores it in time-partitioned MySQL, serves it through a Redis-cached read API, and uses a **LangGraph multi-agent system** to automatically investigate incidents and surface likely root causes.

Datadog, New Relic, and Honeycomb sell this product for thousands per month. TraceTrail is a focused open implementation of the same idea.

<!-- Replace with a real screenshot/GIF of the frontend + a sample Brain investigation -->
![TraceTrail dashboard](docs/screenshot.png)

---

## Features

- **OTLP ingestion** — OpenTelemetry traces, metrics, and logs over OTLP/HTTP
- **Time-partitioned storage** — MySQL 8.4 with daily `RANGE` partitions for fast time-window queries
- **Cached read path** — Redis-backed query API with percentile latency endpoints
- **AI investigation engine** — five-agent LangGraph "Brain" that triages an incident, fans out to specialist agents (logs, metrics, traces), and synthesizes a root-cause hypothesis
- **Realistic demo workload** — cart / inventory / payment microservices, auto-instrumented via the OTel Java agent, with an intentional 10% payment failure rate
- **Load tested** — k6 smoke, ingest, query, and stress scripts

---

## Architecture

```
                          ┌─────────────────────────────────────────┐
  demo microservices      │              TraceTrail                   │
  (cart / inventory /     │                                           │
   payment)               │   ┌──────────────┐    ┌───────────────┐   │
        │ OTLP            │   │  Ingestion   │───▶│   MySQL 8.4   │   │
        │ (OTel Java      │   │   Service    │    │ (daily RANGE  │   │
        │  agent)         │   │ :4318        │    │  partitions)  │   │
        ▼                 │   └──────────────┘    └───────┬───────┘   │
  ┌──────────────┐        │                               │           │
  │ OTel         │───────▶│   ┌──────────────┐    ┌───────▼───────┐   │
  │ Collector    │ bridge │   │    Query     │◀──▶│    Redis 7    │   │
  │ (http/proto) │        │   │   Service    │    │   (cache)     │   │
  └──────────────┘        │   │ :8080        │    └───────────────┘   │
                          │   └──────▲───────┘                        │
                          │          │ REST (httpx)                   │
                          │   ┌──────┴───────┐                        │
                          │   │    Brain     │  LangGraph 5-agent     │
   React frontend ───────▶│   │   Service    │  investigation engine  │
                          │   │ :8090        │  (Ollama / qwen2.5)    │
                          │   └──────────────┘                        │
                          └─────────────────────────────────────────┘
```

The **Brain is just another client** — it calls the Query Service REST API via `httpx` and has no direct DB access. This keeps the read path as the single source of truth and means the AI layer reuses the exact same API a human would.

### Brain agent graph

| Node | Type | Tools |
|------|------|-------|
| **Triage** | router | none — plain `make_llm(json_mode=True).invoke()` |
| **Log Detective** | specialist | `create_react_agent` + httpx wrappers |
| **Metrics Analyst** | specialist | `create_react_agent` + httpx wrappers |
| **Trace Walker** | specialist | `create_react_agent` + httpx wrappers |
| **Synthesizer** | reasoning | none — plain `.invoke()` |

Parallel fan-out uses `Send(node, state)` from `langgraph.types`; `InvestigationState.evidence` uses an `Annotated[list, add]` reducer so concurrent specialist outputs merge correctly.

---

## Tech stack

| Layer | Tech |
|-------|------|
| Ingestion & Query | Spring Boot 3.3.5, Spring Data JPA, Hibernate (`ddl-auto: validate`), Caffeine, Lombok, Jackson |
| Brain | Python, FastAPI, LangGraph, LangChain, httpx, pydantic-settings |
| LLM inference | Ollama (`qwen2.5:3b`, `nomic-embed-text`) |
| Storage | MySQL 8.4 (daily RANGE partitions), Redis 7 |
| Telemetry | OpenTelemetry, OTel Collector |
| Infra | Docker Compose, OTel Java agent |
| Load testing | k6 |

---

## Quick start

Prereqs: Docker Desktop, and [Ollama](https://ollama.com) running on the host with the models pulled.

```bash
ollama pull qwen2.5:3b
ollama pull nomic-embed-text

git clone https://github.com/notsujith/tracetrail.git
cd tracetrail

cp .env.example .env

docker compose up -d
```

Then:

- Frontend: http://localhost:3000
- Query API: http://localhost:8080/api/v1
- Brain API: http://localhost:8090

Generate demo traffic:

```bash
k6 run loadtests/ingest-load.js
```

---

## Project layout

```
ingestion-service/    Spring Boot OTLP ingestion (:4318)
query-service/        Spring Boot read API + Redis cache (:8080)
brain-service/        Python/LangGraph AI engine (:8090)
demo/                 cart / inventory / payment microservices
frontend/             React dashboard
infra/                docker-compose, OTel Collector config, MySQL init
loadtests/            k6 scripts (smoke, ingest, query, stress)
docs/                 architecture blueprints & implementation references
```

---

## Documentation

- [`docs/big-picture.md`](docs/big-picture.md) — what the system is and why each piece exists
- [`docs/phase-1-blueprint.md`](docs/phase-1-blueprint.md) — backend + frontend build plan
- [`docs/phase-3-blueprint.md`](docs/phase-3-blueprint.md) — Brain Service design

---

## License

MIT — see [LICENSE](LICENSE).
