# Development of AI Image Editing Solution for Restaurant Marketing
## The project structure
.
├── dataset
│ ├── original_dataset
│ └── processed_dataset
├── food_picture_editing_app
│ ├── .idea
│ ├── MyApplication
│ └── logo.png
├── process_dataset_code
│ ├── mask_binary.py
│ └── resize_image.py
├── deeplabv3_resnet101_epoch25.pth
├── fine_tuned_deeplabv3+.py
├── new_server_2.py

## Download link
- **Dataset folder**
  [Google Drive - Dataset]  (https://drive.google.com/file/d/1DLY2i6yCQAjwhEeOVg7WWZ6ylj03JLZy/view?usp=sharing)
  
- **Trained DeepLabV3+ model** (deeplabv3_resnet101_epoch25.pth)
  [Google Drive - Model] (https://drive.google.com/file/d/10SJCnhBSUj46INFg2wz1CiBQD0D1HYdK/view?usp=sharing)
  
## Processing Scripts
- `resize_image.py`  
  ➤ Resize the images and masks in the dataset to **512 x 512** resolution.
  
- `mask_binary.py`  
  ➤ Convert the resized masks to **binary masks** for use in training Deeplabv3+ model.
