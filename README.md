# Development of AI Image Editing Solution for Restaurant Marketing
## The project structure
```
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
```

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

- `fine_tuned_deeplabv3+.py`  
  ➤ Script for fine-tuning the DeepLabV3+ model on the processed dataset.

## Run application
1. Run the server
   Execute the script `new_server_2.py` to start the segmentation server:
   ```bash
   python new_server_2.py
2. Open Android Studio  
3. Open project:  
   Navigate to `food_picture_editing_app/MyApplication`.
4. Gradle Sync:  
   Let Android Studio sync and download any required dependencies.
5. Configure API endpoint in SegmentApi.kt:  
   Open SegmentApi.kt and change the base URL to match PC’s IP address.
6. Run the App:  
   - Connect your Android phone via USB (with USB debugging enabled), or run an emulator.  
   - Click Run or press Shift + F10 to build and launch the app.
