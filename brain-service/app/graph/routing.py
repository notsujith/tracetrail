from app.graph.state import InvestigationState
from langgraph.types import Send


def route_specialists(state: InvestigationState) -> list[Send]:
    return [Send(agent, state) for agent in state["selected_agents"]]

