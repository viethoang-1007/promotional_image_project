package com.example.myapplication.screens


import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.runtime.*

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx. compose. ui. layout. onGloballyPositioned
import androidx. compose. foundation. gestures. detectDragGestures
import androidx. compose. foundation. background
import androidx. compose. foundation. shape. CircleShape
import androidx.compose.material.icons.filled.Circle
import androidx. compose. foundation. shape. RoundedCornerShape
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.navigation.NavController




// Segment API
import com.example.myapplication.network.SegmentApi


fun applyAdjustments(original: Bitmap, brightness: Float, contrast: Float, exposure: Float): Bitmap {
    val bmp = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint()
    val cm = ColorMatrix()

    val brightnessMatrix = ColorMatrix().apply { setScale(brightness, brightness, brightness, 1f) }
    val contrastScale = contrast
    val contrastTranslate = (-0.5f * contrastScale + 0.5f) * 255f
    val contrastMatrix = ColorMatrix(floatArrayOf(
        contrastScale, 0f, 0f, 0f, contrastTranslate,
        0f, contrastScale, 0f, 0f, contrastTranslate,
        0f, 0f, contrastScale, 0f, contrastTranslate,
        0f, 0f, 0f, 1f, 0f
    ))
    val exposureMatrix = ColorMatrix().apply { setScale(exposure, exposure, exposure, 1f) }

    cm.postConcat(brightnessMatrix)
    cm.postConcat(contrastMatrix)
    cm.postConcat(exposureMatrix)

    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(original, 0f, 0f, paint)
    return bmp
}

fun rotateBitmap90(original: Bitmap): Bitmap {
    val matrix = Matrix().apply { postRotate(90f) }
    return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
}

