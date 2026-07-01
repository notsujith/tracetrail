"""Golden cases for evaluating the Brain's root-cause synthesizer."""

from dataclasses import dataclass


@dataclass
class EvalCase:
    id: str
    incident: dict
    evidence: list[dict]
    reference: str

    def summary(self) -> str:
        return self.incident.get("summary", "")

    def contexts(self) -> list[str]:
        return [str(item) for item in self.evidence]


CASES: list[EvalCase] = [
    EvalCase(
        id="payment-timeout",
        incident={
            "service": "cart-service",
            "window": "15m",
            "summary": "checkout error rate jumped to 30% in the last 15 minutes",
        },
        evidence=[
            {
                "agent": "metrics_analyst",
                "finding": "cart-service p99 latency rose from 120ms to 2400ms; "
                           "error_rate 0.31 over the window",
            },
            {
                "agent": "trace_walker",
                "finding": "deepest failed span is payment-service POST /charge "
                           "with status_code=2, duration 2000ms (client timeout)",
            },
            {
                "agent": "log_detective",
                "finding": "payment-service logs show 'upstream connect timeout' "
                           "spikes correlated with the error window",
            },
        ],
        reference="payment-service is timing out on POST /charge, and cart-service "
                  "surfaces those timeouts as checkout errors; the root cause is "
                  "downstream in payment-service, not cart-service itself.",
    ),
    EvalCase(
        id="inventory-slow-query",
        incident={
            "service": "inventory-service",
            "window": "10m",
            "summary": "inventory lookups intermittently slow, some requests exceed 1s",
        },
        evidence=[
            {
                "agent": "metrics_analyst",
                "finding": "inventory-service p95 latency 950ms, p99 1600ms; "
                           "error_rate near zero (0.004)",
            },
            {
                "agent": "trace_walker",
                "finding": "slow spans are all inventory-service GET /stock with a "
                           "single long child span 'db.query' ~900ms",
            },
        ],
        reference="inventory-service itself is slow on GET /stock due to a slow "
                  "database query (db.query dominates span time); this is a "
                  "latency regression, not an error/outage, and is local to "
                  "inventory-service.",
    ),
    EvalCase(
        id="sparse-signal",
        incident={
            "service": "cart-service",
            "window": "5m",
            "summary": "one user reported a single failed checkout",
        },
        evidence=[
            {
                "agent": "metrics_analyst",
                "finding": "cart-service error_rate 0.002, latency nominal; "
                           "no anomaly detected in the window",
            },
        ],
        reference="the evidence is sparse and shows no systemic anomaly; a single "
                  "isolated failure cannot be attributed to a specific root cause "
                  "with confidence, so the correct answer is a low-confidence "
                  "'insufficient signal' conclusion.",
    ),
]
