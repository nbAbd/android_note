package com.example.myapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.ImageProxy
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter

class AppUtils {
    companion object {
        fun toBitmap(image: ImageProxy): Bitmap {
            val planeProxy = image.planes[0]
            val buffer = planeProxy.buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        @JvmStatic
        @BindingAdapter(value = ["android:image"])
        fun setNoteImage(imageView: ImageView, uri: String?) {
            uri?.let {
                if (it.toUri().isAbsolute && it.toUri().toFile().exists()) {
                    imageView.setImageDrawable(
                        toRoundedBitmapDrawable(
                            imageView.context,
                            it.toUri()
                        )
                    )
                }
            }
        }

        fun toRoundedBitmapDrawable(c: Context, uri: Uri): RoundedBitmapDrawable {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        c.contentResolver,
                        uri
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(
                    c.contentResolver,
                    uri
                )
            }
            val resizedBitmap =
                Bitmap.createScaledBitmap(bitmap, 950, 1200, false)
            val roundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(c.resources, resizedBitmap)
            roundedBitmapDrawable.cornerRadius = 20.0f
            return roundedBitmapDrawable
        }

        fun toSpannableString(
            roundedBitmapDrawable: Drawable,
            clickableSpan: ClickableSpan?
        ): SpannableString {
            val spannableString = SpannableString(" ")
            roundedBitmapDrawable.apply {
                setBounds(
                    0,
                    0,
                    intrinsicWidth,
                    intrinsicHeight
                )
            }

            spannableString.setSpan(
                ImageSpan(roundedBitmapDrawable, ImageSpan.ALIGN_BASELINE),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            clickableSpan?.let {
                spannableString.setSpan(
                    it,
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return spannableString
        }

        fun deleteImageFromUri(uri: String) {
            val fileToDelete = uri.toUri().toFile()
            fileToDelete.let {
                if (it.exists()) {
                    it.delete()
                    Log.e("IMAGE_DELETED", "PATH: $fileToDelete")
                }
            }
        }
    }
}