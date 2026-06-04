import os

SAMPLE_RATE = int(os.getenv("SAMPLE_RATE", "16000"))
HOP_LENGTH = int(os.getenv("HOP_LENGTH", "160"))
CONFIDENCE_THRESHOLD = float(os.getenv("CONFIDENCE_THRESHOLD", "0.6"))
CREPE_MODEL_CAPACITY = os.getenv("CREPE_MODEL_CAPACITY", "full")
