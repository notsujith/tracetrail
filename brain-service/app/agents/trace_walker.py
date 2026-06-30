from langchain.agents import create_agent

from app.agents import incident_brief
from app.graph.state import InvestigationState
from app.llm import make_llm
from app.tools.query_api import TRACE_TOOLS

TRACE_PROMPT = """You are the Trace Walker. Fetch the full trace and find the
DEEPEST failed span (status_code=2) — the leaf where the failure originated, not
a parent that merely propagated it. Name the service and operation of that
span."""

_agent = create_agent(model=make_llm(), tools=TRACE_TOOLS, system_prompt=TRACE_PROMPT)

def trace_walker_node(state: InvestigationState) -> dict:
    try:
        out = _agent.invoke(
            {"messages": [("user", incident_brief(state))]}
        )
        findings = out["messages"][-1].content

        return {"evidence": [("trace", findings)]}
    except Exception as e:
        return {"evidence": [("trace", f"trace unavailable: {e}")]}