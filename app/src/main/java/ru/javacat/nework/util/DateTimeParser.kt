package ru.javacat.nework.util

import android.os.Build
import android.text.format.DateFormat.format
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val currentDateTime: LocalDateTime = LocalDateTime.now()

fun String.toLocalDateTime(): LocalDateTime =
    try {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n'Z'"))
    } catch (e: Exception) {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
    }

fun String.toLocalDateTimeWhithoutZone(): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))


fun LocalDateTime.asString(): String = this.let {
    format(DateTimeFormatter.ofPattern("dd MMMM yyyy в HH:mm"))
}

fun LocalDateTime.asOnlyDate(): String = this.let {
    format(DateTimeFormatter.ofPattern("dd-MM-YYYY"))
}

fun LocalDateTime.asOnlyTime(): String = this.let {
    format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun setDateToPost(postDateTime: LocalDateTime?): String {
    if (currentDateTime.year == postDateTime?.year &&
        currentDateTime.month == postDateTime.month
    ){
        when (currentDateTime.dayOfMonth) {
            postDateTime.dayOfMonth ->
                return "Сегодня в " + postDateTime.asOnlyTime()
            postDateTime.dayOfMonth.plus(1) ->
                return "Вчера в " + postDateTime.asOnlyTime()
        }
    }
        return postDateTime!!.asString()
}