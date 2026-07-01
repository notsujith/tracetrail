"""Run the Brain synthesizer over the golden dataset and score it.

Usage:
    cd brain-service
    pip install -r eval/requirements-eval.txt
    python -m eval.runner
"""

from __future__ import annotations

import json
import time
from datetime import datetime, timezone
from pathlib import Path

from eval import ollama_judges
from eval.dataset import CASES, EvalCase

REPORTS_DIR = Path(__file__).resolve().parents[2] / "reports"


def _run_synthesizer(case: EvalCase) -> dict:
    from app.agents.synthesizer import synthesizer_node

    state = {
        "incident": case.incident,
        "selected_agents": [],
        "evidence": case.evidence,
        "messages": [],
    }
    out = synthesizer_node(state)
    return out["hypothesis"]


def _hypothesis_text(h: dict) -> str:
    if isinstance(h, dict):
        return str(h.get("hypothesis", h))
    return str(h)


def run() -> dict:
    print(f"Running Brain eval over {len(CASES)} cases...")
    rows = []
    for case in CASES:
        t0 = time.time()
        hypothesis = _run_synthesizer(case)
        rows.append(
            {
                "id": case.id,
                "summary": case.summary(),
                "reference": case.reference,
                "hypothesis": _hypothesis_text(hypothesis),
                "confidence": hypothesis.get("confidence") if isinstance(hypothesis, dict) else None,
                "contexts": case.contexts(),
                "latency_s": round(time.time() - t0, 2),
            }
        )
        print(f"  [{case.id}] done in {rows[-1]['latency_s']}s")

    ragas_scores = _score_ragas(rows)
    deepeval_scores = _score_deepeval(rows)

    report = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "chat_model": ollama_judges.st.OLLAMA_CHAT_MODEL,
        "cases": rows,
        "ragas": ragas_scores,
        "deepeval": deepeval_scores,
    }
    _write_report(report)
    return report


def _score_ragas(rows: list[dict]) -> dict:
    from ragas import EvaluationDataset, evaluate
    from ragas.dataset_schema import SingleTurnSample
    from ragas.metrics import (
        Faithfulness,
        ResponseRelevancy,
        LLMContextPrecisionWithReference,
    )

    samples = [
        SingleTurnSample(
            user_input=r["summary"],
            response=r["hypothesis"],
            retrieved_contexts=r["contexts"],
            reference=r["reference"],
        )
        for r in rows
    ]
    dataset = EvaluationDataset(samples=samples)

    llm = ollama_judges.ragas_llm()
    emb = ollama_judges.ragas_embeddings()

    result = evaluate(
        dataset=dataset,
        metrics=[
            Faithfulness(),
            ResponseRelevancy(),
            LLMContextPrecisionWithReference(),
        ],
        llm=llm,
        embeddings=emb,
    )
    df = result.to_pandas()
    metric_cols = [c for c in df.columns if c not in
                   ("user_input", "response", "retrieved_contexts", "reference")]
    aggregate = {c: float(df[c].mean()) for c in metric_cols}
    per_case = df[metric_cols].to_dict(orient="records")
    return {"aggregate": aggregate, "per_case": per_case}


def _score_deepeval(rows: list[dict]) -> dict:
    from deepeval.metrics import GEval
    from deepeval.test_case import LLMTestCase, LLMTestCaseParams

    model = ollama_judges.deepeval_model()
    metric = GEval(
        name="Root Cause Correctness",
        criteria="Determine whether the actual root-cause hypothesis identifies the "
                 "same underlying cause as the expected reference. Reward correctly "
                 "locating the failing service and mechanism. For sparse-signal cases, "
                 "reward a hedged low-confidence 'insufficient evidence' answer.",
        evaluation_params=[
            LLMTestCaseParams.INPUT,
            LLMTestCaseParams.ACTUAL_OUTPUT,
            LLMTestCaseParams.EXPECTED_OUTPUT,
        ],
        model=model,
        threshold=0.5,
    )

    per_case = []
    for r in rows:
        tc = LLMTestCase(
            input=r["summary"],
            actual_output=r["hypothesis"],
            expected_output=r["reference"],
        )
        metric.measure(tc)
        per_case.append(
            {"id": r["id"], "score": metric.score, "reason": metric.reason}
        )

    scores = [c["score"] for c in per_case if c["score"] is not None]
    aggregate = {"root_cause_correctness": sum(scores) / len(scores) if scores else 0.0}
    return {"aggregate": aggregate, "per_case": per_case}


def _write_report(report: dict) -> None:
    REPORTS_DIR.mkdir(parents=True, exist_ok=True)
    ts = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    json_path = REPORTS_DIR / f"brain_eval_{ts}.json"
    md_path = REPORTS_DIR / f"brain_eval_{ts}.md"

    json_path.write_text(json.dumps(report, indent=2))

    lines = [
        f"# Brain Eval Report ({report['generated_at']})",
        "",
        f"- Chat model: `{report['chat_model']}`",
        f"- Cases: {len(report['cases'])}",
        "",
        "## RAGAS (aggregate)",
        "",
        "| metric | score |",
        "|---|---|",
    ]
    for k, v in report["ragas"]["aggregate"].items():
        lines.append(f"| {k} | {v:.3f} |")
    lines += ["", "## DeepEval (aggregate)", "", "| metric | score |", "|---|---|"]
    for k, v in report["deepeval"]["aggregate"].items():
        lines.append(f"| {k} | {v:.3f} |")
    lines += ["", "## Per-case root-cause correctness", "",
              "| case | score | reason |", "|---|---|---|"]
    for c in report["deepeval"]["per_case"]:
        reason = (c.get("reason") or "").replace("\n", " ")[:160]
        lines.append(f"| {c['id']} | {c['score']:.3f} | {reason} |")

    md_path.write_text("\n".join(lines))
    print(f"\nWrote {md_path}\nWrote {json_path}")


if __name__ == "__main__":
    run()
