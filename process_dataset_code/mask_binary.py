from PIL import Image
import os
import numpy as np

input_dir = "dataset/UECFoodPIXCOMPLETE/test/mask_resized"  # hoặc test/mask
output_dir = "dataset/UECFoodPIXCOMPLETE/test/mask_binary"

os.makedirs(output_dir, exist_ok=True)

for filename in os.listdir(input_dir):
    if filename.lower().endswith(('.png', '.jpg', '.jpeg')):
        path = os.path.join(input_dir, filename)
        img = Image.open(path).convert("L")  # convert to grayscale

        # Tạo mask nhị phân: giữ 0, mọi giá trị khác 0 thành 1
        arr = np.array(img)
        binary_arr = np.where(arr > 0, 1, 0).astype(np.uint8)

        binary_img = Image.fromarray(binary_arr * 255)  # nhân 255 để lưu ảnh đen trắng rõ hơn
        binary_img.save(os.path.join(output_dir, filename))
