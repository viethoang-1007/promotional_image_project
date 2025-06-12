from flask import Flask, request, send_file, jsonify
from PIL import Image
import io
import torch
from torchvision import transforms
from torchvision.models.segmentation import deeplabv3_resnet101, DeepLabV3_ResNet101_Weights
import torch.nn as nn
import numpy as np
import os
from PIL import Image, ImageOps
from diffusers import StableDiffusionInpaintPipeline

app = Flask(__name__)

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
bg_pipeline = StableDiffusionInpaintPipeline.from_pretrained(
    "stabilityai/stable-diffusion-2-inpainting",
    torch_dtype=torch.float32
).to(device)

# === Load model khi server khởi động ===
model = deeplabv3_resnet101(weights=DeepLabV3_ResNet101_Weights.DEFAULT)
model.classifier[4] = nn.Conv2d(256, 2, kernel_size=1)
model.load_state_dict(torch.load("deeplabv3_resnet101_epoch25.pth", map_location=device))
model.to(device)
model.eval()



def resize_or_pad(image: Image.Image, target_size=512, is_mask=False):
    original_size = image.size
    w, h = original_size

    if w <= target_size and h <= target_size:
        delta_w = target_size - w
        delta_h = target_size - h
        padding = (
            delta_w // 2, delta_h // 2,
            delta_w - (delta_w // 2), delta_h - (delta_h // 2)
        )
        image = ImageOps.expand(image, padding, fill=0)
        return image, original_size, (1.0, padding)

    scale = target_size / max(w, h)
    new_w = int(w * scale)
    new_h = int(h * scale)

    interpolation = Image.NEAREST if is_mask else Image.LANCZOS
    image = image.resize((new_w, new_h), interpolation)

    delta_w = target_size - new_w
    delta_h = target_size - new_h
    padding = (
        delta_w // 2, delta_h // 2,
        delta_w - (delta_w // 2), delta_h - (delta_h // 2)
    )
    image = ImageOps.expand(image, padding, fill=0)
    return image, original_size, (scale, padding)

def recover_original_size(segmented_img: Image.Image, scale: float, padding: tuple, original_size: tuple):
    # Remove padding
    left, top, right, bottom = padding
    w_padded, h_padded = segmented_img.size
    cropped = segmented_img.crop((left, top, w_padded - right, h_padded - bottom))

    # Scale ngược lại
    recovered = cropped.resize(original_size, Image.LANCZOS)
    return recovered

@app.route("/segment", methods=["POST"])
def segment():
    if 'image' not in request.files:
        return jsonify({"error": "No image part"}), 400

    file = request.files['image']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    try:
        image = Image.open(file.stream).convert("RGB")
        original_size = image.size

        # Resize input
        resized_image, _, (scale, padding) = resize_or_pad(image)
        image_tensor = transforms.ToTensor()(resized_image).unsqueeze(0).to(device)

        # Segment
        with torch.no_grad():
            output = model(image_tensor)['out']
            pred_mask = torch.argmax(output.squeeze(), dim=0).byte().cpu().numpy()

        # Recover mask
        pred_mask_pil = Image.fromarray(pred_mask * 255)
        recovered_mask = recover_original_size(pred_mask_pil, scale, padding, original_size)
        mask_arr = (np.array(recovered_mask) > 127).astype(np.uint8)

        # Apply mask to original image
        image_np = np.array(image)
        alpha = (mask_arr * 255).astype(np.uint8)
        segmented_rgba = np.dstack((image_np, alpha))
        final_result = Image.fromarray(segmented_rgba, mode='RGBA')
        #final_result.show()

        # Trả lại ảnh dạng PNG
        img_io = io.BytesIO()
        final_result.save(img_io, format='PNG')
        img_io.seek(0)

        return send_file(img_io, mimetype='image/png')

    except Exception as e:
        return jsonify({"error": str(e)}), 500
    

@app.route("/generate_background", methods=["POST"])
def generate_background():
    if 'image' not in request.files or 'prompt' not in request.form:
        return jsonify({"error": "Missing image or prompt"}), 400

    file = request.files['image']
    prompt = request.form['prompt']

    print(">>> /generate_background CALLED")
    print("Files received:", list(request.files.keys()))
    print("Prompt received:", request.form.get("prompt"))

    try:
        foreground = Image.open(file.stream).convert("RGBA")

        # Split mask
        fg = foreground.convert("RGB")
        alpha = foreground.split()[-1]
        mask = Image.fromarray((np.array(alpha) < 10).astype(np.uint8) * 255)

        # Resize with padding (same logic as your local test script)
        def resize_with_padding(image, target_size=(512, 512)):
            old_size = image.size
            ratio = min(target_size[0]/old_size[0], target_size[1]/old_size[1])
            new_size = (int(old_size[0]*ratio), int(old_size[1]*ratio))
            try:
                resample_method = Image.Resampling.LANCZOS
            except AttributeError:
                resample_method = Image.ANTIALIAS
            image = image.resize(new_size, resample_method)
            new_img = Image.new("RGB", target_size, (255, 255, 255))
            new_img.paste(image, ((target_size[0]-new_size[0])//2, (target_size[1]-new_size[1])//2))
            return new_img

        fg = resize_with_padding(fg, (512, 512))
        mask = resize_with_padding(mask, (512, 512))
        print("FG size:", fg.size)
        print("Mask size:", mask.size)


        generator = torch.Generator()
        if device.type == "cuda":
            generator = generator.cuda()
        generator = generator.manual_seed(42)
        with torch.inference_mode():
         result = bg_pipeline(
                prompt=prompt,
            image=fg,
            mask_image=mask,
            generator=generator,
            guidance_scale=7.5,
            num_inference_steps=50
            ).images[0]

        img_io = io.BytesIO()
        result.save(img_io, format='PNG')
        img_io.seek(0)
        return send_file(img_io, mimetype='image/png')

    except Exception as e:
        print("❌ Diffusion failed:", e)

        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=True)
