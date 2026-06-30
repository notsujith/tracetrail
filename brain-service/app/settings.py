from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")
    query_service_url: str
    tenant_api_key: str
    OLLAMA_EMBEDDINGS_MODEL: str = "nomic-embed-text:latest"
    OLLAMA_CHAT_MODEL: str = "qwen2.5:3b"

setting = Settings()