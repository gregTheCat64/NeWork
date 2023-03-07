package ru.javacat.nework.dto

data class Notify (
    val content: String,
    val recipientId: Long?
)