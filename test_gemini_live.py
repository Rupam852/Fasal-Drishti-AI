import urllib.request
import json

url = 'https://tajizxhfxewkelzrmgux.supabase.co/rest/v1/app_config?key=eq.gemini_api_key&select=*'
key = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRhaml6eGhmeGV3a2VsenJtZ3V4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3ODMxMjUsImV4cCI6MjEwNDM1OTEyNX0.l39iKsN8kWzuQt2-T0dISIokx9Ys8E1ITXQfZcTi3Zw'

req = urllib.request.Request(url, headers={'apikey': key, 'Authorization': f'Bearer {key}'})
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode())
    gemini_key = data[0]['value'] if data else ''

print(f"Gemini Key: {gemini_key[:10]}...")

list_url = f'https://generativelanguage.googleapis.com/v1beta/models?key={gemini_key}'
req_models = urllib.request.Request(list_url)
with urllib.request.urlopen(req_models) as resp:
    res = json.loads(resp.read().decode())
    models = res.get('models', [])
    print(f"\n--- TOTAL MODELS ({len(models)}) ---")
    active_gen = []
    for m in models:
        name = m.get('name', '').replace('models/', '')
        disp = m.get('displayName', '')
        methods = m.get('supportedGenerationMethods', [])
        if 'generateContent' in methods:
            print(f"✅ {name} -> {disp}")
            active_gen.append(name)

print("\n--- TESTING ACTIVE GENERATION MODELS LIVE ---")
for m in active_gen[:10]:
    test_url = f'https://generativelanguage.googleapis.com/v1beta/models/{m}:generateContent?key={gemini_key}'
    payload = json.dumps({
        'contents': [{'parts': [{'text': 'Agricultural check: say Fasal OK in 2 words.'}]}],
        'generationConfig': {'maxOutputTokens': 15}
    }).encode('utf-8')
    req_t = urllib.request.Request(test_url, data=payload, headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req_t) as r:
            ans = json.loads(r.read().decode())
            text = ans['candidates'][0]['content']['parts'][0]['text'].strip()
            print(f"  [SUCCESS 200] {m:35} => {text}")
    except Exception as e:
        print(f"  [ERROR]       {m:35} => {e}")
