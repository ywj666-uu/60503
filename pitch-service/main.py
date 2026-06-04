from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from schemas.request import AnalyzeRequest, ExtractPitchRequest, EstimateBpmRequest
from schemas.response import AnalyzeResponse, PitchData, BpmEstimation
from services.pitch_extractor import extract_pitch
from services.pitch_comparator import compare_pitch
from services.bpm_estimator import estimate_bpm
from services.audio_loader import load_audio_from_url

app = FastAPI(title="Opera Pitch Analysis Service", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/api/v1/health")
async def health_check():
    return {"status": "healthy", "service": "pitch-analysis", "version": "2.0.0"}


@app.post("/api/v1/analyze", response_model=AnalyzeResponse)
async def analyze_pitch_endpoint(request: AnalyzeRequest):
    teacher_audio = await load_audio_from_url(
        request.teacher_audio_url, request.sample_rate
    )
    student_audio = await load_audio_from_url(
        request.student_audio_url, request.sample_rate
    )

    teacher_pitch = extract_pitch(
        teacher_audio, request.sample_rate,
        request.hop_length, request.confidence_threshold
    )
    student_pitch = extract_pitch(
        student_audio, request.sample_rate,
        request.hop_length, request.confidence_threshold
    )

    # 比对时传入调门、板式和滑动窗口参数
    result = compare_pitch(
        teacher_pitch, student_pitch,
        diao_men=request.diao_men,
        ban_shi=request.ban_shi,
        error_window_seconds=request.error_window_seconds,
        error_threshold_cents=request.error_threshold_cents
    )

    # 同时估算教师音频 BPM
    bpm_result = estimate_bpm(teacher_audio, request.sample_rate)

    return AnalyzeResponse(
        teacher_pitch=teacher_pitch,
        student_pitch=student_pitch,
        student_pitch_shifted=result["student_shifted"],
        overall_score=result["overall_score"],
        per_frame_accuracy=result["per_frame_accuracy"],
        error_segments=result["error_segments"],
        statistics=result["statistics"],
        estimated_bpm=bpm_result
    )


@app.post("/api/v1/extract-pitch", response_model=PitchData)
async def extract_pitch_endpoint(request: ExtractPitchRequest):
    audio = await load_audio_from_url(request.audio_url, request.sample_rate)
    pitch_data = extract_pitch(
        audio, request.sample_rate,
        request.hop_length, request.confidence_threshold
    )
    return pitch_data


@app.post("/api/v1/estimate-bpm", response_model=BpmEstimation)
async def estimate_bpm_endpoint(request: EstimateBpmRequest):
    """从教师音频自动估算 BPM，作为节拍器默认值。"""
    audio = await load_audio_from_url(request.audio_url, request.sample_rate)
    return estimate_bpm(audio, request.sample_rate)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
