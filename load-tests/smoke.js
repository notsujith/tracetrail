import http from "k6/http";
import { check, sleep } from "k6";

const QUERY_URL = __ENV.QUERY_URL || "http://localhost:8080";
const INGEST_URL = __ENV.INGEST_URL || "http://localhost:4318";
const API_KEY = __ENV.API_KEY || "11111111-1111-1111-1111-111111111111";
const HEADERS = { "X-Tenant-API-Key": API_KEY, "Content-Type": "application/json" };

export const options = {
    vus: 1,
    iterations: 5,
    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<1000"],
    },
};

function hex(n) {
    const c = "0123456789abcdef";
    let s = "";
    for (let i = 0; i < n; i++) s += c[Math.floor(Math.random() * 16)];
    return s;
}

function otlpBody(service) {
    const end = Date.now() * 1e6;
    const start = end - 50 * 1e6;
    return JSON.stringify({
        resourceSpans: [{
            resource: { attributes: [{ key: "service.name", value: { stringValue: service } }] },
            scopeSpans: [{
                scope: { name: "k6-smoke" },
                spans: [{
                    traceId: hex(32),
                    spanId: hex(16),
                    name: "GET /smoke",
                    kind: 2,
                    startTimeUnixNano: String(start),
                    endTimeUnixNano: String(end),
                    status: { code: 1 },
                }],
            }],
        }],
    });
}

export default function () {
    const ingest = http.post(`${INGEST_URL}/v1/traces`, otlpBody("cart-service"), { headers: HEADERS });
    check(ingest, { "ingest 200": (r) => r.status === 200 });

    const services = http.get(`${QUERY_URL}/api/v1/services?lookbackMinutes=60`, { headers: HEADERS });
    check(services, { "services 200": (r) => r.status === 200 });

    const latency = http.get(`${QUERY_URL}/api/v1/services/cart-service/latency?windowMinutes=5`, { headers: HEADERS });
    check(latency, { "latency 200": (r) => r.status === 200 });

    sleep(1);
}