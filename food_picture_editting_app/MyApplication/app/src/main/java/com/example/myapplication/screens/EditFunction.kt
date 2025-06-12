package com.example.myapplication.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx. compose. foundation. background
import androidx.compose.foundation.shape.CircleShape
import androidx. compose. ui. graphics. vector. ImageVector


enum class AdjustmentOption {
    BRIGHTNESS, CONTRAST, EXPOSURE
}

@Composable
fun AdjustmentMenu(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    exposure: Float,
    onExposureChange: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(AdjustmentOption.BRIGHTNESS) }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.offset(x = 20.dp, y = 770.dp)
        ) {
            Icon(imageVector = Icons.Filled.Tune, contentDescription = "Adjust")
        }

        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .offset(x = 0.dp, y = 0.dp)
            ) {
                Column(modifier = Modifier
                    .padding(start = 30.dp, top = 1.dp, end = 30.dp, bottom = 1.dp)
                    .width(405.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { selectedOption = AdjustmentOption.BRIGHTNESS }) {
                            Text(text = "Brightness",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black)
                        }
                        TextButton(onClick = { selectedOption = AdjustmentOption.CONTRAST }) {
                            Text(text= "Contrast",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black)
                        }
                        TextButton(onClick = { selectedOption = AdjustmentOption.EXPOSURE }) {
                            Text(text= "Exposure",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(0.dp))

                    when (selectedOption) {
                        AdjustmentOption.BRIGHTNESS -> {
                            AdjustmentSlider(
                                icon = Icons.Default.WbSunny,
                                value = brightness,
                                onValueChange = onBrightnessChange,
                                label = "Brightness"
                            )
                        }

                        AdjustmentOption.CONTRAST -> {
                            AdjustmentSlider(
                                icon = Icons.Default.Contrast, // Dùng Exposure làm icon thay thế
                                value = contrast,
                                onValueChange = onContrastChange,
                                label = "Contrast"
                            )
                        }

                        AdjustmentOption.EXPOSURE -> {
                            AdjustmentSlider(
                                icon = Icons.Default.Exposure,
                                value = exposure,
                                onValueChange = onExposureChange,
                                label = "Exposure"
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentSlider(
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = Color.Black, modifier = Modifier.size(20.dp))

        Slider(
            value = value * 100f, // chuyển từ 0f–2f thành 0f–200f để người dùng thấy dễ hiểu
            onValueChange = { onValueChange(it / 100f) }, // chuyển lại từ 0f–200f về 0f–2f khi lưu
            valueRange = 0f..200f,
            modifier = Modifier
                .height(12.dp),
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
                        .width(270.dp)
                        .height(4.dp) // 👈 Chiều cao thanh trượt
                        .background(Color.Black) // Toàn track
                )


            })


        Text(
            text = "%.0f".format(value * 100),
            modifier = Modifier.padding(start = 10.dp).width(40.dp),
            color = Color.Black
        )
    }
}

   /* {
            Column(modifier = Modifier
                        .padding(start = 30.dp, top = 1.dp, end = 30.dp, bottom = 1.dp)
                        .width(405.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp)
                ) {
                    Text(
                        text = "Brightness",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.WbSunny, contentDescription = "Brightness", tint = Color.Black,
                        modifier = Modifier.size(20.dp))

                    Slider(
                        value = brightness,
                        onValueChange = onBrightnessChange,
                        valueRange = 0f..2f,
                        modifier = Modifier
                            .graphicsLayer(scaleY = 0.6f) // 👈 Scale chiều cao toàn bộ Slider
                            .width(290.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF90CAF9),
                            activeTrackColor = Color.Black,
                            inactiveTrackColor = Color(0xFFB0BEC5)
                        )
                    )

                    Text(
                        text = "%.0f".format(brightness * 100),
                        modifier = Modifier.padding(start = 10.dp).width(40.dp),
                        color = Color.Black
                    )
                }

                Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 0.dp)
                    ) {
                        Text(
                            text = "Contrast",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Contrast, contentDescription = "Contrast", tint = Color.Black,
                                modifier = Modifier.size(20.dp))

                            Slider(
                                value = contrast,
                                onValueChange = onContrastChange,
                                valueRange = 0f..2f,
                                modifier = Modifier
                                    .graphicsLayer(scaleY = 0.6f)
                                    .width(290.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF90CAF9),
                                    activeTrackColor = Color.Black,
                                    inactiveTrackColor = Color(0xFFB0BEC5)
                                )
                            )

                            Text(
                                text = "%.0f".format(contrast * 100),
                                modifier = Modifier.padding(start = 10.dp).width(40.dp),
                                color = Color.Black
                            )
                        }
                    }


                Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 0.dp)
                    ) {
                        Text(
                            text = "Exposure",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Exposure, contentDescription = "Exposure", tint = Color.Black,
                                modifier = Modifier.size(20.dp))

                            Slider(
                                value = exposure,
                                onValueChange = onExposureChange,
                                valueRange = 0f..2f,
                                modifier = Modifier
                                    .graphicsLayer(scaleY = 0.6f)
                                    .width(290.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF90CAF9),
                                    activeTrackColor = Color.Black,
                                    inactiveTrackColor = Color(0xFFB0BEC5)
                                )
                            )

                            Text(
                                text = "%.0f".format(exposure * 100),
                                modifier = Modifier.padding(start = 10.dp).width(40.dp),
                                color = Color.Black
                            )
                        }
                    }

                }
        }
    }
}}}}  */
