"""Wire RAGAS and DeepEval to the same local Ollama models the Brain uses."""

from __future__ import annotations

from app.settings import setting as st


def ragas_llm():
    from langchain_ollama import ChatOllama
    from ragas.llms import LangchainLLMWrapper

    chat = ChatOllama(
        model=st.OLLAMA_CHAT_MODEL,
        base_url=st.ollama_base_url,
        temperature=0,
    )
    return LangchainLLMWrapper(chat)


def ragas_embeddings():
    from langchain_ollama import OllamaEmbeddings
    from ragas.embeddings import LangchainEmbeddingsWrapper

    emb = OllamaEmbeddings(
        model=st.OLLAMA_EMBEDDINGS_MODEL,
        base_url=st.ollama_base_url,
    )
    return LangchainEmbeddingsWrapper(emb)


def deepeval_model():
    from deepeval.models.base_model import DeepEvalBaseLLM
    from langchain_ollama import ChatOllama

    class OllamaDeepEval(DeepEvalBaseLLM):
        def __init__(self):
            self._chat = ChatOllama(
                model=st.OLLAMA_CHAT_MODEL,
                base_url=st.ollama_base_url,
                temperature=0,
            )

        def load_model(self):
            return self._chat

        def generate(self, prompt: str) -> str:
            return self._chat.invoke(prompt).content

        async def a_generate(self, prompt: str) -> str:
            res = await self._chat.ainvoke(prompt)
            return res.content

        def get_model_name(self) -> str:
            return f"ollama:{st.OLLAMA_CHAT_MODEL}"

    return OllamaDeepEval()
