# Brain Evaluation Harness (RAGAS + DeepEval)

Scores the Brain's root-cause synthesizer against a golden dataset, using the
same local Ollama models the service runs on (no cloud keys).

## What it measures

| Layer    | Metric                           | Question it answers                                        |
|----------|----------------------------------|------------------------------------------------------------|
| RAGAS    | `faithfulness`                   | Is every claim in the hypothesis grounded in the evidence? |
| RAGAS    | `response_relevancy`             | Does the hypothesis actually address the incident?         |
| RAGAS    | `context_precision`              | Was the evidence given relevant to the true root cause?    |
| DeepEval | `Root Cause Correctness` (GEval) | Does the hypothesis match the human reference?             |

`faithfulness` directly tests the synthesizer's hard rule: never invent a
service, metric, or number that isn't in the evidence.

## Prerequisites

- Ollama running locally with both models pulled: