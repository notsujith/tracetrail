from langchain.agents import create_agent
from app.agents import incident_brief
from app.graph.state import InvestigationState
from app.llm import make_llm
from app.tools.query_api import METRICS_TOOLS

METRICS_PROMPT = """You are the Metrics Analyst. Fetch latency and error-rate for
the service and state whether this is a latency regression, an error-rate
incident, or neither. Report specific p99 and error-rate numbers. If nothing is
anomalous, say so explicitly."""

_agent = create_agent(make_llm(), METRICS_TOOLS, system_prompt=METRICS_PROMPT)

def metrics_analyst_node(state: InvestigationState) -> dict:
    try:
        out = _agent.invoke(
            {"messages": [("user", incident_brief(state))]}
        )

        findings = out["messages"][-1].content

        return {"evidence": [("metrics", findings)]}
    except Exception as e:
        return {"evidence": [("metrics", f"metrics unavailable: {e}")]}