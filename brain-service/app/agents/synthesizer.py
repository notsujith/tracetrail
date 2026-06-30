import json

from langchain.agents import create_agent

from app.agents import incident_brief
from app.graph.state import InvestigationState
from app.llm import make_llm

SYNTHESIZER_PROMPT = """You are the Synthesizer in a distributed-tracing incident investigation.

Specialist agents have already gathered evidence. Your job: combine their findings into ONE root-cause hypothesis. You investigate nothing yourself and call no tools — you only reason over the evidence given.

Hard rules:
- Ground EVERY claim in a finding that actually appears in the evidence list. Never invent a service name, metric, error, or number that isn't in the evidence.
- If the evidence is sparse, thin, or the agents disagree, say so and set confidence LOW. A hedged "low" answer is correct; a confident answer with no support is a failure.
- Confidence scale:
  - "high": multiple agents independently point at the same cause with concrete numbers.
  - "medium": one strong finding, or several weak agreeing ones.
  - "low": sparse, conflicting, or no clear signal.

Return ONLY a JSON object. No prose before or after. No markdown fences. Exactly these keys:
{
  "hypothesis": "<one paragraph: the most likely root cause, in plain English>",
  "confidence": "low" | "medium" | "high",
  "evidence": ["<the specific findings you actually used>", ...],
  "citations": [
    {"claim": "<a claim from your hypothesis>", "source": "<agent name whose finding supports it>"}
  ]
}"""
_agent = create_agent(model=make_llm(json_mode=True),
                      system_prompt=SYNTHESIZER_PROMPT)

def _synth_user_msg(state:InvestigationState) -> str:
    evidence_brief = json.dumps(state["evidence"], indent=2)
    return f"""Incident under investigation:

    {incident_brief(state)}

    Evidence collected by specialists:

    {evidence_brief}

    Produce the root-cause hypothesis JSON now."""


def synthesizer_node(state: InvestigationState) -> dict:
    result = _agent.invoke({"messages": [("user", _synth_user_msg(state))]})
    raw: str = result["messages"][-1].content
    try:
        cleaned = (raw.strip().removeprefix("```json").removeprefix("```").removesuffix("```")
                   .strip())
        parsed = json.loads(cleaned)
    except Exception:
        parsed = {
            "hypothesis": raw,
            "confidence": "low",
            "evidence": [],
            "citations": [],
        }
    return {"hypothesis": parsed}
