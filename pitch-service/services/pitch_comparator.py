import numpy as np
from dtw import dtw
from schemas.response import PitchData, ErrorSegment, Statistics


# 调门到半音偏移的映射（相对于 D 调 = 0）
DIAO_MEN_SEMITONE_MAP = {
    "D调": 0,
    "降E调": 1, "bE调": 1,
    "E调": 2,
    "F调": 3,
    "升F调": 4, "#F调": 4,
    "G调": 5,
    "降A调": 6, "bA调": 6,
    "A调": 7,
    "降B调": 8, "bB调": 8,
    "B调": 9,
    "C调": 10,
    "升C调": 11, "#C调": 11,
    "1=D": 0, "1=bE": 1, "1=E": 2, "1=F": 3,
    "1=G": 5, "1=A": 7, "1=bB": 8, "1=C": 10,
    "正宫调": 0, "小工调": 5, "尺字调": 7,
    "凡字调": 3, "六字调": 10, "乙字调": 8,
}

# 板式 → 错误检测窗口长度映射（秒）
# 慢板节奏慢，允许更长窗口避免误报；快板窗口短一些
BAN_SHI_WINDOW_MAP = {
    "慢板": 5.0,
    "原板": 3.0,
    "二六": 2.5,
    "流水": 2.0,
    "快板": 2.0,
    "快三眼": 2.5,
    "慢三眼": 5.0,
    "散板": 4.0,
    "摇板": 4.0,
    "导板": 4.0,
    "回龙": 3.0,
    "西皮慢板": 5.0,
    "西皮原板": 3.0,
    "西皮流水": 2.0,
    "西皮快板": 2.0,
    "二黄慢板": 5.0,
    "二黄原板": 3.0,
    "二黄快原板": 2.5,
}


def get_adaptive_window(ban_shi: str) -> float:
    """根据板式自适应错误检测窗口长度。"""
    if not ban_shi:
        return 3.0
    # 精确匹配
    if ban_shi in BAN_SHI_WINDOW_MAP:
        return BAN_SHI_WINDOW_MAP[ban_shi]
    # 模糊匹配关键字
    for key, val in BAN_SHI_WINDOW_MAP.items():
        if key in ban_shi or ban_shi in key:
            return val
    # 含"慢"字默认放宽
    if "慢" in ban_shi:
        return 5.0
    if "快" in ban_shi or "流水" in ban_shi:
        return 2.0
    return 3.0


def compute_baseline_from_first_n_seconds(
    freq: np.ndarray,
    times: np.ndarray,
    n_seconds: float = 5.0
) -> float:
    """
    取前 n_seconds 秒有声帧的平均音高（Hz）作为基准。
    如果前5秒无有声帧，回退到全局中位数。
    """
    mask = (times <= n_seconds) & (freq > 0)
    valid = freq[mask]
    if len(valid) >= 10:
        return float(np.mean(valid))
    # 回退：全局有声帧中位数
    all_valid = freq[freq > 0]
    if len(all_valid) == 0:
        return 0.0
    return float(np.median(all_valid))


def segment_into_phrases(
    freq: np.ndarray,
    times: np.ndarray,
    min_silence_sec: float = 0.3,
    min_phrase_sec: float = 1.0
) -> list:
    """
    将音频按静音段自动分乐句。

    检测连续无声帧（freq=0）超过 min_silence_sec 的位置作为乐句边界。
    返回 [(start_idx, end_idx), ...] 列表。
    """
    voiced = freq > 0
    hop = float(times[1] - times[0]) if len(times) > 1 else 0.01
    min_silence_frames = int(min_silence_sec / hop)
    min_phrase_frames = int(min_phrase_sec / hop)

    phrases = []
    in_phrase = False
    phrase_start = 0
    silence_count = 0

    for i in range(len(voiced)):
        if voiced[i]:
            if not in_phrase:
                in_phrase = True
                phrase_start = i
            silence_count = 0
        else:
            if in_phrase:
                silence_count += 1
                if silence_count >= min_silence_frames:
                    phrase_end = i - silence_count
                    if phrase_end - phrase_start >= min_phrase_frames:
                        phrases.append((phrase_start, phrase_end))
                    in_phrase = False
                    silence_count = 0

    # 收尾
    if in_phrase:
        phrase_end = len(voiced) - 1
        if phrase_end - phrase_start >= min_phrase_frames:
            phrases.append((phrase_start, phrase_end))

    # 如果分不出乐句，把整段作为一个乐句
    if not phrases:
        phrases = [(0, len(freq) - 1)]

    return phrases