fun flipBitmapVertical(original: Bitmap): Bitmap {
    val matrix = Matrix().apply { preScale(-1f, 1f) }
    return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    val filename = "edited_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SegmentApp")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        resolver.openOutputStream(it)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return true
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(segmentedBitmap: Bitmap?,
               navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var brightness by remember { mutableFloatStateOf(1f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var exposure by remember { mutableFloatStateOf(1f) }

    val history = remember { mutableStateListOf<Pair<Bitmap, Triple<Float, Float, Float>>>() }
    var currentIndex by remember { mutableIntStateOf(0) }

    if (segmentedBitmap != null && history.isEmpty()) {
        history.add(segmentedBitmap.copy(Bitmap.Config.ARGB_8888, true) to Triple(1f, 1f, 1f))
    }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var eraserEnabled by remember { mutableStateOf(false) }
    var eraserRadius by remember { mutableFloatStateOf(30f) }
    var workingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var eraserPreviewRefresh by remember { mutableStateOf(0) }
    val (rawBitmap, adjustmentTriple) = history.getOrNull(currentIndex) ?: return
    brightness = adjustmentTriple.first
    contrast = adjustmentTriple.second
    exposure = adjustmentTriple.third

    val displayBitmap = remember(rawBitmap, workingBitmap, brightness, contrast, exposure,  eraserPreviewRefresh) {
        applyAdjustments(workingBitmap ?: rawBitmap, brightness, contrast, exposure)
    }

    var imageWidthPx by remember { mutableStateOf(1) }
    var imageHeightPx by remember { mutableStateOf(1) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (segmentedBitmap != null) {
            Image(
                bitmap = displayBitmap.asImageBitmap(),
                contentDescription = "Segmented Image",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.2f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                    .onGloballyPositioned {
                        imageWidthPx = it.size.width
                        imageHeightPx = it.size.height
                    }
            )

            if (eraserEnabled) {
                Canvas(modifier = Modifier
                    .matchParentSize()
                    .pointerInput(scale, offsetX, offsetY, eraserRadius) {
                        detectDragGestures(
                            onDragStart = { offset ->

                                if (workingBitmap == null) {
                                    workingBitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true)
                                }
                                val canvas = Canvas(workingBitmap!!)
                                val paint = Paint().apply {
                                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                                    isAntiAlias = true
                                }
                                // Actual scaled image size
                                val scaledImageWidth = imageWidthPx * scale
                                val scaledImageHeight = imageHeightPx * scale

                                // Offset of image center on screen
                                val centerX = imageWidthPx / 2f
                                val centerY = imageHeightPx / 2f

                                // Position in image space
                                val imageX = (offset.x - offsetX - centerX + scaledImageWidth / 2) / scale
                                val imageY = (offset.y - offsetY - centerY + scaledImageHeight / 2) / scale

                                val bmpX = imageX * (rawBitmap.width.toFloat() / imageWidthPx)
                                val bmpY = imageY * (rawBitmap.height.toFloat() / imageHeightPx)
                                canvas.drawCircle(bmpX, bmpY, eraserRadius, paint)
                                eraserPreviewRefresh++

                            },
                            onDrag = { change, _ ->

                                workingBitmap?.let {
                                    val canvas = Canvas(it)
                                    val paint = Paint().apply {
                                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                                        isAntiAlias = true
                                    }

                                    val scaledImageWidth = imageWidthPx * scale
                                    val scaledImageHeight = imageHeightPx * scale
                                    val centerX = imageWidthPx / 2f
                                    val centerY = imageHeightPx / 2f

                                    val imageX = (change.position.x - offsetX - centerX + scaledImageWidth / 2) / scale
                                    val imageY = (change.position.y - offsetY - centerY + scaledImageHeight / 2) / scale

                                    val bmpX = imageX * (rawBitmap.width.toFloat() / imageWidthPx)
                                    val bmpY = imageY * (rawBitmap.height.toFloat() / imageHeightPx)

                                    canvas.drawCircle(bmpX, bmpY, eraserRadius, paint)
                                    eraserPreviewRefresh++

                                }
                            }

                        )
                    }
                ) {


                }
            }

            Text(
                text = "${displayBitmap.width} x ${displayBitmap.height}",
                color = androidx.compose.ui.graphics.Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        AdjustmentMenu(
            brightness = brightness,
            onBrightnessChange = {
                brightness = it
                history[currentIndex] = (workingBitmap ?: rawBitmap) to Triple(brightness, contrast, exposure)
            },
            contrast = contrast,
            onContrastChange = {
                contrast = it
                history[currentIndex] = (workingBitmap ?: rawBitmap) to Triple(brightness, contrast, exposure)
            },
            exposure = exposure,
            onExposureChange = {
                exposure = it
                history[currentIndex] = (workingBitmap ?: rawBitmap) to Triple(brightness, contrast, exposure)
            }
        )

        IconButton(onClick = { if (currentIndex > 0) currentIndex-- }, modifier = Modifier.offset(75.dp, 770.dp)) {
            Icon(Icons.Filled.Undo, contentDescription = "Undo")
        }
        IconButton(onClick = { if (currentIndex < history.lastIndex) currentIndex++ }, modifier = Modifier.offset(130.dp, 770.dp)) {
            Icon(Icons.Filled.Redo, contentDescription = "Redo")
        }
        IconButton(onClick = {
            val rotated = rotateBitmap90(workingBitmap ?: rawBitmap)
            history.add(rotated to Triple(brightness, contrast, exposure))
            currentIndex = history.lastIndex
            workingBitmap = null
        }, modifier = Modifier.offset(185.dp, 770.dp)) {
            Icon(Icons.Filled.RotateRight, contentDescription = "Rotate")
        }
        IconButton(onClick = {
            val flipped = flipBitmapVertical(workingBitmap ?: rawBitmap)
            history.add(flipped to Triple(brightness, contrast, exposure))
            currentIndex = history.lastIndex
            workingBitmap = null
        }, modifier = Modifier.offset(240.dp, 770.dp)) {
            Icon(Icons.Filled.Flip, contentDescription = "Mirror")
        }
        IconButton(onClick = {
            val saved = saveBitmapToGallery(context, displayBitmap)
            Toast.makeText(context, if (saved) "Saved!" else "Failed to save", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.offset(350.dp, 770.dp)) {
            Icon(Icons.Filled.Save, contentDescription = "Save")
        }
        IconButton(onClick = {
            if (eraserEnabled && workingBitmap != null) {
                history.add(workingBitmap!! to Triple(brightness, contrast, exposure))
                currentIndex = history.lastIndex
                workingBitmap = null
            }
            eraserEnabled = !eraserEnabled
        }, modifier = Modifier.offset(295.dp, 770.dp)) {
            Icon(
                imageVector = if (eraserEnabled) Icons.Filled.HighlightOff else Icons.Filled.Circle,
                contentDescription = "Eraser"
            )
        }

        if (eraserEnabled) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 50.dp, bottom = 70.dp)
                    .width(320.dp)

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(23.dp)
                ) {
                    Canvas(modifier = Modifier.size(40.dp)) {
                        drawCircle(
                            color = Color.Black,
                            radius = eraserRadius * (size.minDimension / 100f)

                        )

                    }
                    Text("Eraser Size", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(0.dp)) // 📏 Khoảng cách ngắn lại

                Slider(
                    value = eraserRadius,
                    onValueChange = { eraserRadius = it },
                    valueRange = 1f..100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    thumb = {
                        // Vẫn dùng thumb mặc định nếu muốn
                        Box(
                            Modifier
                                .size(14.dp)
                                .background(Color.LightGray, shape = CircleShape)
                        )
                    },
                    track = { sliderPositions ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp) // 👈 Chiều cao thanh trượt
                                .background(Color.Black) // Toàn track
                        )


                    }
                )
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }
        }
        }
    var showPromptDialog by remember { mutableStateOf(false) }

    if (showPromptDialog) {
        PromptDialog(
            onConfirm = { prompt ->
                showPromptDialog = false
                isLoading = true
                coroutineScope.launch {
                    val promptBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())
                    val resultBitmap = SegmentApi.uploadForBackground(context, displayBitmap, promptBody)

                    isLoading = false

                    if (resultBitmap != null) {
                        navController.currentBackStackEntry?.savedStateHandle?.set("finalResult", resultBitmap)
                        navController.navigate("result")
                    } else {
                        Toast.makeText(context, "Error generating background", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showPromptDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 16.dp, top = 16.dp)
    ) {
        Button(
            onClick = { showPromptDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd) // 👈 Đặt lên góc phải trên
                .width(180.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Background", fontSize = 16.sp)
        }
    }


}









