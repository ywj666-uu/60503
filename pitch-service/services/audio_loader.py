import numpy as np
import httpx
import librosa
import io


async def load_audio_from_url(url: str, sample_rate: int = 16000) -> np.ndarray:
    """Download audio from URL and convert to numpy array."""
    async with httpx.AsyncClient(timeout=60.0) as client:
        response = await client.get(url)
        response.raise_for_status()

    audio_bytes = response.content
    audio, sr = librosa.load(io.BytesIO(audio_bytes), sr=sample_rate, mono=True)
    return audio
