import http from "k6/http";
import { check } from "k6";

const QUERY_URL = __ENV.QUERY_URL || "http://localhost:8080";
const API_KEY = __ENV.API_KEY || "11111111-1111-1111-1111-111111111111";
const HEADERS = { "X-Tenant-API-Key": API_KEY };
const SERVICES = ["cart-service", "inventory-service", "payment-service"];

export const options = {
    stages: [
        { duration: "30s", target: 30 },
        { duration: "1m", target: 30 },
        { duration: "30s", target: 60 },
        { duration: "1m", target: 60 },
        { duration: "20s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.02"],
        http_req_duration: ["p(95)<500"],
    },
};

export default function () {
    const service = SERVICES[Math.floor(Math.random() * SERVICES.length)];

    const services = http.get(`${QUERY_URL}/api/v1/services?lookbackMinutes=60`, { headers: HEADERS });
    check(services, { "services 200": (r) => r.status === 200 });

    const latency = http.get(`${QUERY_URL}/api/v1/services/${service}/latency?windowMinutes=5`, { headers: HEADERS });
    check(latency, { "latency 200": (r) => r.status === 200 });

    const to = Date.now();
    const from = to - 5 * 60 * 1000;
    const spans = http.get(
        `${QUERY_URL}/api/v1/services/${service}/spans?from=${from}&to=${to}&limit=100`,
        { headers: HEADERS }
    );
    check(spans, { "spans 200": (r) => r.status === 200 });
}