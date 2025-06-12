package com.example.myapplication

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.screens.PreviewImageScreen
import com.example.myapplication.screens.SelectImageScreen
import com.example.myapplication.screens.EditScreen
import com.example.myapplication.screens.ResultScreen
import androidx. compose. material3.Surface
import androidx. compose. ui. Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme
              Surface(
                  modifier = Modifier.fillMaxSize(),
                  color = Color(0xFFBCAAA4) // 💡 Ví dụ: pastel xanh xám
              ){
                val navController = rememberNavController()
                val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
                val uriState = remember { mutableStateOf<Uri?>(null) }

                NavHost(navController = navController, startDestination = "select") {
                    composable("select") {
                        SelectImageScreen(
                            onImageSelected = { uri, bmp ->
                                uriState.value = uri
                                bitmapState.value = bmp
                                navController.navigate("preview")
                            }
                        )
                    }
                    composable("preview") {
                        PreviewImageScreen(
                            bitmap = bitmapState.value,
                            imageUri = uriState.value,
                            onBack = { navController.popBackStack() },
                            onSegment = { /* TODO: Add segmentation logic */ },
                            navController = navController
                        )
                    }

                    composable("edit") {
                        val segmentedBitmap = navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<Bitmap>("segmentedBitmap")
                        EditScreen(
                            segmentedBitmap = segmentedBitmap,
                            navController = navController
                        )
                    }

                    composable("result") {
                        ResultScreen(navController = navController)
                    }

                }
            }
        }
    }
}
