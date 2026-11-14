package com.mpi.klass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val dataUri = withContext(Dispatchers.IO) {
                    encodeImageUriToDataUri(context, uri)
                }
                if (!dataUri.isNullOrBlank()) {
                    onPhotoPicked(dataUri)
                }
            }
        }
    }
    return {
        launcher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }
}

actual fun resolveProfileImageModel(photoUrl: String?): Any? {
    if (photoUrl.isNullOrBlank()) return null
    if (photoUrl.startsWith("data:image", ignoreCase = true) && photoUrl.contains("base64,", ignoreCase = true)) {
        return try {
            val base64Part = photoUrl.substringAfter("base64,")
            Base64.decode(base64Part, Base64.DEFAULT)
        } catch (_: Exception) {
            null
        }
    }
    return photoUrl
}

private fun encodeImageUriToDataUri(context: Context, uri: Uri): String? {
    return try {
        val resolver = context.contentResolver

        // 1. Decode bounds first to prevent OOM on large camera photos
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        val maxDim = 360
        var sampleSize = 1
        val origW = boundsOptions.outWidth
        val origH = boundsOptions.outHeight
        if (origW > maxDim || origH > maxDim) {
            val halfW = origW / 2
            val halfH = origH / 2
            while ((halfW / sampleSize) >= maxDim && (halfH / sampleSize) >= maxDim) {
                sampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        // 2. Apply EXIF orientation if available
        val rotationDegrees = try {
            resolver.openInputStream(uri)?.use { exifStream ->
                val exif = ExifInterface(exifStream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (_: Exception) {
            0f
        }

        val orientedBitmap = if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            if (rotated != decoded) decoded.recycle()
            rotated
        } else {
            decoded
        }

        // 3. Scale down cleanly to max 360px
        val width = orientedBitmap.width
        val height = orientedBitmap.height
        val scale = minOf(maxDim.toFloat() / width.toFloat(), maxDim.toFloat() / height.toFloat(), 1f)
        val finalBitmap = if (scale < 1f) {
            val targetW = (width * scale).toInt().coerceAtLeast(1)
            val targetH = (height * scale).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(orientedBitmap, targetW, targetH, true)
            if (scaled != orientedBitmap) orientedBitmap.recycle()
            scaled
        } else {
            orientedBitmap
        }

        // 4. Compress to JPEG & encode Base64 data URI for durable local & Firestore storage
        val out = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 78, out)
        finalBitmap.recycle()
        val bytes = out.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (_: Exception) {
        null
    }
}
