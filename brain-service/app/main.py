from fastapi import FastAPI
from langgraph_sdk.auth.exceptions import HTTPException

from app.graph.build import graph
from app.request import InvestigationRequest

app = FastAPI(title="TraceTrail Brain")

@app.get("/health")
def health() -> dict:
    return {"status": "ok"}

@app.post("/investigation")
def investigate(req: InvestigationRequest) -> dict:
    state = {"incident": req.model_dump(),
             "selected_agents": [],
             "evidence": [],
             "messages": []}
    try:
        out = graph.invoke(state)
        return out["hypothesis"]
    except Exception as e:
        raise HTTPException(status_code=502,
                            detail=f"investigation failed: {e}")