def normalize_pitch_dynamic(
    student_freq: np.ndarray,
    teacher_freq: np.ndarray,
    teacher_times: np.ndarray,
    student_times: np.ndarray
) -> tuple:
    """
    动态分乐句音高校准。

    流程：
    1. 用教师前5秒平均音高作为全局基准
    2. 计算学生对应段落相对基准的全局偏移并做初步平移
    3. 按乐句边界做局部微调：每个乐句内部单独估算残余偏移并修正

    Returns: (shifted_frequencies, global_shift_cents, phrase_shifts)
    """
    # 1) 教师前5秒基准
    teacher_baseline_hz = compute_baseline_from_first_n_seconds(
        teacher_freq, teacher_times, n_seconds=5.0
    )
    if teacher_baseline_hz <= 0:
        return student_freq.copy(), 0.0, []

    teacher_baseline_cents = 1200 * np.log2(teacher_baseline_hz / 440.0)

    # 学生前5秒对应段落的平均
    student_baseline_hz = compute_baseline_from_first_n_seconds(
        student_freq, student_times, n_seconds=5.0
    )
    if student_baseline_hz <= 0:
        return student_freq.copy(), 0.0, []

    student_baseline_cents = 1200 * np.log2(student_baseline_hz / 440.0)

    # 全局偏移
    global_shift = student_baseline_cents - teacher_baseline_cents

    # 消除八度跳变
    while global_shift > 600:
        global_shift -= 1200
    while global_shift < -600:
        global_shift += 1200

    # 2) 全局平移
    if abs(global_shift) < 5:
        shifted = student_freq.copy()
        global_shift = 0.0
    else:
        ratio = 2 ** (global_shift / 1200.0)
        shifted = np.where(student_freq > 0, student_freq / ratio, 0.0)

    # 3) 分乐句动态微调
    teacher_phrases = segment_into_phrases(teacher_freq, teacher_times)
    student_phrases = segment_into_phrases(shifted, student_times)

    # 取两者乐句数的最小值做逐句匹配
    n_phrases = min(len(teacher_phrases), len(student_phrases))
    phrase_shifts = []

    for i in range(n_phrases):
        t_start, t_end = teacher_phrases[i]
        s_start, s_end = student_phrases[i]

        t_phrase_freq = teacher_freq[t_start:t_end + 1]
        s_phrase_freq = shifted[s_start:s_end + 1]

        t_valid = t_phrase_freq[t_phrase_freq > 0]
        s_valid = s_phrase_freq[s_phrase_freq > 0]

        if len(t_valid) < 5 or len(s_valid) < 5:
            phrase_shifts.append(0.0)
            continue

        t_mean_cents = 1200 * np.log2(np.mean(t_valid) / 440.0)
        s_mean_cents = 1200 * np.log2(np.mean(s_valid) / 440.0)
        phrase_offset = s_mean_cents - t_mean_cents

        # 乐句微调幅度限制在 ±100 cents 内，防止异常值
        phrase_offset = np.clip(phrase_offset, -100, 100)

        if abs(phrase_offset) >= 8:  # 超过 8 cents 才微调
            ratio = 2 ** (phrase_offset / 1200.0)
            shifted[s_start:s_end + 1] = np.where(
                shifted[s_start:s_end + 1] > 0,
                shifted[s_start:s_end + 1] / ratio,
                0.0
            )
            phrase_shifts.append(round(phrase_offset, 2))
        else:
            phrase_shifts.append(0.0)

    return shifted, round(global_shift, 2), phrase_shifts


def compute_per_frame_accuracy(
    deviations: np.ndarray,
    threshold_cents: float = 50.0
) -> np.ndarray:
    """逐帧准确率：accuracy = max(0, 1 - |deviation| / threshold)"""
    abs_dev = np.abs(deviations)
    accuracy = np.clip(1.0 - abs_dev / threshold_cents, 0.0, 1.0)
    return accuracy


