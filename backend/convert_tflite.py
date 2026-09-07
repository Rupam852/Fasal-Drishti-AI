"""
Conversion script: Convert model_4_mobilenet_finetuned.keras to model.tflite
Optimized for Android on-device execution.
"""
import os
import sys
import tensorflow as tf

def convert_to_tflite(keras_model_path: str, output_tflite_path: str):
    if not os.path.exists(keras_model_path):
        print(f"Error: Keras model not found at {keras_model_path}")
        sys.exit(1)
        
    print(f"Loading Keras model from {keras_model_path}...")
    model = tf.keras.models.load_model(keras_model_path)
    print("Model loaded successfully!")
    model.summary()

    print("Converting to TensorFlow Lite...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    tflite_model = converter.convert()
    
    with open(output_tflite_path, "wb") as f:
        f.write(tflite_model)
        
    print(f"Successfully converted! TFLite model saved to {output_tflite_path} ({len(tflite_model) / (1024*1024):.2f} MB)")

if __name__ == "__main__":
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    model_path = os.path.join(base_dir, "model_4_mobilenet_finetuned.keras")
    output_path = os.path.join(base_dir, "model.tflite")
    convert_to_tflite(model_path, output_path)
