from pydantic import BaseModel
from typing import List, Optional


class PitchData(BaseModel):
    times: List[float]
    frequencies: List[float]
    confidences: List[float]


class ErrorSegment(BaseModel):
    start_time: float
    end_time: float
    avg_deviation_cents: float
    max_deviation_cents: float
    severity: str
    direction: str
    window_accuracy: float  # 该窗口内的准确率


class Statistics(BaseModel):
    mean_deviation_cents: float
    std_deviation_cents: float
    percentage_in_tune: float
    percentage_voiced_teacher: float
    percentage_voiced_student: float
    pitch_shift_cents: float  # 应用的整体平移量


class BpmEstimation(BaseModel):
    bpm: float
    confidence: float
    beat_times: List[float]  # 估计的节拍时间点


class AnalyzeResponse(BaseModel):
    teacher_pitch: PitchData
    student_pitch: PitchData
    student_pitch_shifted: PitchData  # 平移后的学生音高
    overall_score: float
    per_frame_accuracy: List[float]  # 逐帧准确率曲线
    error_segments: List[ErrorSegment]
    statistics: Statistics
    estimated_bpm: Optional[BpmEstimation] = None