def sliding_window_error_detection(
    per_frame_accuracy: np.ndarray,
    frame_times: np.ndarray,
    deviations: np.ndarray,
    window_seconds: float = 3.0,
    accuracy_threshold: float = 0.5,
    hop_time: float = 0.01
) -> list:
    """
    滑动窗口错误检测。
    窗口长度由 window_seconds 控制（已按板式自适应）。
    """
    if len(per_frame_accuracy) == 0:
        return []

    window_frames = max(1, int(window_seconds / hop_time))
    n_frames = len(per_frame_accuracy)
    errors = []

    in_error = False
    error_start = 0

    for i in range(n_frames - window_frames + 1):
        window_acc = np.mean(per_frame_accuracy[i:i + window_frames])

        if window_acc < accuracy_threshold and not in_error:
            in_error = True
            error_start = i
        elif window_acc >= accuracy_threshold and in_error:
            in_error = False
            errors.append(_build_sliding_error(
                error_start, i + window_frames - 1,
                frame_times, deviations, per_frame_accuracy
            ))

    if in_error:
        errors.append(_build_sliding_error(
            error_start, n_frames - 1,
            frame_times, deviations, per_frame_accuracy
        ))

    return _merge_close_segments(errors, min_gap=1.0)


def _build_sliding_error(
    start_idx: int, end_idx: int,
    frame_times: np.ndarray,
    deviations: np.ndarray,
    per_frame_accuracy: np.ndarray
) -> ErrorSegment:
    end_idx = min(end_idx, len(frame_times) - 1)
    start_time = float(frame_times[start_idx])
    end_time = float(frame_times[end_idx])

    segment_devs = deviations[start_idx:end_idx + 1]
    segment_acc = per_frame_accuracy[start_idx:end_idx + 1]
    abs_devs = np.abs(segment_devs)

    avg_dev = float(np.mean(abs_devs))
    max_dev = float(np.max(abs_devs))
    window_accuracy = float(np.mean(segment_acc))

    if avg_dev > 100:
        severity = "SEVERE"
    elif avg_dev > 50:
        severity = "MODERATE"
    else:
        severity = "MILD"

    mean_signed = float(np.mean(segment_devs))
    direction = "SHARP" if mean_signed > 0 else "FLAT"

    return ErrorSegment(
        start_time=round(start_time, 3),
        end_time=round(end_time, 3),
        avg_deviation_cents=round(avg_dev, 2),
        max_deviation_cents=round(max_dev, 2),
        severity=severity,
        direction=direction,
        window_accuracy=round(window_accuracy * 100, 2)
    )


def _merge_close_segments(segments: list, min_gap: float = 1.0) -> list:
    if len(segments) <= 1:
        return segments

    merged = [segments[0]]
    for seg in segments[1:]:
        prev = merged[-1]
        if seg.start_time - prev.end_time < min_gap:
            merged[-1] = ErrorSegment(
                start_time=prev.start_time,
                end_time=seg.end_time,
                avg_deviation_cents=round((prev.avg_deviation_cents + seg.avg_deviation_cents) / 2, 2),
                max_deviation_cents=max(prev.max_deviation_cents, seg.max_deviation_cents),
                severity=prev.severity if prev.avg_deviation_cents > seg.avg_deviation_cents else seg.severity,
                direction=prev.direction,
                window_accuracy=round((prev.window_accuracy + seg.window_accuracy) / 2, 2)
            )
        else:
            merged.append(seg)
    return merged


