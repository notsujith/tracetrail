import http from "k6/http";
import { check } from "k6";

const QUERY_URL = __ENV.QUERY_URL || "http://localhost:8080";
const INGEST_URL = __ENV.INGEST_URL || "http://localhost:4318";
const API_KEY = __ENV.API_KEY || "11111111-1111-1111-1111-111111111111";
const HEADERS = { "X-Tenant-API-Key": API_KEY, "Content-Type": "application/json" };
const SERVICES = ["cart-service", "inventory-service", "payment-service"];

export const options = {
    stages: [
        { duration: "1m", target: 100 },
        { duration: "2m", target: 200 },
        { duration: "2m", target: 300 },
        { duration: "1m", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.10"],
        http_req_duration: ["p(95)<2000"],
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
    const start = end - Math.floor(Math.random() * 200 + 10) * 1e6;
    const isError = Math.random() < 0.1;
    return JSON.stringify({
        resourceSpans: [{
            resource: { attributes: [{ key: "service.name", value: { stringValue: service } }] },
            scopeSpans: [{
                scope: { name: "k6-stress" },
                spans: [{
                    traceId: hex(32),
                    spanId: hex(16),
                    name: isError ? "POST /checkout" : "GET /items",
                    kind: 2,
                    startTimeUnixNano: String(start),
                    endTimeUnixNano: String(end),
                    status: { code: isError ? 2 : 1 },
                }],
            }],
        }],
    });
}

export default function () {
    const service = SERVICES[Math.floor(Math.random() * SERVICES.length)];

    if (Math.random() < 0.5) {
        const res = http.post(`${INGEST_URL}/v1/traces`, otlpBody(service), { headers: HEADERS });
        check(res, { "ingest ok": (r) => r.status === 200 });
    } else {
        const res = http.get(`${QUERY_URL}/api/v1/services/${service}/latency?windowMinutes=5`, { headers: HEADERS });
        check(res, { "query ok": (r) => r.status === 200 });
    }
}