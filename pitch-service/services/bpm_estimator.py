import numpy as np
import librosa
from schemas.response import BpmEstimation


def estimate_bpm(audio: np.ndarray, sample_rate: int = 16000) -> BpmEstimation:
    """
    从教师音频自动估算 BPM。

    使用 librosa 的 onset detection + tempo estimation，
    对戏曲音频做适配（较宽的 tempo 搜索范围 40-200 BPM）。
    """
    # onset strength envelope
    onset_env = librosa.onset.onset_strength(
        y=audio, sr=sample_rate, hop_length=512
    )

    # 估算 tempo (允许 40-200 BPM 范围，适合戏曲板式)
    tempo, beat_frames = librosa.beat.beat_track(
        onset_envelope=onset_env,
        sr=sample_rate,
        hop_length=512,
        start_bpm=80,
        tightness=100,
        units='frames'
    )

    # 将 beat frames 转为时间点
    beat_times = librosa.frames_to_time(beat_frames, sr=sample_rate, hop_length=512)

    # 计算置信度：基于 beat 间隔的一致性
    if len(beat_times) > 2:
        intervals = np.diff(beat_times)
        cv = np.std(intervals) / np.mean(intervals) if np.mean(intervals) > 0 else 1.0
        confidence = max(0.0, min(1.0, 1.0 - cv))
    else:
        confidence = 0.0

    # tempo 可能是数组（librosa 新版本）
    bpm_value = float(tempo) if np.isscalar(tempo) else float(tempo[0])

    return BpmEstimation(
        bpm=round(bpm_value, 1),
        confidence=round(confidence, 3),
        beat_times=beat_times.tolist()
    )
