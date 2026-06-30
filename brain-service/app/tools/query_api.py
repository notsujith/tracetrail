import time
import httpx
from langchain_core.tools import tool

from app.settings import setting as st

def _get(path: str, params:dict | None = None) -> dict:
    url = f"{st.query_service_url}{path}"
    headers = {"X-Tenant-API-Key": st.tenant_api_key}
    res = httpx.get(url=url, params=params, headers=headers, timeout=10.0)
    res.raise_for_status() # raises httpx status error on non 2XX status code
    return res.json()


@tool
def get_error_logs(service: str, window_minutes: int) -> dict:
    """Fetch error logs and error spans for a service over a recent time window.
    Use this to find error spikes and the dominant failing endpoint."""
    to_ms = int(time.time() * 1000)
    from_ms = to_ms - window_minutes * 60_000

    return _get(path=f"/api/v1/services/{service}/spans",
                params={"from": from_ms, "to": to_ms, "limit": 1000}
                )


@tool
def get_service_latency(service: str, window_minutes: int) -> dict:
    """Fetch p50/p95/p99 latency and error rate for a service.
    Use this to detect latency regressions or error-rate anomalies."""
    return _get(path=f"/api/v1/services/{service}/latency",
                params= {"windowMinutes": window_minutes}
                )

@tool
def get_trace(trace_id: str) -> dict:
    """Fetch a full distributed trace by id.
    Use this to find the deepest failed span (status_code=2) in a request."""
    return _get(path=f"/api/v1/traces/{trace_id}")



LOG_TOOLS = [get_error_logs]
METRICS_TOOLS = [get_service_latency]
TRACE_TOOLS = [get_trace]
