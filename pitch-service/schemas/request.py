from pydantic import BaseModel
from typing import Optional


class AnalyzeRequest(BaseModel):
    teacher_audio_url: str
    student_audio_url: str
    diao_men: Optional[str] = None  # 调门，用于将学生音高平移到教师参考系
    ban_shi: Optional[str] = None  # 板式，用于自适应错误检测窗口长度
    sample_rate: int = 16000
    hop_length: int = 160
    confidence_threshold: float = 0.6
    error_window_seconds: float = 3.0  # 滑动窗口长度（秒），板式为空时使用
    error_threshold_cents: float = 50.0  # 音准阈值（音分）


class ExtractPitchRequest(BaseModel):
    audio_url: str
    sample_rate: int = 16000
    hop_length: int = 160
    confidence_threshold: float = 0.6


class EstimateBpmRequest(BaseModel):
    audio_url: str
    sample_rate: int = 16000
