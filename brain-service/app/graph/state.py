from operator import add
from typing import Annotated
from langgraph.graph import MessagesState

class InvestigationState(MessagesState):
    incident: dict
    selected_agents: list[str]
    evidence: Annotated[list, add]
    hypothesis: dict