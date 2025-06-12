package com.example.myapplication.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.InputStream
import com.example.myapplication.network.SegmentApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.exifinterface.media.ExifInterface
import android.content.Context
import android.graphics.Matrix
import androidx.compose.ui.unit.sp



@Composable
fun PreviewImageScreen(
    bitmap: Bitmap?,
    imageUri: Uri?,
    onBack: () -> Unit,
    onSegment: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (bitmap != null) {
                val rotatedBitmap = bitmap

                val aspectRatio = rotatedBitmap.width.toFloat() / rotatedBitmap.height

                Image(
                    bitmap = rotatedBitmap.asImageBitmap(),
                    contentDescription = "Preview Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .padding(8.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        }
                )
            } else if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        }
                        .heightIn(max = 512.dp),
                    contentScale = ContentScale.Fit
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            }
        }

        if (bitmap != null) {
            Text(
                text = "${bitmap.width} x ${bitmap.height}",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        else if (imageUri != null) {
            val imageBitmap = loadBitmapFromUri_2(context, imageUri)
            if (imageBitmap != null) {
                Text(
                    text = "${imageBitmap.width} x ${imageBitmap.height}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F2F5),
                    contentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(55.dp))
                    Text("Back", modifier = Modifier.weight(1f))
                }
            }

            Button(
                onClick = {
                    if (imageUri != null) {
                        coroutineScope.launch {
                            isLoading = true
                            val responseBody: ResponseBody? = try {
                                SegmentApi.uploadImage(context, imageUri)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                            isLoading = false

                            if (responseBody != null) {
                                val inputStream = responseBody.byteStream()
                                val file = File(context.cacheDir, "segmented_${System.currentTimeMillis()}.png")
                                inputStream.use { input ->
                                    FileOutputStream(file).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                val segmentedBitmap = BitmapFactory.decodeFile(file.absolutePath)
                                navController.currentBackStackEntry?.savedStateHandle?.apply {
                                    set("segmentedBitmap", segmentedBitmap)
                                    set("segmentedFile", file)
                                }
                                navController.navigate("edit")
                            } else {
                                println("❌ Failed to upload or get response from server")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F2F5),
                    contentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_forward_24),
                        contentDescription = "Segment",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text("Segment Image", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

fun loadBitmapFromUri_2(context: Context, uri: Uri): Bitmap? {
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    }
}



