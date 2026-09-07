"""
Test available models on NVIDIA NIM with user's API key
"""
import sys
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

import requests
import json
import time

API_KEY = "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"
URL = "https://integrate.api.nvidia.com/v1/chat/completions"

models_to_test = [
    "meta/llama-3.3-70b-instruct",
    "meta/llama-3.2-11b-vision-instruct",
    "meta/llama-3.1-70b-instruct",
    "meta/llama-3.1-8b-instruct",
    "mistralai/mixtral-8x22b-instruct-v0.1",
    "nvidia/llama-3.1-nemotron-70b-instruct"
]

prompt = "A farmer diagnosed Tomato Late Blight on their crop. Give quick 3-bullet advice in Hindi & English on recommended chemical spray (Mancozeb) and organic remedy."

headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

print("Testing NVIDIA NIM API Models with your live key...\n" + "="*50)

best_model = None
fastest_time = 999.0

for model in models_to_test:
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": "You are an expert agronomist for Indian agriculture."},
            {"role": "user", "content": prompt}
        ],
        "temperature": 0.2,
        "max_tokens": 300
    }
    
    start_t = time.time()
    try:
        res = requests.post(URL, headers=headers, json=payload, timeout=25)
        elapsed = time.time() - start_t
        
        if res.status_code == 200:
            data = res.json()
            reply = data["choices"][0]["message"]["content"]
            print(f"[SUCCESS] MODEL: {model}")
            print(f"   Response Time: {elapsed:.2f}s")
            print(f"   Sample Response:\n{reply}\n" + "-"*40)
            if elapsed < fastest_time:
                fastest_time = elapsed
                best_model = model
        else:
            print(f"[FAILED] MODEL: {model} (Status: {res.status_code}) -> {res.text[:120]}")
    except Exception as e:
        print(f"[ERROR] MODEL: {model} -> Error: {e}")

print("="*50)
print(f"Best Recommended Model: {best_model or 'meta/llama-3.3-70b-instruct'}")
