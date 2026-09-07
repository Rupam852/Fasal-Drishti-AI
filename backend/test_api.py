"""
Sanity check script for Fasal Drishti ML backend modules.
"""
import sys

# Ensure UTF-8 output encoding on Windows console
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

from backend.app.disease_db import get_disease_info, CLASS_NAMES
from backend.app.model_handler import handler

print(f"Total 38 PlantVillage classes mapped: {len(CLASS_NAMES)}")

test_class = "Tomato___Late_blight"
info = get_disease_info(test_class)
print(f"Sample Disease Lookup: {info['crop_name']} - {info['disease_name']} ({info['severity']})")
print(f"Symptoms (Hindi): {info.get('symptoms_hindi', 'N/A')}")
print(f"Treatment: {info.get('treatment', 'N/A')}")

# Simulated prediction test
sim_result = handler._simulate_prediction()
print(f"Simulated Top Prediction: {sim_result['predicted_class']} ({sim_result['confidence']*100:.1f}%)")
print("Sanity check completed successfully!")
