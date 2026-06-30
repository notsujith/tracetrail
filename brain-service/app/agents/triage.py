import json

from langchain.agents import create_agent

from app.agents import incident_brief
from app.graph.state import InvestigationState
from app.llm import make_llm

TRIAGE_PROMPT = """You are the Triage router for a distributed-tracing incident investigation.

Your only job: pick which specialist agents should investigate. You do NOT investigate yourself. You do NOT call tools.

Available specialists (this is the FULL menu — use these exact names):
- "log_detective": reads service logs. Pick when the incident mentions errors, exceptions, stack traces, failed requests, or specific error messages.
- "metrics_analyst": reads latency/throughput metrics. Pick when the incident mentions slowness, latency spikes, p95/p99, timeouts, or degraded performance.
- "trace_walker": walks distributed traces span-by-span. Pick when the incident spans multiple services, mentions a request path, or asks where in the call chain something broke.

Rules:
- Pick every specialist that could plausibly help. When unsure, include more rather than fewer.
- Most real incidents need 2-3 specialists. A single specialist is rare.
- Return ONLY a JSON object, no prose, no markdown:
  {"agents": ["log_detective", "metrics_analyst", "trace_walker"]}
- Use only names from the menu above. Invent nothing."""

_agent = create_agent(model=make_llm(json_mode=True),
                      system_prompt=TRIAGE_PROMPT)

VALID_SPECIALISTS = ["log_detective", "metrics_analyst", "trace_walker"]

def triage_node(state: InvestigationState) -> dict:
    try:
        result = _agent.invoke(
            {"messages":[("user", incident_brief(state))]}
        )
        raw = result["messages"][-1].content
        cleaned = raw.strip().removeprefix("```json").removeprefix("```").removesuffix("```").strip()
        agents = json.loads(cleaned).get("agents", [])
        filtered = [a for a in agents if a in VALID_SPECIALISTS]
    except Exception:
        filtered = list(VALID_SPECIALISTS)
    if not filtered:
        filtered = list(VALID_SPECIALISTS)
    return {"selected_agents": filtered}
