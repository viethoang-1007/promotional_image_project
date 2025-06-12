package com.example.myapplication.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

interface SegmentApiService {
    @Multipart
    @POST("/segment")
    suspend fun segmentImage(
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("/generate_background")
    suspend fun generateBackgroundImage(
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<ResponseBody>
}

object SegmentApi {
    private val retrofit by lazy {

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)   // tối đa thời gian kết nối
            .readTimeout(20, TimeUnit.MINUTES)     // ⏳ chờ phản hồi tối đa 20 phút
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()

        Retrofit.Builder()
            .baseUrl("http://172.30.1.43:5000/")  // <--- phải đúng IP + port server Flask!
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: SegmentApiService by lazy {
        retrofit.create(SegmentApiService::class.java)
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): ResponseBody? {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val file = File.createTempFile("upload", ".jpg")
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        val response = service.segmentImage(body)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun uploadForBackground(
        context: Context,
        bitmap: Bitmap,
        prompt: RequestBody
    ): Bitmap? {
        val file = File.createTempFile("bg_input", ".png", context.cacheDir)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val response = service.generateBackgroundImage(body, prompt)
        return if (response.isSuccessful) {
            val inputStream = response.body()?.byteStream()
            BitmapFactory.decodeStream(inputStream)
        } else null
    }

}
