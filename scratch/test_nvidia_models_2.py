"""
Test additional active NVIDIA NIM models
"""
import sys
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

import requests
import time

API_KEY = "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"
URL = "https://integrate.api.nvidia.com/v1/chat/completions"

models_to_test = [
    "meta/llama-3.2-11b-vision-instruct",
    "meta/llama-3.2-90b-vision-instruct",
    "meta/llama-3.2-3b-instruct",
    "meta/llama-3.2-1b-instruct",
    "mistralai/mistral-large-2-instruct",
    "google/gemma-2-27b-it"
]

prompt = "Crop Disease: Tomato Late Blight. Give 2 lines of treatment in Hindi & English."

headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

for model in models_to_test:
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": "You are an expert crop pathologist."},
            {"role": "user", "content": prompt}
        ],
        "temperature": 0.2,
        "max_tokens": 150
    }
    
    start_t = time.time()
    try:
        res = requests.post(URL, headers=headers, json=payload, timeout=20)
        elapsed = time.time() - start_t
        if res.status_code == 200:
            print(f"[ACTIVE 200] {model} (Time: {elapsed:.2f}s)")
        else:
            print(f"[INACTIVE {res.status_code}] {model}")
    except Exception as e:
        print(f"[ERROR] {model}: {e}")
