"""
FastAPI Server for Fasal Drishti Crop Vision & Agronomy Advisory.
"""

from fastapi import FastAPI, File, UploadFile, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict, Any

from .model_handler import handler
from .disease_db import get_disease_info, CLASS_NAMES, DISEASE_METADATA
from .nvidia_service import call_secondary_ai

app = FastAPI(
    title="Fasal Drishti ML Backend",
    description="Crop Disease Vision & Agronomy Advisory Service",
    version="1.0.0"
)

# Enable CORS for Android client & web testing
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class SecondaryAiRequest(BaseModel):
    primary_result: str
    confidence: float
    user_query: Optional[str] = None
    image_base64: Optional[str] = None
    language: Optional[str] = "en"

class DiseaseResponse(BaseModel):
    class_id: str
    crop_name: str
    crop_hindi: Optional[str] = None
    disease_name: str
    disease_hindi: Optional[str] = None
    severity: str
    is_healthy: bool
    symptoms: str
    symptoms_hindi: Optional[str] = None
    treatment: str
    treatment_hindi: Optional[str] = None
    prevention: str

@app.get("/")
def root():
    return {
        "app": "Fasal Drishti API",
        "status": "online",
        "model_loaded": handler.model is not None,
        "classes_count": len(CLASS_NAMES)
    }

@app.get("/health")
def health():
    return {"status": "healthy", "service": "fasal-drishti-backend"}

@app.post("/predict")
async def predict_crop_disease(file: UploadFile = File(...)):
    """
    Accepts an uploaded crop leaf photo and returns the top predicted diseases,
    confidence score, and comprehensive treatment guidelines.
    """
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Uploaded file must be a valid image (JPEG/PNG/WEBP).")
    
    try:
        image_bytes = await file.read()
        result = handler.predict(image_bytes)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference processing failed: {str(e)}")

@app.post("/predict/secondary")
def secondary_advisory(req: SecondaryAiRequest):
    """
    Calls secondary vision-language / agronomy model (NVIDIA NIM) for low-confidence scans
    or in-depth conversational follow-up.
    """
    return call_secondary_ai(
        primary_result=req.primary_result,
        confidence=req.confidence,
        user_query=req.user_query,
        image_base64=req.image_base64,
        language=req.language or "en"
    )

@app.get("/diseases/{class_id}", response_model=DiseaseResponse)
def get_disease(class_id: str):
    """
    Retrieve symptoms, treatment, and prevention tips for a specific disease class.
    """
    return get_disease_info(class_id)

@app.get("/diseases")
def list_all_diseases(crop: Optional[str] = None):
    """
    List all supported crop diseases, with optional filtering by crop name.
    """
    results = []
    for cid in CLASS_NAMES:
        info = get_disease_info(cid)
        if crop and crop.lower() not in info["crop_name"].lower():
            continue
        results.append(info)
    return {"count": len(results), "diseases": results}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.app.main:app", host="0.0.0.0", port=8000, reload=True)
