FROM python:3.11-slim

WORKDIR /app

# Install system dependencies for image processing and OpenCV if needed
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy backend code and model file
COPY app/ ./app/
COPY ../model_4_mobilenet_finetuned.keras ./model_4_mobilenet_finetuned.keras

ENV PORT=8000
EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
