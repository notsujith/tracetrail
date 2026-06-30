from langgraph.constants import START, END
from langgraph.graph import StateGraph
from app.agents.log_detective import log_detective_node
from app.agents.metrics_analyst import metrics_analyst_node
from app.agents.synthesizer import synthesizer_node
from app.agents.trace_walker import trace_walker_node
from app.agents.triage import triage_node
from app.graph.routing import route_specialists
from app.graph.state import InvestigationState


def build_graph():
    builder = StateGraph(InvestigationState)

    builder.add_node("triage", triage_node)
    builder.add_node("metrics_analyst", metrics_analyst_node)
    builder.add_node("log_detective", log_detective_node)
    builder.add_node("trace_walker", trace_walker_node)
    builder.add_node("synthesizer", synthesizer_node)

    builder.add_edge(START, "triage")
    builder.add_conditional_edges("triage", route_specialists)
    builder.add_edge("metrics_analyst", "synthesizer")
    builder.add_edge("log_detective", "synthesizer")
    builder.add_edge("trace_walker", "synthesizer")
    builder.add_edge("synthesizer", END)

    return builder.compile()

graph = build_graph()
