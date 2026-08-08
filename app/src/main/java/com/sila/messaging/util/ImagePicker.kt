package com.sila.messaging.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

class ImagePicker(private val context: Context) {

    fun createImageUri(): Uri {
        val directory = File(context.cacheDir, "images")
        directory.mkdirs()
        val file = File.createTempFile("sila_", ".jpg", directory)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}

@Composable
fun rememberImagePicker(
    onImagePicked: (Uri) -> Unit
): androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImagePicked(it) }
    }
}

@Composable
fun rememberCameraLauncher(
    onImageCaptured: (Uri) -> Unit
): Pair<androidx.activity.result.ActivityResultLauncher<Uri>, () -> Uri> {
    val context = LocalContext.current
    val picker = remember { ImagePicker(context) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri?.let { onImageCaptured(it) }
    }

    return launcher to {
        picker.createImageUri().also { photoUri = it }
    }
}
