package com.example.myapplication.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx. compose. material3.ButtonDefaults
import androidx. compose. ui. graphics. Color
import androidx. compose. foundation. shape. RoundedCornerShape

@Composable
fun PromptDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter background prompt") },
        text = {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Prompt", color = Color.Gray.copy(alpha = 0.5f)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(prompt) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF87CEEB), // 💡 màu nền nút "Next"
                    contentColor = Color.White        // 💬 màu chữ "Next"
                ),
                shape = RoundedCornerShape(30) // Bo tròn như ảnh bạn gửi

                ) {
                Text("Next")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel",
                color = Color.Gray)
            }
        }
    )
}