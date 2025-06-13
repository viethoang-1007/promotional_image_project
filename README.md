## The project structure:
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
- Download dataset folder:   https://drive.google.com/file/d/1DLY2i6yCQAjwhEeOVg7WWZ6ylj03JLZy/view?usp=sharing.
- Download deeplabv3_resnet101_epoch25.pth:   https://drive.google.com/file/d/10SJCnhBSUj46INFg2wz1CiBQD0D1HYdK/view?usp=sharing.
- in the process_dataset_code:
    + use resized_image to resize the image and mask in dataset into 512 x 512

