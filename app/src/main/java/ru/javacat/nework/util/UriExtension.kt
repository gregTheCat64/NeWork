package ru.javacat.nework.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

fun Uri.toFile(context: Context): File {
    val file =
        File.createTempFile("tmp", "@gree", context.cacheDir)
    file.outputStream().use {
            val input = context.contentResolver.openInputStream(this)
            input?.copyTo(it)
            input?.close()
    }
    return file
}