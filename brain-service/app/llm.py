from langchain_ollama import ChatOllama, OllamaEmbeddings

from app.settings import setting as st

def make_llm(json_mode: bool = False) -> ChatOllama:
    kwargs = dict(model = st.OLLAMA_CHAT_MODEL,
                  temperature = 0,
                  base_url=st.ollama_base_url)
    if json_mode:
        kwargs["format"] = "json"

    return ChatOllama(**kwargs)

def make_embeddings() -> OllamaEmbeddings:
    return OllamaEmbeddings(model=st.OLLAMA_EMBEDDINGS_MODEL,
                            temperature=0)

