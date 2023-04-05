package ru.javacat.nework.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun String.toLocalDateTime(): LocalDateTime =
    try {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n'Z'"))
    } catch (e:Exception) {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
    }



fun LocalDateTime.asString(): String = this.let {
    format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
}