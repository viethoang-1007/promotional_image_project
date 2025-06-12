import os
from PIL import Image
import numpy as np
import torch
from torch import nn
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
import matplotlib.pyplot as plt
from torchvision.models.segmentation import deeplabv3_resnet101, DeepLabV3_ResNet101_Weights

    
class FoodSegDataset(Dataset):
    def __init__(self, image_dir, mask_dir, transform=None):
        self.image_dir = image_dir
        self.mask_dir = mask_dir
        self.transform = transform
        self.images = sorted(os.listdir(image_dir))
        self.masks = sorted(os.listdir(mask_dir))

    def __len__(self):
        return len(self.images)

    def __getitem__(self, idx):
        image = Image.open(os.path.join(self.image_dir, self.images[idx])).convert('RGB')
        mask = Image.open(os.path.join(self.mask_dir, self.masks[idx])).convert('L')
        
        if self.transform:
           image = self.transform(image)

        mask = transforms.ToTensor()(mask).long().squeeze(0)
        mask = (mask > 0).long()  # Convert 255 -> 1

        return image, mask


def compute_metrics(pred, target, num_classes):
    pred = pred.flatten()
    target = target.flatten()

    ious = []
    dices = []

    for cls in range(1, num_classes):
        pred_inds = pred == cls
        target_inds = target == cls
        intersection = (pred_inds & target_inds).sum()
        union = (pred_inds | target_inds).sum()
        iou = intersection / union if union != 0 else 1.0
        dice = 2 * intersection / (pred_inds.sum() + target_inds.sum()) if (pred_inds.sum() + target_inds.sum()) != 0 else 1.0
        ious.append(iou)
        dices.append(dice)

    return np.mean(ious), np.mean(dices)

if __name__ == "__main__":
    train_img_dir = "dataset/processed_dataset/train/img_resized"
    train_mask_dir = "dataset/processed_dataset/train/mask_binary"
    test_img_dir = "dataset/processed_dataset/test/img_resized"
    test_mask_dir = "dataset/processed_dataset/test/mask_binary"

    transform = transforms.Compose([
    transforms.ToTensor()
    ])

    train_dataset = FoodSegDataset(train_img_dir, train_mask_dir, transform=transform)
    test_dataset = FoodSegDataset(test_img_dir, test_mask_dir, transform=transform)

    train_loader = DataLoader(train_dataset, batch_size=4, shuffle=True, num_workers=2)
    test_loader = DataLoader(test_dataset, batch_size=4, shuffle=False)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    num_classes = 2  # binary segmentation

    # Load pretrained DeepLabV3+ with ResNet101 backbone
    weights = DeepLabV3_ResNet101_Weights.DEFAULT
    model = deeplabv3_resnet101(weights=weights)

    # Modify classifier head for binary segmentation
    model.classifier[4] = nn.Conv2d(256, num_classes, kernel_size=1)
    model = model.to(device)

    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=1e-4)

    num_epochs = 50
    for epoch in range(num_epochs):
        model.train()
        running_loss = 0.0
        for images, masks in train_loader:
            images, masks = images.to(device), masks.to(device)

            if masks.dim() == 4:
                masks = masks.squeeze(1)

            outputs = model(images)['out']
            loss = criterion(outputs, masks)

            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

            running_loss += loss.item()

        print(f"Epoch [{epoch+1}/{num_epochs}] - Loss: {running_loss / len(train_loader):.4f}")

        if (epoch + 1) % 5 == 0:
            save_path = f"deeplabv3_resnet101_epoch{epoch+1}.pth"
            torch.save(model.state_dict(), save_path)
            print(f"\u2705 Model saved at epoch {epoch+1} -> {save_path}")

    # Evaluation
    model.eval()
    total_iou = 0.0
    total_dice = 0.0
    count = 0

    with torch.no_grad():
        for i, (img, mask_true) in enumerate(test_loader):
            img = img.to(device)
            mask_true = mask_true.squeeze().cpu().numpy()

            output = model(img)['out']
            pred = torch.argmax(output.squeeze(), dim=0).cpu().numpy()

            iou, dice = compute_metrics(pred, mask_true, num_classes)
            total_iou += iou
            total_dice += dice
            count += 1

            if i < 50:
                plt.figure(figsize=(12, 4))
                plt.subplot(1, 3, 1)
                plt.imshow(transforms.ToPILImage()(img.squeeze().cpu()))
                plt.title("Original")
                plt.subplot(1, 3, 2)
                plt.imshow(mask_true, cmap='gray')
                plt.title("Ground Truth")
                plt.subplot(1, 3, 3)
                plt.imshow(pred, cmap='gray')
                plt.title(f"Predicted (IoU={iou:.2f})")
                plt.tight_layout()
                plt.show()

    print(f"\n\u2705 Average IoU:  {total_iou / count:.4f}")
    print(f"\u2705 Average Dice: {total_dice / count:.4f}")