def compare_pitch(
    teacher: PitchData,
    student: PitchData,
    diao_men: str = None,
    ban_shi: str = None,
    error_window_seconds: float = 3.0,
    error_threshold_cents: float = 50.0
) -> dict:
    """
    比对流程：
    1. 用教师前5秒平均音高为基准，计算学生全局偏移并平移
    2. 分乐句做动态校准（每乐句局部微调残余偏差）
    3. DTW 对齐
    4. 计算逐帧准确率
    5. 按板式自适应窗口长度检测错误段落
    """
    t_freq = np.array(teacher.frequencies)
    s_freq = np.array(student.frequencies)
    t_times = np.array(teacher.times)
    s_times = np.array(student.times)

    t_voiced = t_freq > 0
    s_voiced = s_freq > 0

    if np.sum(t_voiced) == 0 or np.sum(s_voiced) == 0:
        return _empty_result(t_voiced, s_voiced, len(t_freq), len(s_freq))

    # 1+2) 前5秒基准 + 分乐句动态校准
    s_freq_shifted, global_shift, phrase_shifts = normalize_pitch_dynamic(
        s_freq, t_freq, t_times, s_times
    )

    student_shifted = PitchData(
        times=student.times,
        frequencies=s_freq_shifted.tolist(),
        confidences=student.confidences
    )

    # 3) 提取有声帧 & DTW
    t_voiced_freq = t_freq[t_voiced]
    s_shifted_voiced = s_freq_shifted[s_freq_shifted > 0]

    if len(t_voiced_freq) == 0 or len(s_shifted_voiced) == 0:
        return _empty_result(t_voiced, s_voiced, len(t_freq), len(s_freq))

    t_cents = 1200 * np.log2(t_voiced_freq / 440.0)
    s_cents = 1200 * np.log2(s_shifted_voiced / 440.0)

    alignment = dtw(
        s_cents.reshape(-1, 1),
        t_cents.reshape(-1, 1),
        keep_internals=True
    )

    deviations = np.array([
        s_cents[s_idx] - t_cents[t_idx]
        for s_idx, t_idx in zip(alignment.index1, alignment.index2)
    ])

    t_voiced_times = t_times[t_voiced]
    aligned_times = np.array([
        t_voiced_times[t_idx] for t_idx in alignment.index2
    ])

    # 4) 逐帧准确率
    per_frame_accuracy = compute_per_frame_accuracy(deviations, error_threshold_cents)

    # 5) 板式自适应窗口
    adaptive_window = get_adaptive_window(ban_shi) if ban_shi else error_window_seconds
    hop_time = float(t_times[1] - t_times[0]) if len(t_times) > 1 else 0.01

    error_segments = sliding_window_error_detection(
        per_frame_accuracy, aligned_times, deviations,
        window_seconds=adaptive_window,
        accuracy_threshold=0.5,
        hop_time=hop_time
    )

    overall_score = round(float(np.mean(per_frame_accuracy)) * 100, 2)
    abs_deviations = np.abs(deviations)

    statistics = Statistics(
        mean_deviation_cents=round(float(np.mean(abs_deviations)), 2),
        std_deviation_cents=round(float(np.std(abs_deviations)), 2),
        percentage_in_tune=round(float(np.sum(abs_deviations <= error_threshold_cents) / len(abs_deviations) * 100), 2),
        percentage_voiced_teacher=round(float(np.sum(t_voiced) / len(t_freq) * 100), 2),
        percentage_voiced_student=round(float(np.sum(s_voiced) / len(s_freq) * 100), 2),
        pitch_shift_cents=global_shift
    )

    return {
        "student_shifted": student_shifted,
        "overall_score": overall_score,
        "per_frame_accuracy": per_frame_accuracy.tolist(),
        "error_segments": error_segments,
        "statistics": statistics,
        "phrase_shifts": phrase_shifts,
        "adaptive_window_seconds": adaptive_window
    }


def _empty_result(t_voiced, s_voiced, t_total, s_total):
    return {
        "student_shifted": PitchData(times=[], frequencies=[], confidences=[]),
        "overall_score": 0.0,
        "per_frame_accuracy": [],
        "error_segments": [],
        "statistics": Statistics(
            mean_deviation_cents=0.0,
            std_deviation_cents=0.0,
            percentage_in_tune=0.0,
            percentage_voiced_teacher=round(float(np.sum(t_voiced) / t_total * 100), 2) if t_total > 0 else 0,
            percentage_voiced_student=round(float(np.sum(s_voiced) / s_total * 100), 2) if s_total > 0 else 0,
            pitch_shift_cents=0.0
        ),
        "phrase_shifts": [],
        "adaptive_window_seconds": 3.0
    }
