"""
Hugging Face to TensorFlow Lite Converter for Fasal Drishti AI.
Downloads models from Hugging Face Hub, exports to TFLite with NNAPI optimizations,
and updates app assets (labels.txt & model.tflite).
"""
import os
import sys
import json
import numpy as np

def convert_huggingface_model(hf_repo_id: str = "linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification"):
    print(f"============================================================")
    print(f"🚀 Converting Hugging Face Model: {hf_repo_id}")
    print(f"============================================================")

    try:
        from transformers import AutoImageProcessor, AutoModelForImageClassification
        import torch
    except ImportError:
        print("Please install requirements: pip install transformers torch tensorflow onnx tf2onnx")
        return

    print("1. Downloading Hugging Face model and labels...")
    processor = AutoImageProcessor.from_pretrained(hf_repo_id)
    model = AutoModelForImageClassification.from_pretrained(hf_repo_id)
    model.eval()

    labels = [model.config.id2label[i] for i in range(len(model.config.id2label))]
    print(f"   Found {len(labels)} classes.")

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    assets_dir = os.path.join(base_dir, "app", "src", "main", "assets")
    os.makedirs(assets_dir, exist_ok=True)

    # Save labels.txt
    labels_file = os.path.join(assets_dir, "labels.txt")
    with open(labels_file, "w", encoding="utf-8") as f:
        for lbl in labels:
            f.write(f"{lbl}\n")
    print(f"   ✅ Saved {len(labels)} labels to {labels_file}")

    # Export to ONNX / TFLite
    dummy_input = torch.randn(1, 3, 224, 224)
    onnx_path = os.path.join(base_dir, "backend", "temp_model.onnx")
    
    print("2. Exporting PyTorch model to ONNX...")
    torch.onnx.export(
        model,
        dummy_input,
        onnx_path,
        export_params=True,
        opset_version=13,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes=None
    )
    print("   ✅ Exported ONNX model.")

    print("3. Converting ONNX to TFLite...")
    try:
        import onnx
        from onnx_tf.backend import prepare
        import tensorflow as tf

        onnx_model = onnx.load(onnx_path)
        tf_rep = prepare(onnx_model)
        tf_saved_model_path = os.path.join(base_dir, "backend", "temp_saved_model")
        tf_rep.export_graph(tf_saved_model_path)

        converter = tf.lite.TFLiteConverter.from_saved_model(tf_saved_model_path)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.float32]
        tflite_model = converter.convert()

        output_tflite = os.path.join(assets_dir, "model_4_mobilenet_finetuned.tflite")
        with open(output_tflite, "wb") as f:
            f.write(tflite_model)
            
        print(f"   🎉 SUCCESS! TFLite model saved to: {output_tflite} ({len(tflite_model) / (1024*1024):.2f} MB)")
    except Exception as e:
        print(f"   Note: For direct on-device conversion, ensure onnx-tf / tensorflow are installed: {e}")

if __name__ == "__main__":
    repo = sys.argv[1] if len(sys.argv) > 1 else "linkanjarad/mobilenet_v2_1.0_224-plant-disease-identification"
    convert_huggingface_model(repo)
