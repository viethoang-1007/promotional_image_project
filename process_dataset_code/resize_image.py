from PIL import Image, ImageOps
import os

def resize_or_pad(image: Image.Image, target_size=512, is_mask=False) -> Image.Image:
    w, h = image.size

    # Nếu cả hai chiều đều <= target_size → chỉ padding
    if w <= target_size and h <= target_size:
        delta_w = target_size - w
        delta_h = target_size - h
        padding = (
            delta_w // 2, delta_h // 2,
            delta_w - (delta_w // 2), delta_h - (delta_h // 2)
        )
        return ImageOps.expand(image, padding, fill=0)

    # Nếu có chiều > target_size → resize theo cạnh lớn nhất, giữ tỉ lệ
    scale = target_size / max(w, h)
    new_w = int(w * scale)
    new_h = int(h * scale)

    interpolation = Image.NEAREST if is_mask else Image.LANCZOS
    image = image.resize((new_w, new_h), interpolation)

    # Padding để thành hình vuông target_size x target_size
    delta_w = target_size - new_w
    delta_h = target_size - new_h
    padding = (
        delta_w // 2, delta_h // 2,
        delta_w - (delta_w // 2), delta_h - (delta_h // 2)
    )
    return ImageOps.expand(image, padding, fill=0)


input_dir = "dataset/UECFoodPIXCOMPLETE/test/img"
output_dir = "dataset/UECFoodPIXCOMPLETE/test/img_resized"
os.makedirs(output_dir, exist_ok=True)

for filename in os.listdir(input_dir):
    if filename.lower().endswith((".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff")):
        input_path = os.path.join(input_dir, filename)
        output_path = os.path.join(output_dir, filename)

        image = Image.open(input_path).convert("RGB")
        image_resized = resize_or_pad(image, 512, is_mask=False)
        image_resized.save(output_path)

print("✅ Done resizing and padding all images.")