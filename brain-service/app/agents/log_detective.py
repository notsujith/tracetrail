from langchain.agents import create_agent

from app.agents import incident_brief
from app.graph.state import InvestigationState
from app.llm import make_llm
from app.tools.query_api import LOG_TOOLS

LOG_PROMPT = """You are the Log Detective. Fetch error logs for the service in the
window. Identify whether there is an error spike, which endpoint dominates the
errors, and the severity. Report the error rate and the top failing endpoint. If
a representative failing trace_id appears, include it."""

_agent = create_agent(model=make_llm(), tools=LOG_TOOLS, system_prompt=LOG_PROMPT)

def log_detective_node(state: InvestigationState) -> dict:
    try:
        out = _agent.invoke(
            {"messages": [("user", incident_brief(state))]}
        )
        findings = out["messages"][-1].content

        return {"evidence": [("logs", findings)]}
    except Exception as e:
        return {"evidence": [("logs", f"logs unavailable: {e}")]}