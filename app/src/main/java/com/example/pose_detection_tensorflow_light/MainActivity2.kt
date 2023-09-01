package com.example.pose_detection_tensorflow_light

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.InputStream
import android.util.Log
import com.bumptech.glide.Glide

class MainActivity2 : ComponentActivity() {
    private lateinit var imageView: ImageView
    private val streamUrl = "http://192.168.100.24:5000/stream_video"
//    private val streamUrl  ="https://video.industriousrebel.com/stream"
    private val okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        imageView = findViewById(R.id.imageView)

        CoroutineScope(Dispatchers.Main).launch {
            fetchStream()
        }
    }
//    private suspend fun fetchStream() = withContext(Dispatchers.IO) {
//        Log.d("MainActivity2", "Fetching stream")
//        val request = Request.Builder()
//            .url(streamUrl)
//            .build()
//
//        try {
//            val response = okHttpClient.newCall(request).execute()
//            val boundary = response.header("Content-Type")?.split("boundary=")?.get(1)
//
//            if (boundary != null) {
//                val inputStream: InputStream = response.body?.byteStream() ?: return@withContext
//
//                while (isActive) {
//                    val imageBytes = readImageBytes(inputStream, boundary)
//                    Log.d("MainActivity2", "Image bytes read: ${imageBytes.size} bytes")
//
//                    // Load and display image using Glide
//                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//                    if (bitmap != null) {
//                        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageView.width, imageView.height, true)
//                        runOnUiThread {
//                            Glide.with(this@MainActivity2)
//                                .load(resizedBitmap)
//                                .into(imageView)
//                        }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("MainActivity2", "Error fetching stream: ${e.message}")
//            e.printStackTrace()
//        }
//    }
    private suspend fun fetchStream() = withContext(Dispatchers.IO) {
        Log.d("MainActivity2", "Fetching stream")
        val request = Request.Builder()
            .url(streamUrl)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val boundary = response.header("Content-Type")?.split("boundary=")?.get(1)

            if (boundary != null) {
                val inputStream: InputStream = response.body?.byteStream() ?: return@withContext

                while (isActive) {
                    val imageBytes = readImageBytes(inputStream, boundary)
                    Log.d("MainActivity2", "Image bytes read: ${imageBytes} bytes")
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    if (bitmap != null) {
                        Log.d("MainActivity2", "Bitmap created")
                        // Check bitmap size and display
                        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageView.width, imageView.height, true)
                        runOnUiThread {
                            imageView.setImageBitmap(resizedBitmap)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity2", "Error fetching stream: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun readImageBytes(inputStream: InputStream, boundary: String): ByteArray {
        val byteOutputStream = ByteArrayOutputStream()
        val boundaryBytes = ("--$boundary").toByteArray()
        var buffer = ByteArray(8192)
        var bytesRead: Int
        var matchCount = 0

        while (true) {
            bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) {
                break
            }

            for (i in 0 until bytesRead) {
                if (buffer[i] == boundaryBytes[matchCount]) {
                    matchCount++
                    if (matchCount == boundaryBytes.size) {
                        // Reached the boundary
                        Log.d("MainActivity2", "Reached the boundary")
                        return byteOutputStream.toByteArray()
                    }
                } else {
                    matchCount = 0
                }
            }

            byteOutputStream.write(buffer, 0, bytesRead)
        }

        return byteOutputStream.toByteArray()
    }
}