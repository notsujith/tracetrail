from pydantic import BaseModel


class InvestigationRequest(BaseModel):
    service: str
    window: str
    summary: str