from app.graph.state import InvestigationState


def incident_brief(state: InvestigationState) -> str:
    incident: dict = state["incident"]
    service = incident.get("service", "unknown")
    window = incident.get("window", "unspecified")
    summary = incident.get("summary", "")

    return (f"Incident on {service}, window {window}. Summary: {summary}. "
            f"Investigate and report your finding concisely with specific numbers.")