import numpy as np
import crepe
from schemas.response import PitchData


def extract_pitch(
    audio: np.ndarray,
    sample_rate: int = 16000,
    hop_length: int = 160,
    confidence_threshold: float = 0.6
) -> PitchData:
    """
    Extract pitch from audio using CREPE.

    Returns PitchData with times, frequencies, and confidences.
    Unvoiced frames (below confidence threshold) have frequency set to 0.
    """
    step_size = (hop_length / sample_rate) * 1000  # convert to milliseconds

    time, frequency, confidence, _ = crepe.predict(
        audio,
        sample_rate,
        model_capacity='full',
        step_size=step_size,
        viterbi=True
    )

    # Zero out frequencies where confidence is below threshold
    frequency = np.where(confidence >= confidence_threshold, frequency, 0.0)

    return PitchData(
        times=time.tolist(),
        frequencies=frequency.tolist(),
        confidences=confidence.tolist()
    )
