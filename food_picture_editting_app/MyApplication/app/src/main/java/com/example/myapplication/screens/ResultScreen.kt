package com.example.myapplication.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx. compose. ui. layout. ContentScale

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx. compose. ui. input. pointer. pointerInput
import androidx. compose. ui. graphics. nativeCanvas
import androidx. compose. ui. graphics. Paint
import android. R. attr. textSize
import android. R. attr. typeface
import androidx. compose. foundation. background
import androidx. compose. foundation. clickable
import androidx. compose. foundation. gestures. detectTapGestures
import androidx. compose. material. icons. filled. ArrowDropDown
import android.graphics.Typeface
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import kotlin.math.pow
import androidx. compose. foundation. shape. RoundedCornerShape
import androidx. compose. ui. draw. clip
import androidx. compose. ui. text. style. TextAlign
import androidx. compose. material. icons. filled. Delete
import androidx. compose. material. icons. filled. Edit
import android. util. Log
import androidx.compose.ui.text.font.FontWeight



@Composable
fun ResultScreen(navController: NavController) {
    val resultBitmap = navController.previousBackStackEntry?.savedStateHandle?.get<Bitmap>("finalResult")
    val context = LocalContext.current

    var showTextDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var showFontMenu by remember { mutableStateOf(false) }

    val textOverlays = remember { mutableStateListOf<TextOverlay>() }
    var selectedTextIndex by remember { mutableStateOf(-1) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(TextFieldValue("")) }


    Box(modifier = Modifier.fillMaxSize()) {
        resultBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(bmp.width.toFloat() / bmp.height)
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val selected = textOverlays.indexOfLast { overlay ->
                            val dx = tapOffset.x - overlay.x.value
                            val dy = tapOffset.y - overlay.y.value
                            dx.pow(2) + dy.pow(2) < 10000f
                        }
                        selectedTextIndex = selected
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        if (selectedTextIndex >= 0) {
                            textOverlays[selectedTextIndex].x.value += dragAmount.x
                            textOverlays[selectedTextIndex].y.value += dragAmount.y
                        }
                    }
                }
            ) {
                textOverlays.forEach { overlay ->
                    val paint = android.graphics.Paint().apply {
                        color = overlay.color.toArgb()
                        textSize = overlay.fontSize.value * density
                        isAntiAlias = true
                        isUnderlineText = overlay.underline.value
                        typeface = Typeface.create(
                            when (overlay.fontFamily) {
                                FontFamily.SansSerif -> Typeface.SANS_SERIF
                                FontFamily.Serif -> Typeface.SERIF
                                FontFamily.Monospace -> Typeface.MONOSPACE
                                else -> Typeface.DEFAULT
                            },
                            when {
                                overlay.bold.value && overlay.italic.value -> Typeface.BOLD_ITALIC
                                overlay.bold.value -> Typeface.BOLD
                                overlay.italic.value -> Typeface.ITALIC
                                else -> Typeface.NORMAL
                            }
                        )
                    }
                    val canvas = drawContext.canvas.nativeCanvas
                    canvas.save()
                    canvas.rotate(
                        overlay.rotationAngle.value,
                        overlay.x.value,
                        overlay.y.value
                    )
                    canvas.drawText(
                        overlay.text.value,
                        overlay.x.value,
                        overlay.y.value,
                        paint
                    )
                    canvas.restore()
                    /*drawContext.canvas.nativeCanvas.drawText(
                        overlay.text,
                        overlay.x.value,
                        overlay.y.value,
                        paint*/

                }
            }

            IconButton(onClick = {
                val saved = saveBitmapToGallery_2(context, bmp, textOverlays)
                Toast.makeText(context, if (saved) "Saved!" else "Failed to save", Toast.LENGTH_SHORT).show()
            },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
            }

            IconButton(onClick = { showTextDialog = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
            ) {
                Icon(Icons.Filled.TextFields, contentDescription = null)
            }
        }

        if (selectedTextIndex >= 0) {
            val selected = textOverlays[selectedTextIndex]
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFFEDEDED))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Font:", modifier = Modifier.offset(x = (-15).dp))
                        IconButton(onClick = { showFontMenu = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showFontMenu,
                            onDismissRequest = { showFontMenu = false }
                        ) {
                            listOf(
                                "Default" to FontFamily.Default,
                                "Sans" to FontFamily.SansSerif,
                                "Serif" to FontFamily.Serif,
                                "Mono" to FontFamily.Monospace,
                                "Cursive" to FontFamily.Cursive
                            ).forEach { (label, font) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selected.fontFamily = font
                                        showFontMenu = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        listOf(
                            Triple("B", selected.bold, { selected.bold.value = !selected.bold.value }),
                            Triple("I", selected.italic, { selected.italic.value = !selected.italic.value }),
                            Triple("U", selected.underline, { selected.underline.value = !selected.underline.value })
                        ).forEach { (label, state, toggle) ->
                            Button(
                                onClick = toggle,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = if (state.value) Color.Gray else Color(0xFFE0E0E0)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (selectedTextIndex >= 0) {
                                editedText = TextFieldValue(textOverlays[selectedTextIndex].text.value)
                                showEditDialog = true
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Text")
                        }

                        IconButton(
                            onClick = {
                                if (selectedTextIndex >= 0) {
                                    textOverlays.removeAt(selectedTextIndex)
                                    selectedTextIndex = -1
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Text")
                        }
                    }
                }



                Spacer(Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp) // khoảng cách giữa 2 nhóm
                ) {
                    // ==== SIZE ====
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Size:")
                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { if (selected.fontSize.value > 12f) selected.fontSize.value -= 2f },
                            modifier = Modifier.size(35.dp).clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().offset(y = (-7).dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text("-", fontSize = 40.sp, color = Color.Black)
                            }
                        }

                        Text(
                            text = "${selected.fontSize.value.toInt()} sp",
                            modifier = Modifier.padding(horizontal = 8.dp).width(50.dp),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { if (selected.fontSize.value < 1000f) selected.fontSize.value += 2f },
                            modifier = Modifier.size(35.dp).clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().offset(y = (0).dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text("+", fontSize = 30.sp, color = Color.Black)
                            }
                        }
                    }

                    // ==== ROTA ====
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rota:")
                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { selected.rotationAngle.value -= 5f },
                            modifier = Modifier.size(35.dp).clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().offset(y = (-7).dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text("-", fontSize = 40.sp, color = Color.Black)
                            }
                        }

                        Text(
                            text = "${selected.rotationAngle.value.toInt()}°",
                            modifier = Modifier.padding(horizontal = 8.dp).width(50.dp),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { selected.rotationAngle.value += 5f },
                            modifier = Modifier.size(35.dp).clip(RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+", fontSize = 30.sp, color = Color.Black, textAlign = TextAlign.Center)
                        }
                    }
                }


                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Color:")
                    Spacer(Modifier.width(8.dp))
                    listOf(
                        Color.White,
                        Color.Black,
                        Color.Red,
                        Color.Blue,
                        Color.Green,
                        Color.Yellow,
                        Color.Cyan,
                        Color.Magenta,
                        Color.Gray,
                        Color.LightGray,
                        Color(0xFFFFA500), // Orange
                        Color(0xFF800080), // Purple
                        Color(0xFF00FFFF), // Aqua
                        Color(0xFF008080), // Teal
                        Color(0xFF808000)).forEach { colorOption ->
                        Box(modifier = Modifier
                            .size(24.dp)
                            .background(colorOption)
                            .clickable { selected.color = colorOption })
                        Spacer(Modifier.width(4.dp))
                    }
                }

            }
        }}

        if (showTextDialog) {
            AlertDialog(
                onDismissRequest = { showTextDialog = false },
                title = { Text("Enter Text") },
                text = {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Text", color = Color.Gray.copy(alpha = 0.5f)) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (inputText.text.isNotBlank()) {
                            textOverlays.add(
                                TextOverlay(
                                    text = mutableStateOf(inputText.text),
                                    x = mutableStateOf(390f),
                                    y = mutableStateOf(870f)
                                )
                            )
                            inputText = TextFieldValue("")
                            showTextDialog = false
                        }
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF87CEEB), // 💡 màu nền nút "Next"
                            contentColor = Color.White        // 💬 màu chữ "Next"
                        )) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTextDialog = false }) {
                        Text("Cancel",
                            color = Color.Gray)
                    }
                }
            )
        }
        if (showEditDialog && selectedTextIndex >= 0) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Text") },
                text = {
                    OutlinedTextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        label = { Text("Text", color = Color.Gray.copy(alpha = 0.5f)) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        textOverlays[selectedTextIndex].text.value = editedText.text
                        showEditDialog = false
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF87CEEB), // 💡 màu nền nút "Next"
                            contentColor = Color.White        // 💬 màu chữ "Next"
                        )
                    ) {
                        Text("Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }


fun saveBitmapToGallery_2(context: Context, bitmap: Bitmap, overlays: List<TextOverlay>): Boolean {
    val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(resultBitmap)

    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels.toFloat()
    val screenHeight = displayMetrics.heightPixels.toFloat()

    val imageWidth = bitmap.width.toFloat()    // 512
    val imageHeight = bitmap.height.toFloat()  // 512

    val scaleFactor = screenWidth / imageWidth // scale = 2.11

    val scaledImageHeight = imageHeight * scaleFactor // = 1080
    val verticalPadding = (screenHeight - scaledImageHeight) / 2f

    overlays.forEach { overlay ->
        val paint = android.graphics.Paint().apply {
            color = overlay.color.toArgb()                     // returns Int, ok for Paint
            textSize = overlay.fontSize.value * displayMetrics.scaledDensity / scaleFactor
            isAntiAlias = true
            val baseTypeface = when (overlay.fontFamily) {
                FontFamily.SansSerif -> Typeface.SANS_SERIF
                FontFamily.Serif -> Typeface.SERIF
                FontFamily.Monospace -> Typeface.MONOSPACE
                else -> Typeface.DEFAULT
            }
            val style = when {
                overlay.bold.value && overlay.italic.value -> Typeface.BOLD_ITALIC
                overlay.bold.value -> Typeface.BOLD
                overlay.italic.value -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            typeface = Typeface.create(baseTypeface, style)

            // === Gạch chân ===
            isUnderlineText = overlay.underline.value
        }

        // Scale position from UI → bitmap size
        val x = overlay.x.value / scaleFactor
        val y = (overlay.y.value - verticalPadding) / scaleFactor
        canvas.save()
        try {
            canvas.rotate(
                overlay.rotationAngle.value,
                x,
                y
            )
            canvas.drawText(overlay.text.value, x, y, paint)
        } catch (e: Exception) {
            Log.e("SaveBitmap", "Draw error", e)
        } finally {
            canvas.restore()
        }
    }

    val filename = "generated_${System.currentTimeMillis()}.png"
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/GeneratedImages")
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        resolver.openOutputStream(uri)?.use { out ->
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
        return true
    }
    return false
}


// Data class
data class TextOverlay(
    var text: MutableState<String>,
    var x: MutableState<Float>,
    var y: MutableState<Float>,
    var color: Color = Color.Black,
    var fontSize: MutableState<Float> = mutableStateOf(24f),
    var fontFamily: FontFamily = FontFamily.Default,
    var rotationAngle: MutableState<Float> = mutableStateOf(0f),
    var bold: MutableState<Boolean> = mutableStateOf(false),
    var italic: MutableState<Boolean> = mutableStateOf(false),
    var underline: MutableState<Boolean> = mutableStateOf(false)
)