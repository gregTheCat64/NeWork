package ru.javacat.nework.domain.model

data class User(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
    val favoured: Boolean
)